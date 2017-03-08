/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.commons.util;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.commons.util.ClassFileVisitor.CLASS_FILE_SUFFIX;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.platform.commons.meta.API;

/**
*
*
* <h3>DISCLAIMER</h3>
*
* <p>These utilities are intended solely for usage within the JUnit framework itself. <strong>Any
* usage by external parties is not supported.</strong> Use at your own risk!
*
* @since 1.0
*/
@API(Internal)
class ClasspathScanner {

	private static final Logger LOG = Logger.getLogger(ClasspathScanner.class.getName());

	private static final String DEFAULT_PACKAGE_NAME = "";
	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
	private static final char PACKAGE_SEPARATOR_CHAR = '.';
	private static final String PACKAGE_SEPARATOR_STRING = String.valueOf(PACKAGE_SEPARATOR_CHAR);

	/** Malformed class name InternalError like reported in #401. */
	private static final String MALFORMED_CLASS_NAME_ERROR_MESSAGE = "Malformed class name";

	private final Supplier<ClassLoader> classLoaderSupplier;

	private final BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass;

	ClasspathScanner(
			Supplier<ClassLoader> classLoaderSupplier,
			BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass) {

		this.classLoaderSupplier = classLoaderSupplier;
		this.loadClass = loadClass;
	}

	boolean isPackage(String packageName) {
		assertPackageNameIsPlausible(packageName);

		try {
			return packageName.isEmpty() // default package
					|| getClassLoader().getResources(packagePath(packageName.trim())).hasMoreElements();
		} catch (Exception ex) {
			return false;
		}
	}

	List<Class<?>> scanForClassesInPackage(
			String basePackageName, Predicate<Class<?>> classFilter, Predicate<String> classNameFilter) {
		assertPackageNameIsPlausible(basePackageName);
		Preconditions.notNull(classFilter, "classFilter must not be null");
		Preconditions.notNull(classNameFilter, "classNameFilter must not be null");
		basePackageName = basePackageName.trim();

		return findClassesForUris(
				getRootUrisForPackage(basePackageName), basePackageName, classFilter, classNameFilter);
	}

	List<Class<?>> scanForClassesInClasspathRoot(
			URI root, Predicate<Class<?>> classFilter, Predicate<String> classNameFilter) {
		Preconditions.notNull(root, "root must not be null");
		Preconditions.notNull(classFilter, "classFilter must not be null");
		Preconditions.notNull(classNameFilter, "classNameFilter must not be null");

		return findClassesForUri(root, DEFAULT_PACKAGE_NAME, classFilter, classNameFilter);
	}

	/** Recursively scan for classes in all of the supplied source directories. */
	private List<Class<?>> findClassesForUris(
			List<URI> baseUris,
			String basePackageName,
			Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {

		// @formatter:off
		return baseUris
				.stream()
				.map(baseUri -> findClassesForUri(baseUri, basePackageName, classFilter, classNameFilter))
				.flatMap(Collection::stream)
				.distinct()
				.collect(toList());
		// @formatter:on
	}

	private List<Class<?>> findClassesForUri(
			URI baseUri,
			String basePackageName,
			Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		try (CloseablePath closeablePath = CloseablePath.create(baseUri)) {
			Path baseDir = closeablePath.getPath();
			return findClassesForPath(baseDir, basePackageName, classFilter, classNameFilter);
		} catch (PreconditionViolationException ex) {
			throw ex;
		} catch (Exception ex) {
			logWarning(ex, () -> "Error scanning files for URI " + baseUri);
			return emptyList();
		}
	}

	private List<Class<?>> findClassesForPath(
			Path baseDir,
			String basePackageName,
			Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		Preconditions.condition(Files.exists(baseDir), () -> "baseDir must exist: " + baseDir);
		List<Class<?>> classes = new ArrayList<>();
		try {
			Files.walkFileTree(
					baseDir,
					new ClassFileVisitor(
							classFile ->
									processClassFileSafely(
											baseDir,
											basePackageName,
											classFilter,
											classNameFilter,
											classFile,
											classes::add)));
		} catch (IOException ex) {
			logWarning(ex, () -> "I/O error scanning files in " + baseDir);
		}
		return classes;
	}

	private void processClassFileSafely(
			Path baseDir,
			String basePackageName,
			Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter,
			Path classFile,
			Consumer<Class<?>> classConsumer) {
		Optional<Class<?>> clazz = Optional.empty();
		try {
			String fullyQualifiedClassName =
					determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
			if (classNameFilter.test(fullyQualifiedClassName)) {
				clazz = this.loadClass.apply(fullyQualifiedClassName, getClassLoader());
				clazz.filter(classFilter).ifPresent(classConsumer);
			}
		} catch (InternalError internalError) {
			handleInternalError(classFile, clazz, internalError);
		} catch (Throwable throwable) {
			handleThrowable(classFile, throwable);
		}
	}

	private String determineFullyQualifiedClassName(
			Path baseDir, String basePackageName, Path classFile) {
		// @formatter:off
		return Stream.of(
						basePackageName,
						determineSubpackageName(baseDir, classFile),
						determineSimpleClassName(classFile))
				.filter(value -> !value.isEmpty()) // Handle default package appropriately.
				.collect(joining(PACKAGE_SEPARATOR_STRING));
		// @formatter:on
	}

	private String determineSimpleClassName(Path classFile) {
		String fileName = classFile.getFileName().toString();
		return fileName.substring(0, fileName.length() - CLASS_FILE_SUFFIX.length());
	}

	private String determineSubpackageName(Path baseDir, Path classFile) {
		Path relativePath = baseDir.relativize(classFile.getParent());
		String pathSeparator = baseDir.getFileSystem().getSeparator();
		String subpackageName =
				relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
		if (subpackageName.endsWith(pathSeparator)) {
			// Workaround for JDK bug: https://bugs.openjdk.java.net/browse/JDK-8153248
			subpackageName =
					subpackageName.substring(0, subpackageName.length() - pathSeparator.length());
		}
		return subpackageName;
	}

	private void handleInternalError(Path classFile, Optional<Class<?>> clazz, InternalError ex) {
		if (MALFORMED_CLASS_NAME_ERROR_MESSAGE.equals(ex.getMessage())) {
			logMalformedClassName(classFile, clazz, ex);
		} else {
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void handleThrowable(Path classFile, Throwable throwable) {
		rethrowIfBlacklisted(throwable);
		logGenericFileProcessingException(classFile, throwable);
	}

	private void logMalformedClassName(Path classFile, Optional<Class<?>> clazz, InternalError ex) {
		try {
			if (clazz.isPresent()) {
				// Do not use getSimpleName() or getCanonicalName() here because they will likely
				// throw another exception due to the underlying error.
				logWarning(
						ex,
						() ->
								format(
										"The java.lang.Class loaded from path [%s] has a malformed class name [%s].",
										classFile.toAbsolutePath(), clazz.get().getName()));
			} else {
				logWarning(
						ex,
						() ->
								format(
										"The java.lang.Class loaded from path [%s] has a malformed class name.",
										classFile.toAbsolutePath()));
			}
		} catch (Throwable t) {
			ex.addSuppressed(t);
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void logGenericFileProcessingException(Path classFile, Throwable throwable) {
		logWarning(
				throwable,
				() ->
						format(
								"Failed to load java.lang.Class for path [%s] during classpath scanning.",
								classFile.toAbsolutePath()));
	}

	private ClassLoader getClassLoader() {
		return this.classLoaderSupplier.get();
	}

	private static void assertPackageNameIsPlausible(String packageName) {
		Preconditions.notNull(packageName, "package name must not be null");
		Preconditions.condition(
				DEFAULT_PACKAGE_NAME.equals(packageName) || StringUtils.isNotBlank(packageName),
				"package name must not contain only whitespace");
	}

	private static String packagePath(String packageName) {
		return packageName.replace(PACKAGE_SEPARATOR_CHAR, CLASSPATH_RESOURCE_PATH_SEPARATOR);
	}

	private List<URI> getRootUrisForPackage(String basePackageName) {
		try {
			Enumeration<URL> resources = getClassLoader().getResources(packagePath(basePackageName));
			List<URI> uris = new ArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				uris.add(resource.toURI());
			}
			return uris;
		} catch (Exception ex) {
			logWarning(
					ex, () -> "Error reading URIs from class loader for base package " + basePackageName);
			return emptyList();
		}
	}

	private static void logWarning(Throwable throwable, Supplier<String> msgSupplier) {
		LOG.log(Level.WARNING, throwable, msgSupplier);
	}
}
