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
package org.eclipse.sw360.antenna.workflow.processors.enricher;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.testing.util.JarCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChildJarResolverTest extends AntennaTestWithMockedContext {


    private JarCreator jarCreator;
    private ChildJarResolver resolver;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private Path workspace;

    @Before
    public void setUp() throws Exception {
        workspace = temporaryFolder.newFolder("workspace").toPath();

        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(workspace);

        jarCreator = new JarCreator();
        resolver = new ChildJarResolver();
        resolver.setAntennaContext(antennaContextMock);
        resolver.configure();
    }

    @After
    public void tearDown() throws Exception {
        jarCreator.cleanUp();
        temporaryFolder.delete();

        verify(toolConfigMock).getAntennaTargetDirectory();
    }

    private List<Artifact> makeArtifacts(Path... paths) {
        List<Artifact> artifacts = new ArrayList<>();
        Arrays.stream(paths).forEach(
                path -> {
                    Artifact artifact = new Artifact();
                    artifact.setPathnames(new String[] { path.toString() });
                    artifact.getArtifactIdentifier()
                            .setFilename(path.getFileName().toString());
                    artifacts.add(artifact);
                });
        return artifacts;
    }

    @Test
    public void resolveChildSource() throws Exception {
        Path artifactPath = Paths.get("JarWithJar.jar/Jar.jar").toAbsolutePath();
        Path parentArtifactPath = Paths.get("JarWithJar.jar").toAbsolutePath();
        List<Artifact> artifacts = makeArtifacts(artifactPath, parentArtifactPath);

        File jarWithSources = jarCreator.createJarWithSource();
        artifacts.get(1).setMavenSourceJar(jarWithSources);

        resolver.process(artifacts);

        assertNotNull(artifacts.get(0).getMvnSourceJar());
    }


    @Test
    public void testWithDummyArtifacts() throws Exception {
        final Path jarWithManifest = jarCreator.createJarWithManifest();
        final Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        final Path jarInJarInJar = jarCreator.createJarInJarInJar();
        final Path jarInJar = jarCreator.createJarInJar();
        List<Artifact> artifacts = makeArtifacts(jarWithManifest, jarWithoutManifest, jarInJarInJar, jarInJar);

        resolver.process(artifacts);

        assertEquals(4, artifacts.size());
        assertEquals(makeArtifacts(jarWithManifest, jarWithoutManifest, jarInJarInJar, jarInJar), artifacts);
    }

    @Test
    public void testWithArtifactsWhichHaveSource() throws Exception {
        final Path jarWithManifest = jarCreator.createJarWithManifest();
        final Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        final Path jarInJarInJar = jarCreator.createJarInJarInJar();
        final Path jarInJar = jarCreator.createJarInJar();
        List<Artifact> artifacts = makeArtifacts(jarWithManifest, jarWithoutManifest, jarInJarInJar, jarInJar);

        File jarWithSources = jarCreator.createJarWithSource();
        artifacts.get(0).setMavenSourceJar(jarWithSources);

        resolver.process(artifacts);

        assertEquals(4, artifacts.size());
        for (int i = 0; i < 4; i++) {
            assertEquals((i==0 ? jarWithSources : null), artifacts.get(i).getMvnSourceJar());
            assertNull(artifacts.get(i).getP2SourceJar());
        }
    }

    @Test
    public void testWithMatchingMavenArtifactsWhichHaveSource() throws Exception {
        final Path jarWithManifest = jarCreator.createJarWithManifest();
        final Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        final Path jarWithInJarInJar = jarCreator.createJarInJarInJar();
        Path jarInJarInJar = jarWithInJarInJar
                .resolve(JarCreator.jarInJarName)
                .resolve(JarCreator.jarWithManifestName);
        final Path jarInJar = jarCreator.createJarInJar();
        List<Artifact> artifacts = makeArtifacts(
                jarWithManifest,    //0
                jarWithoutManifest, //1
                jarWithInJarInJar,  //2
                jarInJarInJar,      //3
                jarInJar);          //4

        File jarWithSources = jarCreator.createJarWithSource();
        artifacts.get(2).setMavenSourceJar(jarWithSources);

        resolver.process(artifacts);

        assertEquals(5, artifacts.size());
        for (int i = 0; i < 5; i++) {
            if(i == 2) {
                assertEquals(jarWithSources, artifacts.get(i).getMvnSourceJar());
            }else if(i == 3) {
                assertNotNull(artifacts.get(i).getMvnSourceJar());
                assertNotEquals(jarWithInJarInJar.toFile(), artifacts.get(i).getMvnSourceJar());
                assertTrue(artifacts.get(i).getMvnSourceJar().exists());
                assertTrue(artifacts.get(i).getMvnSourceJar().toString().startsWith(workspace.toString()));

                int count = 0;
                try (JarFile parentSourceJar = new JarFile(artifacts.get(i).getMvnSourceJar())) {
                    final Enumeration<JarEntry> entries = parentSourceJar.entries();
                    while(entries.hasMoreElements()){
                        entries.nextElement();
                        count++;
                    }
                }
                assertEquals(4, count);
            }else{
                assertNull(artifacts.get(i).getMvnSourceJar());
            }
            assertNull(artifacts.get(i).getP2SourceJar());
        }
    }

    @Test
    public void testWithMatchingP2ArtifactsWhichHaveSource() throws Exception {
        final Path jarWithManifest = jarCreator.createJarWithManifest();
        final Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        final Path jarWithInJarInJar = jarCreator.createJarInJarInJar();
        Path jarInJarInJar = jarWithInJarInJar
                .resolve(JarCreator.jarInJarName)
                .resolve(JarCreator.jarWithManifestName);
        final Path jarInJar = jarCreator.createJarInJar();
        List<Artifact> artifacts = makeArtifacts(
                jarWithManifest,    //0
                jarWithoutManifest, //1
                jarWithInJarInJar,  //2
                jarInJarInJar,      //3
                jarInJar);          //4

        File jarWithSources = jarCreator.createJarWithSource();
        artifacts.get(2).setP2SourceJar(jarWithSources);

        resolver.process(artifacts);

        assertEquals(5, artifacts.size());
        for (int i = 0; i < 5; i++) {
            if(i == 2) {
                assertEquals(jarWithSources, artifacts.get(i).getP2SourceJar());
            }else if(i == 3) {
                assertNotNull(artifacts.get(i).getP2SourceJar());
                assertNotEquals(jarWithInJarInJar.toFile(), artifacts.get(i).getP2SourceJar());
                assertTrue(artifacts.get(i).getP2SourceJar().exists());
                assertTrue(artifacts.get(i).getP2SourceJar().toString().startsWith(workspace.toString()));

                int count = 0;
                try (JarFile parentSourceJar = new JarFile(artifacts.get(i).getP2SourceJar())) {
                    final Enumeration<JarEntry> entries = parentSourceJar.entries();
                    while(entries.hasMoreElements()){
                        entries.nextElement();
                        count++;
                    }
                }
                assertEquals(4, count);
            }else{
                assertNull(artifacts.get(i).getP2SourceJar());
            }
            assertNull(artifacts.get(i).getMvnSourceJar());
        }
    }
}
