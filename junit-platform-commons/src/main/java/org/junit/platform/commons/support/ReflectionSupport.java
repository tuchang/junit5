/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.commons.support;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;

/**
* Common reflection and classpath scanning support.
*
* @since 1.0
*/
@API(Maintained)
public final class ReflectionSupport {

	///CLOVER:OFF
	private ReflectionSupport() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	* Find all {@linkplain Class classes} of the supplied {@code root} {@linkplain URI} that match
	* the specified {@code classTester} and {@code classNameFilter} predicates.
	*
	* @param root the root URI to start scanning
	* @param classTester the class type filter; never {@code null}
	* @param classNameFilter the class name filter; never {@code null}
	* @return the list of all such classes found; never {@code null}
	*/
	public static List<Class<?>> findAllClassesInClasspathRoot(
			URI root, Predicate<Class<?>> classTester, Predicate<String> classNameFilter) {
		return ReflectionUtils.findAllClassesInClasspathRoot(root, classTester, classNameFilter);
	}

	/**
	* Find all {@linkplain Class classes} of the supplied {@code basePackageName} that match the
	* specified {@code classTester} and {@code classNameFilter} predicates.
	*
	* @param basePackageName the base package name to start scanning
	* @param classTester the class type filter; never {@code null}
	* @param classNameFilter the class name filter; never {@code null}
	* @return the list of all such classes found; never {@code null}
	*/
	public static List<Class<?>> findAllClassesInPackage(
			String basePackageName, Predicate<Class<?>> classTester, Predicate<String> classNameFilter) {
		return ReflectionUtils.findAllClassesInPackage(basePackageName, classTester, classNameFilter);
	}

	/**
	* Find all {@linkplain Method methods} of the supplied class or interface that match the
	* specified {@code predicate}.
	*
	* <p>If you're are looking for methods annotated with a certain annotation type, consider using
	* {@linkplain AnnotationSupport#findAnnotatedMethods(Class, Class, MethodSortOrder)}.
	*
	* @param clazz the class or interface in which to find the methods; never {@code null}
	* @param predicate the method filter; never {@code null}
	* @param sortOrder the method sort order; never {@code null}
	* @return the list of all such methods found; never {@code null}
	*/
	public static List<Method> findMethods(
			Class<?> clazz, Predicate<Method> predicate, MethodSortOrder sortOrder) {
		return ReflectionUtils.findMethods(
				clazz, predicate, ReflectionUtils.MethodSortOrder.valueOf(sortOrder.name()));
	}
}
