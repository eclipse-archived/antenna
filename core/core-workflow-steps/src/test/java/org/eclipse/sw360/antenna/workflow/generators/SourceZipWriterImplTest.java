/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static org.mockito.Mockito.*;

public class SourceZipWriterImplTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String PATH_TO_EMPTY_JAR = "SourceZipWriterTest/empty.txt";
    /* A bug with duplicate entries was discovered with this exemplary dependency; see Issue #601.
       Thus, this dependency is used to test that case.
     */
    private static final String PATH_TO_JAR_WITH_DUPLICATE_ENTRIES = "SourceZipWriterTest/c3p0-0.9.5.3-sources.jar";
    private static final String DUPLICATE_ENTRY = "c3p0-0.9.5.3-sources/com/mchange/v2/c3p0/subst/C3P0Substitutions.java";

    private final IArtifactFilter artifactFilterMock = mock(IArtifactFilter.class);
    private final IProcessingReporter processingReporterMock = mock(IProcessingReporter.class);

    @Before
    public void setUp() {
        when(artifactFilterMock.passed(any())).thenReturn(true);
    }

    @Test
    public void testEmptyZipsAreCaughtAndReported() throws URISyntaxException {
        executeSourceZipWriter(PATH_TO_EMPTY_JAR);

        verify(processingReporterMock, times(1)).add(any(Artifact.class),
                eq(MessageType.PROCESSING_FAILURE),
                contains("Archive is not a ZIP archive"));
    }

    @Test
    public void testZipWriterProcessesDuplicateEntriesInJars() throws URISyntaxException, IOException {
        final File zipFile = executeSourceZipWriter(PATH_TO_JAR_WITH_DUPLICATE_ENTRIES);

        verify(processingReporterMock, never()).add(any(Artifact.class),
                eq(MessageType.PROCESSING_FAILURE),
                contains("duplicate entry"));

        assertThatZipFileContainsDuplicateEntries(zipFile, DUPLICATE_ENTRY);
    }

    private File executeSourceZipWriter(final String pathToJar) throws URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource(pathToJar);
        final File file = Paths.get(resource.toURI()).toFile();
        final String pathToJarFile = file.getAbsolutePath();

        final Path pathToSourcesZip = temporaryFolder.getRoot().toPath().resolve("sources.zip");

        final SourceZipWriterImpl sourceZipWriter = new SourceZipWriterImpl(pathToSourcesZip,
                artifactFilterMock,
                artifactFilterMock,
                processingReporterMock);

        final Artifact artifact = new Artifact();
        final ArtifactSourceFile sourceFile = new ArtifactSourceFile(Paths.get(pathToJarFile));
        artifact.addFact(sourceFile);
        final List<Artifact> artifacts = Arrays.asList(artifact);

        return sourceZipWriter.createZip(artifacts);
    }

    private void assertThatZipFileContainsDuplicateEntries(final File file, final String duplicateName) throws IOException {
        final ZipFile zipFile = new ZipFile(file);
        final Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

        int count = 0;
        while(entries.hasMoreElements()) {
            final ZipArchiveEntry archiveEntry = entries.nextElement();

            if(archiveEntry.getName().equals(duplicateName)) {
                count++;
            }
        }

        Assert.assertTrue(count >= 2);
    }
}
