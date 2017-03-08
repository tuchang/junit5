/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.vintage.engine.samples.junit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/** @since 4.12 */
@RunWith(RunnerWithCustomUniqueIds.class)
public class JUnit4TestCaseWithRunnerWithCustomUniqueIds {

	@Test
	public void test() {
		Assert.fail();
	}
}
