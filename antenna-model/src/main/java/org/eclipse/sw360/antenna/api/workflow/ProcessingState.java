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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessingState {
    private List<Artifact> artifacts = new ArrayList<>();
    private final Map<String,IAttachable> attachableMap = new HashMap<>();
    private final List<String> additionalReportComments = new ArrayList<>();

    private final Logger LOGGER =  LoggerFactory.getLogger(ProcessingState.class);

    public ProcessingState(Collection<WorkflowStepResult> initialResults) {
        if(initialResults == null) {
            return;
        }
        if(initialResults.stream()
                .anyMatch(ir -> ! ir.isArtifactsShouldBeAppended())) {
            LOGGER.warn("Some of the initial WorkflowStepResults are overwriting. This is unexpected and will be ignored.");
        }
        initialResults.forEach(wsr -> applyWorkflowStepResult(wsr, true));
    }

    public Set<Artifact> getArtifacts() {
        return new HashSet<>(artifacts);
    }

    public Map<String, IAttachable> getAttachables() {
        return attachableMap;
    }

    public List<String> getAdditionalReportComments() {
        return additionalReportComments;
    }

    public void applyWorkflowStepResult(WorkflowStepResult workflowStepResult) {
        applyWorkflowStepResult(workflowStepResult, false);
    }

    public void applyWorkflowStepResult(WorkflowStepResult workflowStepResult, boolean forceAppend) {
        if(forceAppend || workflowStepResult.isArtifactsShouldBeAppended()){
            artifacts.addAll(workflowStepResult.getArtifacts());
        } else {
            artifacts = new ArrayList<>(workflowStepResult.getArtifacts());
        }
        attachableMap.putAll(workflowStepResult.getAttachables());
        additionalReportComments.addAll(workflowStepResult.getAdditionalReportComments());
    }
}
