/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.analyzers;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.reporting.MessageType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class resolves the configuration, specified in the configuration file in
 * the artifacts list. - remove artifact from list - add artifact to list -
 * change attribute of artifact if specified in config.xml
 */
public class ConfigurationAnalyzer extends AbstractAnalyzer {

    public ConfigurationAnalyzer() {
        this.workflowStepOrder = 100;
    }

    private List<Artifact> getConfiguredArtifacts(Configuration configuration, IProcessingReporter reporter) {
        return configuration.getAddArtifact()
                .stream()
                .peek(artifact -> reporter.add(artifact, MessageType.ADD_ARTIFACT,
                        "Artifact was added to artifacts list."))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "Antenna Configuration";
    }

    @Override
    public WorkflowStepResult yield() {
        return new WorkflowStepResult(getConfiguredArtifacts(context.getConfiguration(), reporter), true);
    }
}
