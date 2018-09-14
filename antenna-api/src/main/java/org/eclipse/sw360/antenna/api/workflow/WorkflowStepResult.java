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
import org.eclipse.sw360.antenna.model.Artifact;

import java.util.*;

public class WorkflowStepResult {

    private final Set<Artifact> artifacts = new HashSet();
    private final Map<String,IAttachable> attachableMap = new HashMap<>();
    private final List<String> additionalReportComments = new ArrayList<>();

    public WorkflowStepResult(Collection<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
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

    public static WorkflowStepResult merge(Collection<WorkflowStepResult> workflowStepResults) {
        return workflowStepResults.stream()
                .reduce(WorkflowStepResult::merge)
                .orElse(new WorkflowStepResult(Collections.emptyList()));
    }

    private WorkflowStepResult merge(WorkflowStepResult workflowStepResult) {
        artifacts.addAll(workflowStepResult.artifacts);
        attachableMap.putAll(workflowStepResult.attachableMap);
        return this;
    }

    public WorkflowStepResult mergeWithKeepingArtifacts(WorkflowStepResult workflowStepResult) {
        attachableMap.putAll(workflowStepResult.attachableMap);
        additionalReportComments.addAll(0, workflowStepResult.additionalReportComments);
        return this;
    }
}
