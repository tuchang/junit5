/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
* A {@link DiscoverySelector} that selects a file so that {@link
* org.junit.platform.engine.TestEngine TestEngines} can discover tests or containers based on files
* in the file system.
*
* @since 1.0
* @see DirectorySelector
* @see #getFile()
* @see #getPath()
* @see #getRawPath()
*/
@API(Experimental)
public class FileSelector implements DiscoverySelector {

	private final String path;

	FileSelector(String path) {
		this.path = path;
	}

	/**
	* Get the selected file as a {@link java.io.File}.
	*
	* @see #getPath()
	* @see #getRawPath()
	*/
	public File getFile() {
		return new File(this.path);
	}

	/**
	* Get the selected file as a {@link java.nio.file.Path} using the {@linkplain
	* FileSystems#getDefault default} {@link FileSystem}.
	*
	* @see #getFile()
	* @see #getRawPath()
	*/
	public Path getPath() {
		return Paths.get(this.path);
	}

	/**
	* Get the selected file as a <em>raw path</em>.
	*
	* @see #getFile()
	* @see #getPath()
	*/
	public String getRawPath() {
		return this.path;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", this.path).toString();
	}
}
