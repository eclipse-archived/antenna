/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipExtractorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testZipExtractor() throws IOException {
        File zipFileFolder = temporaryFolder.newFolder();
        StringBuilder sb = new StringBuilder();
        sb.append("Test String");

        File zipFile = zipFileFolder.toPath().resolve("zipFile.zip").toFile();

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream out = new ZipOutputStream(fos)) {
            ZipEntry e = new ZipEntry("testText.txt");
            out.putNextEntry(e);

            byte[] data = sb.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
        }

        File unzipFolder = temporaryFolder.newFolder();

        ZipExtractor.extractAll(zipFile, unzipFolder);

        assertThat(unzipFolder.list()).contains("testText.txt");
        assertThat(Files.readAllLines(Paths.get(unzipFolder.toString(), "testText.txt"))).containsExactly("Test String");
    }

}
