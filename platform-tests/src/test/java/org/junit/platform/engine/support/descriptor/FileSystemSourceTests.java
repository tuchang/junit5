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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
* Unit tests for {@link FileSource} and {@link DirectorySource}.
*
* @since 1.0
*/
class FileSystemSourceTests extends AbstractTestSourceTests {

	@Test
	void nullSourceFileOrDirectoryYieldsException() {
		assertThrows(PreconditionViolationException.class, () -> new FileSource(null));
	}

	@Test
	void directory() throws Exception {
		File canonicalDir = new File(".").getCanonicalFile();
		File relativeDir = new File("..", canonicalDir.getName());

		DirectorySource source = new DirectorySource(relativeDir);

		assertThat(source.getUri()).isEqualTo(canonicalDir.toURI());
		assertThat(source.getFile()).isEqualTo(canonicalDir);
	}

	@Test
	void fileWithoutPosition() throws Exception {
		File canonicalDir = new File(".").getCanonicalFile();
		File relativeDir = new File("..", canonicalDir.getName());
		File relativeFile = new File(relativeDir, "test.txt");
		File canonicalFile = relativeFile.getCanonicalFile();

		FileSource source = new FileSource(relativeFile);

		assertThat(source.getUri()).isEqualTo(canonicalFile.toURI());
		assertThat(source.getFile()).isEqualTo(canonicalFile);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void fileWithPosition() throws Exception {
		File file = new File("test.txt");
		FilePosition position = new FilePosition(42, 23);
		FileSource source = new FileSource(file, position);

		assertThat(source.getUri()).isEqualTo(file.getAbsoluteFile().toURI());
		assertThat(source.getFile()).isEqualTo(file.getAbsoluteFile());
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void equalsAndHashCodeForFileSource() {
		File file1 = new File("foo.txt");
		File file2 = new File("bar.txt");
		assertEqualsAndHashCode(new FileSource(file1), new FileSource(file1), new FileSource(file2));

		FilePosition position = new FilePosition(42, 23);
		assertEqualsAndHashCode(
				new FileSource(file1, position),
				new FileSource(file1, position),
				new FileSource(file2, position));
	}

	@Test
	void equalsAndHashCodeForDirectorySource() {
		File dir1 = new File(".");
		File dir2 = new File("..");
		assertEqualsAndHashCode(
				new DirectorySource(dir1), new DirectorySource(dir1), new DirectorySource(dir2));
	}
}
