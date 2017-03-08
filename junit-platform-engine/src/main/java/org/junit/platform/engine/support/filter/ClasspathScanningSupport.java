/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.engine.support.filter;

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.engine.Filter.composeFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.PackageNameFilter;

/**
* Support utility methods for classpath scanning.
*
* @since 1.0
*/
@API(Experimental)
public class ClasspathScanningSupport {

	/**
	* Build a {@link Predicate} for fully qualified class names to be used for classpath scanning
	* from an {@link EngineDiscoveryRequest}.
	*
	* @param request the request to build a predicate from
	*/
	public static Predicate<String> buildClassNamePredicate(EngineDiscoveryRequest request) {
		List<DiscoveryFilter<String>> filters = new ArrayList<>();
		filters.addAll(request.getDiscoveryFiltersByType(ClassNameFilter.class));
		filters.addAll(request.getDiscoveryFiltersByType(PackageNameFilter.class));
		return composeFilters(filters).toPredicate();
	}
}
