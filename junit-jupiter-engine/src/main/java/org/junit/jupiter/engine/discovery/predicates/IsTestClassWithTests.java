/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.jupiter.engine.discovery.predicates;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;

/**
* Test if a class is a JUnit Jupiter test class containing executable tests, test factories, or
* nested tests.
*
* @since 5.0
*/
@API(Internal)
public class IsTestClassWithTests implements Predicate<Class<?>> {

	private static final IsTestMethod isTestMethod = new IsTestMethod();

	private static final IsTestFactoryMethod isTestFactoryMethod = new IsTestFactoryMethod();

	private static final Predicate<Method> isTestOrTestFactoryMethod =
			isTestMethod.or(isTestFactoryMethod);

	private static final IsPotentialTestContainer isPotentialTestContainer =
			new IsPotentialTestContainer();

	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	@Override
	public boolean test(Class<?> candidate) {
		// please do not collapse into single return
		if (!isPotentialTestContainer.test(candidate)) {
			return false;
		}
		return hasTestOrTestFactoryMethods(candidate) || hasNestedTests(candidate);
	}

	private boolean hasTestOrTestFactoryMethods(Class<?> candidate) {
		return !ReflectionUtils.findMethods(candidate, isTestOrTestFactoryMethod).isEmpty();
	}

	private boolean hasNestedTests(Class<?> candidate) {
		return !ReflectionUtils.findNestedClasses(candidate, isNestedTestClass).isEmpty();
	}
}
