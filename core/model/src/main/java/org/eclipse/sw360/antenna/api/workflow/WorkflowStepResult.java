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
package org.eclipse.sw360.antenna.api.workflow;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import java.util.*;
import java.util.stream.Collectors;

public class WorkflowStepResult {
    private final Set<Artifact> artifacts = new HashSet<>();
    /*
     * if the value artifactsShouldBeAppended is set to
     *  - false: the set artifacts here is treated as the complete set and it overwrites the set in the state
     *  - true: the set of artifacts here is added to the set of artifacts in the state
     */
    private final boolean artifactsShouldBeAppended;

    private final Map<String,IAttachable> attachableMap = new HashMap<>();

    private final List<String> additionalReportComments = new ArrayList<>();

    public WorkflowStepResult(Collection<Artifact> artifacts, boolean artifactsShouldBeAppended) {
        this.artifacts.addAll(artifacts);
        this.artifactsShouldBeAppended = artifactsShouldBeAppended;
    }

    public WorkflowStepResult(Collection<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
        artifactsShouldBeAppended = false;
    }

    public Set<Artifact> getArtifacts() {
        return artifacts;
    }

    public boolean attach(String key, IAttachable attachable){
        return attachableMap.put(key, attachable) != null;
    }

    public void attachAll(Map<String,IAttachable> attachables) {
        attachableMap.putAll(attachables);
    }

    public Map<String, IAttachable> getAttachables() {
        return attachableMap;
    }

    public List<String> getAdditionalReportComments() {
        return additionalReportComments;
    }

    public boolean addAdditionalReportComment(String comment) {
        return additionalReportComments.add(comment);
    }

    public boolean isArtifactsShouldBeAppended() {
        return artifactsShouldBeAppended;
    }

    public List<Artifact> getPotentialDuplicatesWith(WorkflowStepResult compareWsr) {
        return this.artifacts.stream()
                .flatMap(artifact ->
                    compareWsr.getArtifacts().stream()
                            .filter(artifact::isPotentialDuplicateOf)
                )
                .collect(Collectors.toList());
    }
}
