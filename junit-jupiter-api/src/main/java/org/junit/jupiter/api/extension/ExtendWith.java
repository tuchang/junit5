/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.platform.commons.meta.API;

/**
* {@code @ExtendWith} is a {@linkplain Repeatable repeatable} annotation that is used to register
* {@linkplain Extension extensions} for the annotated test class or test method.
*
* <h3>Supported Extension APIs</h3>
*
* <ul>
*   <li>{@link ContainerExecutionCondition}
*   <li>{@link TestExecutionCondition}
*   <li>{@link BeforeAllCallback}
*   <li>{@link AfterAllCallback}
*   <li>{@link BeforeEachCallback}
*   <li>{@link AfterEachCallback}
*   <li>{@link BeforeTestExecutionCallback}
*   <li>{@link AfterTestExecutionCallback}
*   <li>{@link TestInstancePostProcessor}
*   <li>{@link ParameterResolver}
*   <li>{@link TestExecutionExceptionHandler}
* </ul>
*
* @since 5.0
* @see Extension
*/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(Extensions.class)
@API(Experimental)
public @interface ExtendWith {

	/** An array of one or more {@link Extension} classes to register. */
	Class<? extends Extension>[] value();
}
