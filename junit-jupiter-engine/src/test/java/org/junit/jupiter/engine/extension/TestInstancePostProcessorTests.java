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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
* Integration tests that verify support for {@link TestInstancePostProcessor}.
*
* @since 5.0
*/
public class TestInstancePostProcessorTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	public void instancePostProcessorsInNestedClasses() {
		LauncherDiscoveryRequest request =
				request().selectors(selectClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertThat(callSequence)
				.containsExactly(

						// OuterTestCase
						"fooPostProcessTestInstance:OuterTestCase",
						"beforeOuterMethod",
						"testOuter",

						// InnerTestCase

						"fooPostProcessTestInstance:OuterTestCase",
						"fooPostProcessTestInstance:InnerTestCase",
						"barPostProcessTestInstance:InnerTestCase",
						"beforeOuterMethod",
						"beforeInnerMethod",
						"testInner");
		// @formatter:on
	}

	@Test
	public void testSpecificTestInstancePostProcessorIsCalled() {
		LauncherDiscoveryRequest request =
				request()
						.selectors(selectClass(TestCaseWithTestSpecificTestInstancePostProcessor.class))
						.build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		assertThat(callSequence)
				.containsExactly(
						"fooPostProcessTestInstance:TestCaseWithTestSpecificTestInstancePostProcessor",
						"beforeEachMethod",
						"test");
	}

	// -------------------------------------------------------------------

	@ExtendWith(FooInstancePostProcessor.class)
	private static class OuterTestCase implements Named {

		private String outerName;

		@Override
		public void setName(String name) {
			this.outerName = name;
		}

		@BeforeEach
		void beforeOuterMethod() {
			callSequence.add("beforeOuterMethod");
		}

		@Test
		void testOuter() {
			assertEquals("foo", outerName);
			callSequence.add("testOuter");
		}

		@Nested
		@ExtendWith(BarInstancePostProcessor.class)
		class InnerTestCase implements Named {

			private String innerName;

			@Override
			public void setName(String name) {
				this.innerName = name;
			}

			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				assertEquals("foo", outerName);
				assertEquals("bar", innerName);
				callSequence.add("testInner");
			}
		}
	}

	private static class TestCaseWithTestSpecificTestInstancePostProcessor implements Named {

		private String name;

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@BeforeEach
		void beforeEachMethod() {
			callSequence.add("beforeEachMethod");
		}

		@ExtendWith(FooInstancePostProcessor.class)
		@Test
		void test() {
			callSequence.add("test");
			assertEquals("foo", name);
		}
	}

	private static class FooInstancePostProcessor implements TestInstancePostProcessor {

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context)
				throws Exception {
			if (testInstance instanceof Named) {
				((Named) testInstance).setName("foo");
			}
			callSequence.add("fooPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	private static class BarInstancePostProcessor implements TestInstancePostProcessor {

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context)
				throws Exception {
			if (testInstance instanceof Named) {
				((Named) testInstance).setName("bar");
			}
			callSequence.add("barPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	private interface Named {

		void setName(String name);
	}
}
