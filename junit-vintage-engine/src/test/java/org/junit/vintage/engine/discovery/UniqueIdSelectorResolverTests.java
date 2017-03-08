/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.vintage.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.vintage.engine.VintageUniqueIdBuilder.engineId;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.vintage.engine.RecordCollectingLogger;
import org.junit.vintage.engine.VintageUniqueIdBuilder;

/** @since 4.12 */
class UniqueIdSelectorResolverTests {

	private RecordCollectingLogger logger = new RecordCollectingLogger();
	private TestClassCollector collector = new TestClassCollector();

	@Test
	void logsWarningOnUnloadableTestClass() {
		UniqueId uniqueId = VintageUniqueIdBuilder.uniqueIdForClass("foo.bar.UnknownClass");
		UniqueIdSelector selector = selectUniqueId(uniqueId);

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		assertNoRequests();
		assertLoggedWarning(
				"Unresolvable Unique ID (" + uniqueId + "): Unknown class foo.bar.UnknownClass");
	}

	@Test
	void logsWarningForEngineUniqueId() {
		String uniqueId = engineId().toString();
		UniqueIdSelector selector = selectUniqueId(uniqueId);

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		assertNoRequests();
		assertLoggedWarning(
				"Unresolvable Unique ID (" + engineId() + "): Cannot resolve the engine's unique ID");
	}

	@Test
	void ignoresUniqueIdsOfOtherEngines() {
		UniqueId uniqueId = UniqueId.forEngine("someEngine");
		UniqueIdSelector selector = selectUniqueId(uniqueId);

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		assertNoRequests();
		assertThat(logger.getLogRecords()).isEmpty();
	}

	@Test
	void logsWarningOnUnexpectedTestDescriptor() {
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID).append("wrong-type", "foo:bar");
		UniqueIdSelector selector = selectUniqueId(uniqueId);

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		assertNoRequests();
		assertLoggedWarning(
				"Unresolvable Unique ID ("
						+ uniqueId
						+ "): Unique ID segment after engine segment must be of type \"runner\"");
	}

	private void assertLoggedWarning(String expectedMessage) {
		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals(expectedMessage, logRecord.getMessage());
	}

	private void assertNoRequests() {
		Set<TestClassRequest> requests = collector.toRequests(c -> true);
		assertThat(requests).isEmpty();
	}
}
