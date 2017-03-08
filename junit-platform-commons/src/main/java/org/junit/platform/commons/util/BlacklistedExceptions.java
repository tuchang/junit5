/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.commons.util;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.Collections;
import java.util.List;
import org.junit.platform.commons.meta.API;

/**
* Internal utilities for working with <em>blacklisted</em> exceptions.
*
* <p><em>Blacklisted</em> exceptions are those that should always terminate test plan execution
* immediately.
*
* <h4>Currently Blacklisted Exceptions</h4>
*
* <ul>
*   <li>{@link OutOfMemoryError}
* </ul>
*
* <h3>DISCLAIMER</h3>
*
* <p>These utilities are intended solely for usage within the JUnit framework itself. <strong>Any
* usage by external parties is not supported.</strong> Use at your own risk!
*
* @since 1.0
*/
@API(Internal)
public final class BlacklistedExceptions {

	private static final List<Class<? extends Throwable>> blacklist =
			Collections.singletonList(OutOfMemoryError.class);

	///CLOVER:OFF
	private BlacklistedExceptions() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	* Rethrow the supplied {@link Throwable exception} if it is <em>blacklisted</em>.
	*
	* <p>If the supplied {@code exception} is not <em>blacklisted</em>, this method does nothing.
	*/
	public static void rethrowIfBlacklisted(Throwable exception) {
		if (blacklist.stream().anyMatch(exceptionType -> exceptionType.isInstance(exception))) {
			ExceptionUtils.throwAsUncheckedException(exception);
		}
	}
}
