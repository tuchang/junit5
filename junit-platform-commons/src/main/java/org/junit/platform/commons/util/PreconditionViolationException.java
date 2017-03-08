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

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;

/**
* Thrown if a <em>precondition</em> is violated.
*
* @since 1.0
* @see Preconditions
*/
@API(Internal)
public class PreconditionViolationException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public PreconditionViolationException(String message) {
		super(message);
	}

	public PreconditionViolationException(String message, Throwable cause) {
		super(message, cause);
	}
}
