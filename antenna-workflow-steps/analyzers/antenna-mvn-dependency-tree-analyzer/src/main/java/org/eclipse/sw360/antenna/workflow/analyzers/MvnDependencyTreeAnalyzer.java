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

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.frontend.mojo.WrappedDependencyNodes;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MvnDependencyTreeAnalyzer extends AbstractAnalyzer {

    @Override
    public WorkflowStepResult yield() {
        Set<Artifact> antennaArtifacts = new HashSet<>();

        Optional<WrappedDependencyNodes> wrappedProjectsOptional = context.getGeneric(WrappedDependencyNodes.class);


        if (!wrappedProjectsOptional.isPresent()) {
            throw new AntennaExecutionException("No Maven dependency tree was provided for analysis.");
        }

        List<DependencyNode> projectList = wrappedProjectsOptional.get().getDependencyNodes();

        projectList.forEach(node -> antennaArtifacts.add(getArtifactFromNode(node)));

        return new WorkflowStepResult(antennaArtifacts);
    }

    private Artifact getArtifactFromNode(DependencyNode node) {
        Artifact antennaArtifact = new Artifact();

        String[] paths;
        if(node.getArtifact().getFile() != null) {
            paths = new String[]{node.getArtifact().getFile().getPath()};
        } else {
            paths = new String[0];
        }

        antennaArtifact.setArtifactIdentifier(getArtifactIdentifier(node));
        antennaArtifact.setPathnames(paths);
        antennaArtifact.setAnalysisSource(getName());
        antennaArtifact.setMatchState(MatchState.EXACT);
        return antennaArtifact;
    }

    private ArtifactIdentifier getArtifactIdentifier(DependencyNode node) {
        MavenCoordinates coordinates = new MavenCoordinates();
        coordinates.setArtifactId(node.getArtifact().getArtifactId());
        coordinates.setGroupId(node.getArtifact().getGroupId());
        coordinates.setVersion(node.getArtifact().getVersion());

        ArtifactIdentifier identifier = new ArtifactIdentifier();
        identifier.setMavenCoordinates(coordinates);
        if(node.getArtifact().getFile() != null) {
            identifier.setFilename(node.getArtifact().getFile().getPath());
        }

        return identifier;
    }

    @Override
    public String getName() {
        return "MVN DEPENDENCY TREE";
    }

}