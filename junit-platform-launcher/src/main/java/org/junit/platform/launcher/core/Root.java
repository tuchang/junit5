/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.launcher.core;

import static org.junit.platform.engine.Filter.composeFilters;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
* Represents the root of all discovered {@link TestEngine TestEngines} and their {@link
* TestDescriptor TestDescriptors}.
*
* @since 1.0
*/
class Root {

	private static final TestDescriptor.Visitor REMOVE_DESCRIPTORS_WITHOUT_TESTS =
			descriptor -> {
				if (!descriptor.isRoot() && !descriptor.hasTests()) {
					descriptor.removeFromHierarchy();
				}
			};

	private final Map<TestEngine, TestDescriptor> testEngineDescriptors = new LinkedHashMap<>(4);

	/** Add an {@code engine}'s root {@link TestDescriptor}. */
	void add(TestEngine engine, TestDescriptor testDescriptor) {
		this.testEngineDescriptors.put(engine, testDescriptor);
	}

	Iterable<TestEngine> getTestEngines() {
		return this.testEngineDescriptors.keySet();
	}

	Collection<TestDescriptor> getEngineDescriptors() {
		return this.testEngineDescriptors.values();
	}

	TestDescriptor getTestDescriptorFor(TestEngine testEngine) {
		return this.testEngineDescriptors.get(testEngine);
	}

	void applyPostDiscoveryFilters(LauncherDiscoveryRequest discoveryRequest) {
		Filter<TestDescriptor> postDiscoveryFilter =
				composeFilters(discoveryRequest.getPostDiscoveryFilters());
		TestDescriptor.Visitor removeExcludedTestDescriptors =
				descriptor -> {
					if (!descriptor.isRoot() && isExcluded(descriptor, postDiscoveryFilter)) {
						descriptor.removeFromHierarchy();
					}
				};
		acceptInAllTestEngines(removeExcludedTestDescriptors);
	}

	/**
	* Prune all branches in the tree of {@link TestDescriptor TestDescriptors} that do not have
	* executable tests.
	*
	* <p>If a {@link TestEngine} ends up with no {@code TestDescriptors} after pruning, it will
	* <strong>not</strong> be removed.
	*/
	void prune() {
		acceptInAllTestEngines(REMOVE_DESCRIPTORS_WITHOUT_TESTS);
	}

	private boolean isExcluded(
			TestDescriptor descriptor, Filter<TestDescriptor> postDiscoveryFilter) {
		return descriptor.getChildren().isEmpty() && postDiscoveryFilter.apply(descriptor).excluded();
	}

	private void acceptInAllTestEngines(TestDescriptor.Visitor visitor) {
		this.testEngineDescriptors.values().forEach(descriptor -> descriptor.accept(visitor));
	}
}
