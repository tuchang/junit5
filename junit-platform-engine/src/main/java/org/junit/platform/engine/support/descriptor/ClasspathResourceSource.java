/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Objects;
import java.util.Optional;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestSource;

/**
* <em>Classpath resource</em> based {@link org.junit.platform.engine.TestSource} with an optional
* {@linkplain FilePosition position}.
*
* @since 1.0
* @see org.junit.platform.engine.discovery.ClasspathResourceSelector
*/
@API(Experimental)
public class ClasspathResourceSource implements TestSource {

	private static final long serialVersionUID = 1L;

	private final String classpathResourceName;
	private final FilePosition filePosition;

	/**
	* Create a new {@code ClasspathResourceSource} using the supplied classpath resource name.
	*
	* <p>The name of a <em>classpath resource</em> must follow the semantics for resource paths as
	* defined in {@link ClassLoader#getResource(String)}.
	*
	* <p>If the supplied classpath resource name is prefixed with a slash ({@code /}), the slash will
	* be removed.
	*
	* @param classpathResourceName the name of the classpath resource; never {@code null} or blank
	* @see ClassLoader#getResource(String)
	* @see ClassLoader#getResourceAsStream(String)
	* @see ClassLoader#getResources(String)
	*/
	public ClasspathResourceSource(String classpathResourceName) {
		this(classpathResourceName, null);
	}

	/**
	* Create a new {@code ClasspathResourceSource} using the supplied classpath resource name and
	* {@link FilePosition}.
	*
	* <p>The name of a <em>classpath resource</em> must follow the semantics for resource paths as
	* defined in {@link ClassLoader#getResource(String)}.
	*
	* <p>If the supplied classpath resource name is prefixed with a slash ({@code /}), the slash will
	* be removed.
	*
	* @param classpathResourceName the name of the classpath resource; never {@code null} or blank
	* @param filePosition the position in the classpath resource; may be {@code null}
	*/
	public ClasspathResourceSource(String classpathResourceName, FilePosition filePosition) {
		Preconditions.notBlank(
				classpathResourceName, "Classpath resource name must not be null or blank");
		boolean startsWithSlash = classpathResourceName.startsWith("/");
		this.classpathResourceName =
				(startsWithSlash ? classpathResourceName.substring(1) : classpathResourceName);
		this.filePosition = filePosition;
	}

	/**
	* Get the name of the source <em>classpath resource</em>.
	*
	* <p>The name of a <em>classpath resource</em> follows the semantics for resource paths as
	* defined in {@link ClassLoader#getResource(String)}.
	*
	* @see ClassLoader#getResource(String)
	* @see ClassLoader#getResourceAsStream(String)
	* @see ClassLoader#getResources(String)
	*/
	public String getClasspathResourceName() {
		return this.classpathResourceName;
	}

	/** Get the {@link FilePosition}, if available. */
	public final Optional<FilePosition> getPosition() {
		return Optional.ofNullable(this.filePosition);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ClasspathResourceSource that = (ClasspathResourceSource) o;
		return Objects.equals(this.classpathResourceName, that.classpathResourceName)
				&& Objects.equals(this.filePosition, that.filePosition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.classpathResourceName, this.filePosition);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("classpathResourceName", this.classpathResourceName)
				.append("filePosition", this.filePosition)
				.toString();
		// @formatter:on
	}
}
