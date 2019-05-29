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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
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
import java.util.*;
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
    public void tearDown() {
        jarCreator.cleanUp();
        temporaryFolder.delete();

        verify(toolConfigMock).getAntennaTargetDirectory();
    }

    private List<Artifact> makeArtifacts(Path... paths) {
        List<Artifact> artifacts = new ArrayList<>();
        Arrays.stream(paths).forEach(
                path -> {
                    Artifact artifact = new Artifact();
                    artifact.addFact(new ArtifactPathnames(path.toString()));
                    artifact.addFact(new ArtifactFilename(path.getFileName().toString()));
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
        artifacts.get(1).addFact(new ArtifactSourceFile(jarWithSources.toPath()));

        resolver.process(artifacts);

        assertTrue(artifacts.get(0).askFor(ArtifactSourceFile.class).isPresent());
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
    }

    @Test
    public void testWithArtifactsWhichHaveSource() throws Exception {
        final Path jarWithManifest = jarCreator.createJarWithManifest();
        final Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        final Path jarInJarInJar = jarCreator.createJarInJarInJar();
        final Path jarInJar = jarCreator.createJarInJar();
        List<Artifact> artifacts = makeArtifacts(jarWithManifest, jarWithoutManifest, jarInJarInJar, jarInJar);

        File jarWithSources = jarCreator.createJarWithSource();
        artifacts.get(0).addFact(new ArtifactSourceFile(jarWithSources.toPath()));

        resolver.process(artifacts);

        assertEquals(4, artifacts.size());
        for (int i = 0; i < 4; i++) {
            final Optional<Path> resultSourceFile = artifacts.get(i).askForGet(ArtifactSourceFile.class);
            assertEquals(i == 0, resultSourceFile.isPresent());
            if(resultSourceFile.isPresent()) {
                assertEquals((i==0 ? jarWithSources : null), resultSourceFile.get().toFile());
            }
        }
    }

    @Test
    public void testWithMatchingArtifactsWhichHaveSource() throws Exception {
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
        artifacts.get(2).addFact(new ArtifactSourceFile(jarWithSources.toPath()));

        resolver.process(artifacts);

        assertEquals(5, artifacts.size());
        for (int i = 0; i < 5; i++) {
            final Optional<Path> sourceResult = artifacts.get(i).askForGet(ArtifactSourceFile.class);
            if(i == 2) {
                assertEquals(jarWithSources, artifacts.get(i).askForGet(ArtifactSourceFile.class).get().toFile());
            }else if(i == 3) {
                assertTrue(sourceResult.isPresent());
                assertNotEquals(jarWithInJarInJar.toFile(), sourceResult.get().toFile());
                assertTrue(sourceResult.get().toFile().exists());
                assertTrue(sourceResult.get().toString().startsWith(workspace.toString()));

                int count = 0;
                try (JarFile parentSourceJar = new JarFile(sourceResult.get().toFile())) {
                    final Enumeration<JarEntry> entries = parentSourceJar.entries();
                    while(entries.hasMoreElements()){
                        entries.nextElement();
                        count++;
                    }
                }
                assertEquals(4, count);
            }else{
                assertFalse(sourceResult.isPresent());
            }
        }
    }
}
