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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.Locale;
import java.util.function.Function;

/** @since 4.12 */
class UniqueIdStringifier implements Function<Serializable, String> {

	static final Charset CHARSET = StandardCharsets.UTF_8;

	@Override
	public String apply(Serializable uniqueId) {
		if (uniqueId instanceof CharSequence) {
			return uniqueId.toString();
		}
		if (uniqueId instanceof Number) {
			return NumberFormat.getInstance(Locale.US).format(uniqueId);
		}
		return encodeBase64(serialize(uniqueId));
	}

	private byte[] serialize(Serializable uniqueId) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(byteStream)) {
			out.writeObject(uniqueId);
		} catch (IOException e) {
			return uniqueId.toString().getBytes(CHARSET);
		}
		return byteStream.toByteArray();
	}

	private String encodeBase64(byte[] bytes) {
		return new String(Base64.getEncoder().encode(bytes), CHARSET);
	}
}
