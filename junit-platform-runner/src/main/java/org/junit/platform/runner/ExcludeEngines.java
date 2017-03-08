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
* {@code @ExcludeEngines} specifies the {@linkplain #value IDs} of {@link
* org.junit.platform.engine.TestEngine TestEngines} to be excluded when running a test suite via
* {@code @RunWith(JUnitPlatform.class)}.
*
* @since 1.0
* @see JUnitPlatform
* @see IncludeEngines
* @see org.junit.platform.launcher.EngineFilter#excludeEngines
*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(Maintained)
public @interface ExcludeEngines {

	/** One or more Engine IDs to be excluded from the test plan. */
	String[] value();
}
