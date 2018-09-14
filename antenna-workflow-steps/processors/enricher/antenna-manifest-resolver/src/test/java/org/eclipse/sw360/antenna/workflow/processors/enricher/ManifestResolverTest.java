/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors.enricher;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.testing.util.JarCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;

public class ManifestResolverTest extends AntennaTestWithMockedContext {

    private JarCreator jarCreator;
    private ManifestResolver resolver;

    @Before
    public void setUp() throws Exception {
        this.jarCreator = new JarCreator();
        resolver = new ManifestResolver();
        resolver.setAntennaContext(antennaContextMock);
        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(temporaryFolder.newFolder("target").toPath());
    }

    @After
    public void tearDown() {
        this.jarCreator.cleanUp();
        verify(toolConfigMock).getAntennaTargetDirectory();
    }

    private List<Artifact> makeArtifacts(Path path) {
        List<Artifact> artifacts = new ArrayList<>();
        Artifact artifact = new Artifact();
        artifact.setPathnames(new String[] { path.toString() });
        artifact.getArtifactIdentifier()
                .setFilename(path.getFileName().toString());
        artifacts.add(artifact);
        return artifacts;
    }

    private void assertManifestMetadata(Artifact artifact) {
        assertThat(artifact.getArtifactIdentifier().getBundleCoordinates().getSymbolicName())
                .isEqualTo(JarCreator.testManifestSymbolicName);
        assertThat(artifact.getArtifactIdentifier().getBundleCoordinates().getBundleVersion())
                .isEqualTo(JarCreator.testManifestVersion);
    }

    @Test
    public void resolveArtifactsTest() throws IOException {
        Path jarWithManifest = jarCreator.createJarWithManifest();
        List<Artifact> artifacts = makeArtifacts(jarWithManifest);

        resolver.process(artifacts);

        assertManifestMetadata(artifacts.get(0));
    }

    @Test
    public void testWithoutManifest() throws IOException {
        Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        List<Artifact> artifacts = makeArtifacts(jarWithoutManifest);

        resolver.process(artifacts);

        assertThat(artifacts.get(0).getArtifactIdentifier().getBundleCoordinates().getSymbolicName()).isEqualTo(null);
        assertThat(artifacts.get(0).getArtifactIdentifier().getBundleCoordinates().getBundleVersion()).isEqualTo(null);
    }

    @Test
    public void testJarInJar() throws IOException {
        Path jarInJarInJar = jarCreator.createJarInJar()
                .resolve(JarCreator.jarWithManifestName);
        List<Artifact> artifacts = makeArtifacts(jarInJarInJar);

        resolver.process(artifacts);

        assertThat(artifacts.get(0).getJar()).isNotNull();
        assertThat(artifacts.get(0).getJar().getName()).isEqualTo(JarCreator.jarWithManifestName);
        assertManifestMetadata(artifacts.get(0));
    }

    @Test
    public void testJarInJarInJar() throws IOException {
        Path jarInJarInJar = jarCreator.createJarInJarInJar()
                .resolve(JarCreator.jarInJarName)
                .resolve(JarCreator.jarWithManifestName);
        List<Artifact> artifacts = makeArtifacts(jarInJarInJar);

        resolver.process(artifacts);

        assertThat(artifacts.get(0).getJar()).isNotNull();
        assertThat(artifacts.get(0).getJar().getName()).isEqualTo(JarCreator.jarWithManifestName);
        assertManifestMetadata(artifacts.get(0));
    }
}
