/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven.workflow.processors;

import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.maven.workflow.processors.enricher.MavenArtifactResolver;
import org.eclipse.sw360.antenna.maven.workflow.processors.enricher.MavenArtifactResolverImpl;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

public class MavenArtifactResolverTest extends AntennaTestWithMockedContext {
    private IProject project;
    private MavenArtifactResolver mavenArtifactResolver;
    private List<Artifact> artifacts;

    @Before
    public void setUp() throws IOException {
        project = Mockito.mock(IProject.class);
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

        mavenArtifactResolver = new MavenArtifactResolver();
        mavenArtifactResolver.setAntennaContext(antennaContextMock);

        artifacts = Collections.singletonList(mkArtifact("org.apache.commons", "commons-csv", "1.7"));
    }

    @Test
    public void mavenArtifactResolverForGoodArtifact() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("sourceRepositoryUrl", "");
        configMap.put("preferredSourceQualifier", "sources-ext");
        mavenArtifactResolver.configure(configMap);

        Collection<Artifact> processedArtifacts = mavenArtifactResolver.process(artifacts);

        assertThat(processedArtifacts.size()).isEqualTo(1);
        assertThat(antennaContextMock.getToolConfiguration().getDependenciesDirectory().resolve("commons-csv-1.7.jar")).exists();
    }

    @Test
    public void mavenArtifactResolverForGoodArtifactDirectWithImpl() throws MalformedURLException {
        ProxySettings proxySettings = new ProxySettings(false, null, 0);

        MavenArtifactResolverImpl mavenArtifactResolverImpl = new MavenArtifactResolverImpl(proxySettings, antennaContextMock.getGeneric(RepositorySystem.class),
                antennaContextMock.getGeneric(MavenProject.class), antennaContextMock.getGeneric(LegacySupport.class),
                toolConfigMock.getDependenciesDirectory(), Collections.emptyList(), null,
                null, reporterMock, false, antennaContextMock.getProject().getBasedir());

        Collection<Artifact> processedArtifacts = mavenArtifactResolverImpl.process(artifacts);

        assertThat(processedArtifacts.size()).isEqualTo(1);
        assertThat(antennaContextMock.getToolConfiguration().getDependenciesDirectory().resolve("commons-csv-1.7.jar")).exists();
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
