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

import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.frontend.stub.mojo.WrappedDependencyNodes;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MvnDependencyTreeAnalyzer extends AbstractAnalyzer {

    public MvnDependencyTreeAnalyzer() {
        this.workflowStepOrder = 400;
    }

    @Override
    public WorkflowStepResult yield() {

        Optional<WrappedDependencyNodes> wrappedProjectsOptional = context.getGeneric(WrappedDependencyNodes.class);

        if (!wrappedProjectsOptional.isPresent()) {
            throw new ExecutionException("No Maven dependency tree was provided for analysis.");
        }

        List<DependencyNode> projectList = wrappedProjectsOptional.get().getDependencyNodes();

        final List<Artifact> collect = projectList.stream()
                .map(this::getArtifactFromNode)
                .collect(Collectors.toList());

        return new WorkflowStepResult(collect);
    }

    private Artifact getArtifactFromNode(DependencyNode node) {
        Artifact antennaArtifact = new Artifact(getName());

        String[] paths;
        if(node.getArtifact().getFile() != null) {
            paths = new String[]{node.getArtifact().getFile().getPath()};
        } else {
            paths = new String[0];
        }

        antennaArtifact.addCoordinate(getMavenCoordinate(node));
        if(node.getArtifact().getFile() != null) {
            antennaArtifact.addFact(new ArtifactFile(node.getArtifact().getFile().toPath()));
        }
        antennaArtifact.addFact(new ArtifactPathnames(paths));
        antennaArtifact.addFact(new ArtifactMatchingMetadata(MatchState.EXACT));
        return antennaArtifact;
    }

    private Coordinate getMavenCoordinate(DependencyNode node) {
        return new Coordinate(
                Coordinate.Types.MAVEN,
                node.getArtifact().getGroupId(),
                node.getArtifact().getArtifactId(),
                node.getArtifact().getVersion());
    }

    @Override
    public String getName() {
        return "Maven dependency anaylzer";
    }

}