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
import org.junit.platform.engine.discovery.ClassNameFilter;

/**
* {@code @ExcludeClassNamePatterns} specifies regular expressions that are used to match against
* fully qualified class names when running a test suite via {@code @RunWith(JUnitPlatform.class)}.
*
* <p>The patterns are combined using OR semantics, i.e. if the fully qualified name of a class
* matches against at least one of the patterns, the class will be excluded from the test plan.
*
* @since 1.0
* @see JUnitPlatform
* @see ClassNameFilter#excludeClassNamePatterns
*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(Maintained)
public @interface ExcludeClassNamePatterns {

	/** Regular expressions used to match against fully qualified class names. */
	String[] value();
}
