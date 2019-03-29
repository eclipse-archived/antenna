/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.outputHandlers;

import org.apache.commons.io.IOUtils;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class FileToArchiveWriterTest extends AntennaTestWithMockedContext {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private List<String> originalContentOfZip = Arrays.asList("firstFile.txt","secondFile.txt","inner/thirdFile.txt");
    private File archiveToAddFileIn;
    private File fileToAddIntoArchive;
    private String contentOfInnerFile = "content of inner file";

    private Path innerPath;
    private FileToArchiveWriter fileToArchiveWriter;

    @Parameterized.Parameters(name = "{index}: InnerPath = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"file.pdf"},
                {"some/inner/path/to/file.pdf"}
        });
    }

    public FileToArchiveWriterTest(String innerPath){
        this.innerPath = Paths.get(innerPath);
    }

    @Before
    public void before() throws Exception {
        archiveToAddFileIn = temporaryFolder.newFile("archiveToAddFileIn.jar");
        try(FileOutputStream fileOutputStream = new FileOutputStream(archiveToAddFileIn);
            ZipOutputStream zos = new ZipOutputStream(fileOutputStream)){
            for (String f: originalContentOfZip) {
                zos.putNextEntry(new ZipEntry(f));
            }
        }

        fileToAddIntoArchive = temporaryFolder.newFile("fileToAddIntoArchive.txt");
        try(PrintWriter out = new PrintWriter(fileToAddIntoArchive)){
            out.print(contentOfInnerFile);
        }

        fileToArchiveWriter = new FileToArchiveWriter();
        fileToArchiveWriter.setAntennaContext(antennaContextMock);
    }

    private List<String> listContentsOfZip(File zipFile) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            return zip.stream()
                    .map(ZipEntry::getName)
                    .collect(Collectors.toList());
        }
    }

    private String getContentOfEntryInZip(File zipFile, String entryName) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            return zip.stream()
                    .filter(e -> Paths.get(entryName).compareTo(Paths.get(e.getName())) == 0)
                    .map(entry -> {
                        try {
                            return zip.getInputStream(entry);
                        } catch (IOException exeption) {
                            throw new RuntimeException(exeption);
                        }
                    })
                    .findAny()
                    .map(in -> {
                        try {
                            return IOUtils.toString(in, Charset.defaultCharset());
                        } catch (IOException exeption) {
                            throw new RuntimeException(exeption);
                        }
                    })
                    .orElse("<none>");
        }
    }

    @Test
    public void testAddFileToArchive() throws Exception {
        fileToArchiveWriter.addFileToArchive(fileToAddIntoArchive.toPath(), archiveToAddFileIn.toPath(), innerPath);
        List<String> filesInZip = listContentsOfZip(archiveToAddFileIn);
        assertThat(filesInZip.stream().map(Paths::get)).contains(innerPath);
        originalContentOfZip.forEach(
                f -> assertThat(filesInZip).contains(f));

        String contentOfAddedFile = getContentOfEntryInZip(archiveToAddFileIn, innerPath.toString());
        assertThat(contentOfAddedFile).isEqualTo(contentOfInnerFile);

    }
}