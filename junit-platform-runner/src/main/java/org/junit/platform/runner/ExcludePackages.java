/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.runner;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.platform.commons.meta.API;

/**
* {@code @ExcludePackages} specifies the {@linkplain #value packages} to be excluded when running a
* test suite via {@code @RunWith(JUnitPlatform.class)}.
*
* @since 1.0
* @see JUnitPlatform
* @see IncludePackages
*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(Maintained)
public @interface ExcludePackages {

	/** One or more tags to exclude. */
	String[] value();
}
