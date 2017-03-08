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
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.util.function.Predicate;
import org.junit.jupiter.api.Nested;
import org.junit.platform.commons.meta.API;

/**
* Test if a class is a JUnit Jupiter {@link Nested @Nested} test class.
*
* @since 5.0
*/
@API(Internal)
public class IsNestedTestClass implements Predicate<Class<?>> {

	private static final IsInnerClass isInnerClass = new IsInnerClass();

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (!isInnerClass.test(candidate)) return false;
		return isAnnotated(candidate, Nested.class);
	}
}
