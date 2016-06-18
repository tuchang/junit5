/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Arrays.asList;
import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.launcher.core.TestDiscoveryRequestBuilder.request;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.gen5.engine.test.event.ExecutionEventRecorder;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.AbstractJUnit5TestEngineTests;
import org.junit.jupiter.engine.JUnit5TestEngine;

/**
 * Integration tests that verify support for {@link BeforeTestExecutionCallback},
 * {@link AfterTestExecutionCallback}, {@link BeforeEach}, and {@link AfterEach}
 * in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class BeforeAndAfterTestExecutionCallbackTests extends AbstractJUnit5TestEngineTests {

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	public void beforeEachAndAfterEachCallbacks() {
		TestDiscoveryRequest request = request().selectors(selectClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(

			// OuterTestCase
			"beforeEachOuter",
				"fooBefore",
				"barBefore",
					"testOuter",
				"barAfter",
				"fooAfter",
			"afterEachOuter",

			// InnerTestCase
			"beforeEachOuter",
				"beforeEachInner",
					"fooBefore",
					"barBefore",
						"fizzBefore",
							"testInner",
						"fizzAfter",
					"barAfter",
					"fooAfter",
				"afterEachInner",
			"afterEachOuter"

		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	public void beforeEachAndAfterEachCallbacksDeclaredOnSuperclassAndSubclass() {
		TestDiscoveryRequest request = request().selectors(selectClass(ChildTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBefore",
			"barBefore",
				"testChild",
			"barAfter",
			"fooAfter"
		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	public void beforeEachAndAfterEachCallbacksDeclaredOnInterfaceAndClass() {
		TestDiscoveryRequest request = request().selectors(selectClass(TestInterfaceTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(

			// Test Interface
			"fooBefore",
				"barBefore",
					"defaultTestMethod",
				"barAfter",
			"fooAfter",

			// Test Class
			"fooBefore",
				"barBefore",
					"localTestMethod",
				"barAfter",
			"fooAfter"

		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	// -------------------------------------------------------------------

	private static List<String> callSequence = new ArrayList<>();

	@ExtendWith(FooTestExecutionCallbacks.class)
	private static class ParentTestCase {
	}

	@ExtendWith(BarTestExecutionCallbacks.class)
	private static class ChildTestCase extends ParentTestCase {

		@Test
		void test() {
			callSequence.add("testChild");
		}

	}

	@ExtendWith(FooTestExecutionCallbacks.class)
	private interface TestInterface {

		@Test
		default void defaultTest() {
			callSequence.add("defaultTestMethod");
		}

	}

	@ExtendWith(BarTestExecutionCallbacks.class)
	private static class TestInterfaceTestCase implements TestInterface {

		@Test
		void localTest() {
			callSequence.add("localTestMethod");
		}
	}

	@ExtendWith({ FooTestExecutionCallbacks.class, BarTestExecutionCallbacks.class })
	private static class OuterTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEachOuter");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterEachOuter");
		}

		@Nested
		@ExtendWith(FizzTestExecutionCallbacks.class)
		class InnerTestCase {

			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeEachInner");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}

			@AfterEach
			void afterInnerMethod() {
				callSequence.add("afterEachInner");
			}
		}

	}

	private static class FooTestExecutionCallbacks implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("fooBefore");
		}

		@Override
		public void afterTestExecution(TestExtensionContext context) {
			callSequence.add("fooAfter");
		}
	}

	private static class BarTestExecutionCallbacks implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("barBefore");
		}

		@Override
		public void afterTestExecution(TestExtensionContext context) {
			callSequence.add("barAfter");
		}
	}

	private static class FizzTestExecutionCallbacks implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("fizzBefore");
		}

		@Override
		public void afterTestExecution(TestExtensionContext context) {
			callSequence.add("fizzAfter");
		}
	}

}
