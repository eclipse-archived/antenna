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
package org.eclipse.sw360.antenna.workflow.analyzers;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.frontend.mojo.WrappedDependencyNodes;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

public class MvnDependencyTreeAnalyzerTest {

    @Mock
    private AntennaContext antennaContextMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testProjectIsAnalyzedCorrectly() throws MalformedURLException {
        MvnDependencyTreeAnalyzer analyzer = new MvnDependencyTreeAnalyzer();

        analyzer.setAntennaContext(antennaContextMock);

        when(antennaContextMock.getGeneric(WrappedDependencyNodes.class))
                .thenReturn(Optional.of(getNodes(1)));

        WorkflowStepResult result = analyzer.yield();

        assertThat(result.getArtifacts().size(), is(equalTo(1)));

        Artifact actualArtifact = result.getArtifacts().iterator().next();
        Artifact expectedArtifact = getExpectedArtifact(0);

        assertThat(actualArtifact.getAnalysisSource(),
                is(equalTo(expectedArtifact.getAnalysisSource())));
        assertThat(actualArtifact.getMatchState(),
                is(equalTo(expectedArtifact.getMatchState())));
        assertThat(actualArtifact.getArtifactIdentifier(),
                is(equalTo(expectedArtifact.getArtifactIdentifier())));
    }

    @Test
    public void testResultSize() {
        int size = 3;

        MvnDependencyTreeAnalyzer analyzer = new MvnDependencyTreeAnalyzer();
        analyzer.setAntennaContext(antennaContextMock);

        when(antennaContextMock.getGeneric(WrappedDependencyNodes.class))
                .thenReturn(Optional.of(getNodes(size)));

        WorkflowStepResult result = analyzer.yield();

        assertThat(result.getArtifacts().size(), is(equalTo(size)));
    }

    private WrappedDependencyNodes getNodes(int count) {
        List<DependencyNode> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            projects.add(getNode(i));
        }
        return new WrappedDependencyNodes(projects);
    }

    private DependencyNode getNode(int i) {
        ArtifactStub artifact = new ArtifactStub();
        artifact.setFile(new File("path/" + i));
        artifact.setGroupId("org.eclipse.sw360.antenna");
        artifact.setArtifactId("artifact-" + i);
        artifact.setVersion("1.0." + i);

        DependencyNode dependencyNode = new DefaultDependencyNode(null, artifact, null, null, null);
        return dependencyNode;
    }

    private Artifact getExpectedArtifact(int i) {
        Artifact artifact = new Artifact();
        MavenCoordinates coordinates = new MavenCoordinates();
        coordinates.setGroupId("org.eclipse.sw360.antenna");
        coordinates.setArtifactId("artifact-" + i);
        coordinates.setVersion("1.0." + i);

        String path = "path/" + i;
        ArtifactIdentifier identifier = new ArtifactIdentifier();
        identifier.setMavenCoordinates(coordinates);
        identifier.setFilename(new File(path).getPath());

        String[] pathNames = {path};
        artifact.setArtifactIdentifier(identifier);
        artifact.setPathnames(pathNames);
        artifact.setAnalysisSource(new MvnDependencyTreeAnalyzer().getName());
        artifact.setMatchState(MatchState.EXACT);

        return artifact;
    }
}