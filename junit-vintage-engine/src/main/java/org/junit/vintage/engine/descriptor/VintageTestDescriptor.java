/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.vintage.engine.descriptor;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.isEqual;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.experimental.categories.Category;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.runner.Description;

/** @since 4.12 */
@API(Internal)
public class VintageTestDescriptor extends AbstractTestDescriptor {

	public static final String ENGINE_ID = "junit-vintage";
	public static final String SEGMENT_TYPE_RUNNER = "runner";
	public static final String SEGMENT_TYPE_TEST = "test";

	private final Description description;

	public VintageTestDescriptor(
			TestDescriptor parent, String segmentType, String segmentValue, Description description) {

		this(parent, segmentType, segmentValue, description, toTestSource(description));
	}

	VintageTestDescriptor(
			TestDescriptor parent,
			String segmentType,
			String segmentValue,
			Description description,
			Optional<? extends TestSource> source) {

		this(parent, segmentType, segmentValue, description, generateDisplayName(description), source);
	}

	VintageTestDescriptor(
			TestDescriptor parent,
			String segmentType,
			String segmentValue,
			Description description,
			String displayName,
			Optional<? extends TestSource> source) {

		super(parent.getUniqueId().append(segmentType, segmentValue), displayName);

		this.description = description;
		source.ifPresent(this::setSource);
	}

	private static String generateDisplayName(Description description) {
		return description.getMethodName() != null
				? description.getMethodName()
				: description.getDisplayName();
	}

	public Description getDescription() {
		return description;
	}

	@Override
	public boolean isTest() {
		return description.isTest();
	}

	@Override
	public boolean isContainer() {
		return description.isSuite();
	}

	@Override
	public Set<TestTag> getTags() {
		Set<TestTag> result = new LinkedHashSet<>();
		getParent().ifPresent(parent -> result.addAll(parent.getTags()));
		// @formatter:off
		getDeclaredCategories()
				.ifPresent(
						categoryClasses ->
								stream(categoryClasses)
										.map(ReflectionUtils::getAllAssignmentCompatibleClasses)
										.flatMap(Collection::stream)
										.distinct()
										.map(Class::getName)
										.map(TestTag::create)
										.forEachOrdered(result::add));
		// @formatter:on
		return result;
	}

	private Optional<Class<?>[]> getDeclaredCategories() {
		Category annotation = description.getAnnotation(Category.class);
		return Optional.ofNullable(annotation).map(Category::value);
	}

	private static Optional<TestSource> toTestSource(Description description) {
		Class<?> testClass = description.getTestClass();
		if (testClass != null) {
			String methodName = description.getMethodName();
			if (methodName != null) {
				MethodSource methodSource = toMethodSource(testClass, methodName);
				if (methodSource != null) {
					return Optional.of(methodSource);
				}
			}
			return Optional.of(new ClassSource(testClass));
		}
		return Optional.empty();
	}

	private static MethodSource toMethodSource(Class<?> testClass, String methodName) {
		if (methodName.contains("[") && methodName.endsWith("]")) {
			// special case for parameterized tests
			return toMethodSource(testClass, methodName.substring(0, methodName.indexOf("[")));
		} else {
			List<Method> methods = findMethods(testClass, where(Method::getName, isEqual(methodName)));
			return (methods.size() == 1) ? new MethodSource(getOnlyElement(methods)) : null;
		}
	}
}
