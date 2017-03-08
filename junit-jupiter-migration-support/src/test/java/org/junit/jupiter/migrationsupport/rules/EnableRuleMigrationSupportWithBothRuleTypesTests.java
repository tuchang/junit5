/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.jupiter.migrationsupport.rules;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.migrationsupport.rules.FailAfterAllHelper.fail;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.Verifier;

@EnableRuleMigrationSupport
public class EnableRuleMigrationSupportWithBothRuleTypesTests {

	private static boolean afterOfRule1WasExecuted = false;

	private static boolean beforeOfRule2WasExecuted = false;
	private static boolean afterOfRule2WasExecuted = false;

	@Rule
	public Verifier verifier1 =
			new Verifier() {

				@Override
				protected void verify() throws Throwable {
					afterOfRule1WasExecuted = true;
				}
			};

	private ExternalResource resource2 =
			new ExternalResource() {
				@Override
				protected void before() throws Throwable {
					beforeOfRule2WasExecuted = true;
				}

				@Override
				protected void after() {
					afterOfRule2WasExecuted = true;
				}
			};

	@Rule
	public ExternalResource getResource2() {
		return resource2;
	}

	@Test
	void beforeMethodOfBothRule2WasExecuted() {
		assertTrue(beforeOfRule2WasExecuted);
	}

	@AfterAll
	static void afterMethodsOfBothRulesWereExecuted() {
		if (!afterOfRule1WasExecuted) fail();
		if (!afterOfRule2WasExecuted) fail();
	}
}
