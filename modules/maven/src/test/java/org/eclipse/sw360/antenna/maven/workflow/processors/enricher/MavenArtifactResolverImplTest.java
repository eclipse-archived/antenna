/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven.workflow.processors.enricher;

import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.maven.*;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

public class MavenArtifactResolverImplTest extends AntennaTestWithMockedContext {
    @Mock
    private IArtifactRequester requester = mock(IArtifactRequester.class);

    private IProject project;
    private MavenArtifactResolverImpl mavenArtifactResolverImpl;
    private List<Artifact> artifacts;

    @Before
    public void setUp() throws IOException {
        project = mock(IProject.class);
        when(antennaContextMock.getProject()).thenReturn(project);
        when(antennaContextMock.getGeneric(RepositorySystem.class))
                .thenReturn(Optional.empty());
        MavenProject mavenProject = new MavenProject();
        when(antennaContextMock.getGeneric(MavenProject.class))
                .thenReturn(Optional.of(mavenProject));
        when(antennaContextMock.getGeneric(LegacySupport.class))
                .thenReturn(Optional.empty());

        when(configMock.getIgnoreForSourceResolving()).thenReturn(Collections.emptyList());
        when(project.getBasedir()).thenReturn(temporaryFolder.getRoot());
        Path targetDir = temporaryFolder.newFolder("target").toPath();
        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(targetDir);
        when(toolConfigMock.getDependenciesDirectory()).thenReturn(targetDir.resolve("dependencies"));

        mavenArtifactResolverImpl = new MavenArtifactResolverImpl(ProxySettings.noProxy(),
                antennaContextMock.getGeneric(RepositorySystem.class),
                antennaContextMock.getGeneric(MavenProject.class),
                antennaContextMock.getGeneric(LegacySupport.class),
                toolConfigMock.getDependenciesDirectory(),
                Collections.emptyList(), null,null, reporterMock, false,
                antennaContextMock.getProject().getBasedir());

        artifacts = Collections.singletonList(mkArtifact("groupId", "artifactId", "version"));
    }

    @Test
    public void testMavenArtifactResolverImplWithSuccess() throws IOException {
        MavenArtifactResolverImpl spiedMavenArtifactResolverImpl = spy(mavenArtifactResolverImpl);

        doReturn(requester)
                .when(spiedMavenArtifactResolverImpl).getArtifactRequester();
        doReturn(Optional.of(temporaryFolder.newFile("http-downloader-result.jar")))
                .when(requester).requestFile(any(Coordinate.class), any(Path.class), eq(ClassifierInformation.DEFAULT_JAR));
        doReturn((Optional.of(temporaryFolder.newFile("http-downloader-result-sources.jar"))))
                .when(requester).requestFile(any(Coordinate.class), any(Path.class), eq(ClassifierInformation.DEFAULT_SOURCE_JAR));

        Collection<Artifact> result = spiedMavenArtifactResolverImpl.process(artifacts);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.stream()
                .anyMatch(a -> a.askFor(ArtifactFile.class).isPresent())).isTrue();
        assertThat(result.stream()
                .anyMatch(a -> a.askFor(ArtifactSourceFile.class).isPresent())).isTrue();
    }

    @Test
    public void testMavenArtifactResolverImplWithUnresolvableArtifacts() {
        MavenArtifactResolverImpl spiedMavenArtifactResolverImpl = spy(mavenArtifactResolverImpl);

        doReturn(requester)
                .when(spiedMavenArtifactResolverImpl).getArtifactRequester();
        doReturn(Optional.empty())
                .when(requester).requestFile(any(Coordinate.class), any(Path.class), eq(ClassifierInformation.DEFAULT_JAR));
        doReturn((Optional.empty()))
                .when(requester).requestFile(any(Coordinate.class), any(Path.class), eq(ClassifierInformation.DEFAULT_SOURCE_JAR));

        Collection<Artifact> result = spiedMavenArtifactResolverImpl.process(artifacts);

        assertThat(result.stream()
                .anyMatch(a -> a.askFor(ArtifactSourceFile.class).isPresent())).isFalse();
        assertThat(result.stream()
                .anyMatch(a -> a.askFor(ArtifactFile.class).isPresent())).isFalse();
    }

    @Test
    public void testWithIncompatibleCoordinates() {
        Artifact artifact = new Artifact()
                .addFact(new ArtifactCoordinates(
                        new Coordinate(Coordinate.Types.NPM, "groupId", "artifactId", "version")));

        MavenArtifactResolverImpl spiedMavenArtifactResolverImpl = spy(mavenArtifactResolverImpl);

        doReturn(requester)
                .when(spiedMavenArtifactResolverImpl).getArtifactRequester();

        spiedMavenArtifactResolverImpl.process(Collections.singleton(artifact));

        verify(requester, never())
                .requestFile(any(Coordinate.class), any(Path.class), any(ClassifierInformation.class));
    }

    private Artifact mkArtifact(String groupId, String name, String version) {
        Artifact artifact = new Artifact();

        artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, groupId, name, version));
        artifact.addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        return artifact;
    }

    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(antennaContextMock, atLeast(0)).getProject();
        verify(antennaContextMock, atLeast(0)).getGeneric(RepositorySystem.class);
        verify(antennaContextMock, atLeast(0)).getGeneric(MavenProject.class);
        verify(antennaContextMock, atLeast(0)).getGeneric(LegacySupport.class);
        verify(toolConfigMock, atLeast(0)).getAntennaTargetDirectory();
        verify(toolConfigMock, atLeast(0)).getDependenciesDirectory();
        verify(toolConfigMock, atLeast(0)).useProxy();
        verify(toolConfigMock, atLeast(0)).getProxyHost();
        verify(toolConfigMock, atLeast(0)).getProxyPort();
        verify(toolConfigMock, atLeast(0)).isMavenInstalled();
        verify(configMock, atLeast(0)).getIgnoreForSourceResolving();
        verify(reporterMock, atLeast(0)).getProcessingReport();
        verify(project, atLeast(0)).getBasedir();
    }
}
