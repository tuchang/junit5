/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.vintage.engine.samples.junit4;

import static java.util.stream.IntStream.range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/** @since 4.12 */
public class ExceptionThrowingRunner extends Runner {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface ChildCount {

		int value();
	}

	private final Class<?> testClass;

	public ExceptionThrowingRunner(Class<?> testClass) {
		this.testClass = testClass;
	}

	@Override
	public Description getDescription() {
		Description suiteDescription = Description.createSuiteDescription(testClass);
		ChildCount childCountAnnotation = testClass.getAnnotation(ChildCount.class);
		int childCount = Optional.ofNullable(childCountAnnotation).map(ChildCount::value).orElse(0);
		// @formatter:off
		range(0, childCount)
				.mapToObj(index -> Description.createTestDescription(testClass, "Test #" + index))
				.forEach(suiteDescription::addChild);
		// @formatter:on
		return suiteDescription;
	}

	@Override
	public void run(RunNotifier notifier) {
		throw new RuntimeException("Simulated exception in custom runner for " + testClass.getName());
	}
}
