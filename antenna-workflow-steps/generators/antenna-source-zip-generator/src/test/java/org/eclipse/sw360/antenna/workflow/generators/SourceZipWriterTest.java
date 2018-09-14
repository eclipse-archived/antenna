/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SourceZipWriterTest extends AntennaTestWithMockedContext {

    private SourceZipWriter writer;
    private Artifact artifact;

    @Before
    public void setUp() {
        writer = new SourceZipWriter();
        artifact = new Artifact();
        when(antennaContextMock.getProcessingReporter())
                .thenReturn(null);
    }

    @Test
    public void testGetFileNameOrNullForEmptyArtifact() throws Exception {
        assertThat(writer.getFileNameOrNull(artifact), is(nullValue()));
    }

    @Test
    public void testGetFileNameOrNullWithArtifactIdentifier() throws Exception {
        ArtifactIdentifier identifier = new ArtifactIdentifier();
        identifier.setFilename("filename");
        artifact.setArtifactIdentifier(identifier);
        assertThat(writer.getFileNameOrNull(artifact), is("filename"));
    }

    @Test
    public void testGetFileNameOrNullWithEmptyArtifactIdentifier() throws Exception {
        ArtifactIdentifier identifier = new ArtifactIdentifier();
        artifact.setArtifactIdentifier(identifier);
        assertThat(writer.getFileNameOrNull(artifact), is(nullValue()));
    }

    @Test
    public void testGetFileNameOrNullWithSourceJar() throws Exception {
        artifact.setMavenSourceJar(new File("filename"));
        assertThat(writer.getFileNameOrNull(artifact), is("filename"));
    }

    @Test
    public void testGetFileNameOrNullWithP2SourceJar() throws Exception {
        artifact.setP2SourceJar(new File("filename"));
        assertThat(writer.getFileNameOrNull(artifact), is("filename"));
    }

}
