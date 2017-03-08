/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.vintage.engine.discovery;

import static java.util.Collections.emptyList;

import java.util.List;

/** @since 4.12 */
class TestClassRequest {

	private final Class<?> testClass;
	private final List<RunnerTestDescriptorAwareFilter> filters;

	TestClassRequest(Class<?> testClass) {
		this(testClass, emptyList());
	}

	TestClassRequest(Class<?> testClass, List<RunnerTestDescriptorAwareFilter> filters) {
		this.testClass = testClass;
		this.filters = filters;
	}

	Class<?> getTestClass() {
		return testClass;
	}

	List<RunnerTestDescriptorAwareFilter> getFilters() {
		return filters;
	}
}
