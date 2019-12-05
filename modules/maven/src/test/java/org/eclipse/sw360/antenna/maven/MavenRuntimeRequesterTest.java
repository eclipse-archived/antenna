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
package org.eclipse.sw360.antenna.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MavenRuntimeRequesterTest extends AntennaTestWithMockedContext {
    @Mock
    private RepositorySystem repositorySystem = mock(RepositorySystem.class);
    @Mock
    private ArtifactRepository localRepositoryMock = mock(ArtifactRepository.class);
    @Mock
    private ArtifactRepository remoteRepositoryMock = mock(ArtifactRepository.class);

    private ArtifactStubFactory stubFactory = new ArtifactStubFactory();

    private MavenRuntimeRequester mavenRuntimeRequester;

    private Coordinate coordinate = new Coordinate(Coordinate.Types.MAVEN, "groupId", "artifactId", "version");
    private org.apache.maven.artifact.Artifact stubbedMavenArtifact;

    @Before
    public void before() throws Exception {
        this.baseBefore();
        List<ArtifactRepository> repositories = Collections.singletonList(remoteRepositoryMock);
        mavenRuntimeRequester = new MavenRuntimeRequester(repositorySystem, localRepositoryMock, repositories, Optional.empty());

        stubbedMavenArtifact = stubFactory.createArtifact("groupId", "artifactId", "version");
    }

    @Test
    public void testRequestFileWithSuccess() throws IOException {
        File artifactFile = temporaryFolder.newFile("downloadedJar.jar");
        stubbedMavenArtifact.setFile(artifactFile);

        ArtifactResolutionResult result  = new ArtifactResolutionResult()
                .setOriginatingArtifact(stubbedMavenArtifact);

        doReturn(stubbedMavenArtifact)
                .when(repositorySystem)
                .createArtifact(anyString(), anyString(), anyString(), anyString());

        doReturn(result)
                .when(repositorySystem)
                .resolve(any());

        Path targetDirectory = temporaryFolder.newFolder("dependencies").toPath();
        Optional<File> file = mavenRuntimeRequester.requestFile(coordinate, targetDirectory, ClassifierInformation.DEFAULT_JAR);

        assertThat(file).isPresent();
        assertThat(Arrays.stream(targetDirectory.toFile().listFiles())
                .anyMatch(f -> f.equals(file.get()))).isTrue();
    }

    @Test
    public void testRequestFileWithExceptions() throws IOException {
        ArtifactResolutionResult result = new ArtifactResolutionResult()
                .setOriginatingArtifact(stubbedMavenArtifact)
                .addMissingArtifact(stubbedMavenArtifact);

        doReturn(stubbedMavenArtifact)
                .when(repositorySystem)
                .createArtifact(anyString(), anyString(), anyString(), anyString());

        doReturn(result)
                .when(repositorySystem)
                .resolve(any());

        Path targetDirectory = temporaryFolder.newFolder("dependencies").toPath();
        Optional<File> file = mavenRuntimeRequester.requestFile(coordinate, targetDirectory, ClassifierInformation.DEFAULT_JAR);

        assertThat(file).isEmpty();
    }
}
