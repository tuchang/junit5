/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
* Integration tests that verify support for {@link Disabled @Disabled} in the {@link
* JupiterTestEngine}.
*
* @since 5.0
*/
public class DisabledTests extends AbstractJupiterTestEngineTests {

	@Test
	public void executeTestsWithDisabledTestClass() {
		LauncherDiscoveryRequest request =
				request().selectors(selectClass(DisabledTestClassTestCase.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	public void executeTestsWithDisabledTestMethods() throws Exception {
		LauncherDiscoveryRequest request =
				request().selectors(selectClass(DisabledTestMethodsTestCase.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestSkippedCount(), "# tests skipped");

		String method = DisabledTestMethodsTestCase.class.getDeclaredMethod("disabledTest").toString();
		String reason = eventRecorder.getSkippedTestEvents().get(0).getPayload(String.class).get();
		assertEquals(method + " is @Disabled", reason);
	}

	// -------------------------------------------------------------------

	@Disabled
	private static class DisabledTestClassTestCase {

		@Test
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}

	private static class DisabledTestMethodsTestCase {

		@Test
		void enabledTest() {}

		@Test
		@Disabled
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}
}
