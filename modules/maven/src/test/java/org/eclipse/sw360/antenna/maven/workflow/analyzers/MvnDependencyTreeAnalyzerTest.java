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
package org.eclipse.sw360.antenna.maven.workflow.analyzers;

import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.frontend.mojo.WrappedDependencyNodes;
import org.eclipse.sw360.antenna.maven.workflow.analyzers.MvnDependencyTreeAnalyzer;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MvnDependencyTreeAnalyzerTest {

    @Mock
    private AntennaContext antennaContextMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testProjectIsAnalyzedCorrectly() {
        MvnDependencyTreeAnalyzer analyzer = new MvnDependencyTreeAnalyzer();

        analyzer.setAntennaContext(antennaContextMock);

        when(antennaContextMock.getGeneric(WrappedDependencyNodes.class))
                .thenReturn(Optional.of(getNodes(1)));

        WorkflowStepResult result = analyzer.yield();

        assertThat(result.getArtifacts()).hasSize(1);

        Artifact actualArtifact = result.getArtifacts().iterator().next();
        Artifact expectedArtifact = getExpectedArtifact(0);

        assertThat(actualArtifact.getAnalysisSource()).isEqualTo(expectedArtifact.getAnalysisSource());
        assertThat(actualArtifact.askFor(ArtifactMatchingMetadata.class).get().getMatchState())
                .isEqualTo(expectedArtifact.askFor(ArtifactMatchingMetadata.class).get().getMatchState());
        assertThat(actualArtifact.getArtifactIdentifiers())
                .isEqualTo(expectedArtifact.getArtifactIdentifiers());
    }

    @Test
    public void testResultSize() {
        int size = 3;

        MvnDependencyTreeAnalyzer analyzer = new MvnDependencyTreeAnalyzer();
        analyzer.setAntennaContext(antennaContextMock);

        when(antennaContextMock.getGeneric(WrappedDependencyNodes.class))
                .thenReturn(Optional.of(getNodes(size)));

        WorkflowStepResult result = analyzer.yield();

        assertThat(result.getArtifacts()).hasSize(size);
    }

    private WrappedDependencyNodes getNodes(int count) {
        List<DependencyNode> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            projects.add(getNode(i));
        }
        return new WrappedDependencyNodes(projects);
    }

    private File getFilePathForI(Integer i) {
        return new File("path", i.toString());
    }

    private DependencyNode getNode(int i) {
        ArtifactStub artifact = new ArtifactStub();
        artifact.setFile(getFilePathForI(i));
        artifact.setGroupId("org.eclipse.sw360.antenna");
        artifact.setArtifactId("artifact-" + i);
        artifact.setVersion("1.0." + i);

        return new DefaultDependencyNode(null, artifact, null, null, null);
    }

    private Artifact getExpectedArtifact(int i) {
        Artifact artifact = new Artifact(new MvnDependencyTreeAnalyzer().getName());

        MavenCoordinates.MavenCoordinatesBuilder coordinatesBuilder = new MavenCoordinates.MavenCoordinatesBuilder();
        coordinatesBuilder.setGroupId("org.eclipse.sw360.antenna");
        coordinatesBuilder.setArtifactId("artifact-" + i);
        coordinatesBuilder.setVersion("1.0." + i);
        final MavenCoordinates coordinates = coordinatesBuilder.build();
        artifact.addFact(coordinates);

        String path = getFilePathForI(i).toString();
        final ArtifactFile artifactFile = new ArtifactFile(Paths.get(path));
        artifact.addFact(artifactFile);

        String[] pathNames = {path};
        artifact.addFact(new ArtifactPathnames(pathNames));
        artifact.addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        return artifact;
    }
}