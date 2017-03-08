/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.engine.Constants.DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.extension.sub.SystemPropertyCondition;
import org.junit.jupiter.engine.extension.sub.SystemPropertyCondition.SystemProperty;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
* Integration tests that verify support for {@link TestExecutionCondition} and {@link
* ContainerExecutionCondition} extension points in the {@link JupiterTestEngine}.
*
* @since 5.0
*/
public class ExecutionConditionTests extends AbstractJupiterTestEngineTests {

	private static final String FOO = "DisabledTests.foo";
	private static final String BAR = "DisabledTests.bar";
	private static final String BOGUS = "DisabledTests.bogus";

	@BeforeEach
	public void setUp() {
		System.setProperty(FOO, BAR);
	}

	@AfterEach
	public void tearDown() {
		System.clearProperty(FOO);
	}

	@Test
	public void conditionWorksOnContainer() {
		LauncherDiscoveryRequest request =
				request().selectors(selectClass(TestCaseWithContainerExecutionCondition.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	public void conditionWorksOnTest() {
		LauncherDiscoveryRequest request =
				request().selectors(selectClass(TestCaseWithTestExecutionCondition.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(3, eventRecorder.getTestSkippedCount(), "# tests skipped");
	}

	@Test
	public void overrideConditionsUsingFullyQualifiedClassName() {
		String deactivatePattern = SystemPropertyCondition.class.getName();
		assertContainerExecutionConditionOverride(deactivatePattern, 1, 1);
		assertTestExecutionConditionOverride(deactivatePattern, 4, 2, 2);
	}

	@Test
	public void overrideConditionsUsingStar() {
		// "*" should deactivate DisabledCondition and SystemPropertyCondition
		String deactivatePattern = "*";
		assertContainerExecutionConditionOverride(deactivatePattern, 2, 2);
		assertTestExecutionConditionOverride(deactivatePattern, 5, 2, 3);
	}

	@Test
	public void overrideConditionsUsingStarPlusSimpleClassName() {
		// DisabledCondition should remain activated
		String deactivatePattern = "*" + SystemPropertyCondition.class.getSimpleName();
		assertContainerExecutionConditionOverride(deactivatePattern, 1, 1);
		assertTestExecutionConditionOverride(deactivatePattern, 4, 2, 2);
	}

	@Test
	public void overrideConditionsUsingPackageNamePlusDotStar() {
		// DisabledCondition should remain activated
		String deactivatePattern = SystemPropertyCondition.class.getPackage().getName() + ".*";
		assertContainerExecutionConditionOverride(deactivatePattern, 1, 1);
		assertTestExecutionConditionOverride(deactivatePattern, 4, 2, 2);
	}

	@Test
	public void overrideConditionsUsingMultipleWildcards() {
		// DisabledCondition should remain activated
		String deactivatePattern = "org.junit.jupiter.*.System*Condition";
		assertContainerExecutionConditionOverride(deactivatePattern, 1, 1);
		assertTestExecutionConditionOverride(deactivatePattern, 4, 2, 2);
	}

	private void assertContainerExecutionConditionOverride(
			String deactivatePattern, int testStartedCount, int testFailedCount) {
		// @formatter:off
		LauncherDiscoveryRequest request =
				request()
						.selectors(selectClass(TestCaseWithContainerExecutionCondition.class))
						.configurationParameter(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME, deactivatePattern)
						.build();
		// @formatter:on

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(0, eventRecorder.getContainerSkippedCount(), "# containers skipped");
		assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(testStartedCount, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(testFailedCount, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	private void assertTestExecutionConditionOverride(
			String deactivatePattern, int started, int succeeded, int failed) {
		// @formatter:off
		LauncherDiscoveryRequest request =
				request()
						.selectors(selectClass(TestCaseWithTestExecutionCondition.class))
						.configurationParameter(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME, deactivatePattern)
						.build();
		// @formatter:on

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(started, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(succeeded, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(failed, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	// -------------------------------------------------------------------

	@SystemProperty(key = FOO, value = BOGUS)
	private static class TestCaseWithContainerExecutionCondition {

		@Test
		void disabledTest() {
			fail("this should be disabled");
		}

		@Test
		@Disabled
		void atDisabledTest() {
			fail("this should be @Disabled");
		}
	}

	private static class TestCaseWithTestExecutionCondition {

		@Test
		void enabledTest() {}

		@Test
		@Disabled
		void atDisabledTest() {
			fail("this should be @Disabled");
		}

		@Test
		@SystemProperty(key = FOO, value = BAR)
		void systemPropertyEnabledTest() {}

		@Test
		@SystemProperty(key = FOO, value = BOGUS)
		void systemPropertyWithIncorrectValueTest() {
			fail("this should be disabled");
		}

		@Test
		@SystemProperty(key = BOGUS, value = "doesn't matter")
		void systemPropertyNotSetTest() {
			fail("this should be disabled");
		}
	}
}
