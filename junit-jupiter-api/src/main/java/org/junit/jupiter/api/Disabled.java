/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Stable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.platform.commons.meta.API;

/**
* {@code @Disabled} is used to signal that the annotated test class or test method is currently
* <em>disabled</em> and should not be executed.
*
* <p>When applied at the class level, all test methods within that class are automatically disabled
* as well.
*
* @since 5.0
*/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Stable)
public @interface Disabled {

	/** The reason this test is disabled. */
	String value() default "";
}
