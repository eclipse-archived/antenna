/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.validators.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.workflow.stubs.AbstractComplianceChecker;
import org.eclipse.sw360.antenna.workflow.stubs.DefaultPolicyEvaluation;

import java.util.Collection;
import java.util.Map;

/**
 * Checks if a list of artifact has maven or bundle coordinates.
 */
public class CoordinatesValidator extends AbstractComplianceChecker {

    private IEvaluationResult.Severity missingCoordinatesSeverity;

    public CoordinatesValidator() {
        this.workflowStepOrder = VALIDATOR_BASE_ORDER + 100;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
        DefaultPolicyEvaluation policyEvaluation = new DefaultPolicyEvaluation();
        artifacts.stream()
                .filter(artifact -> ! artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY))
                .filter(artifact -> artifact.askForAll(ArtifactCoordinates.class).size() == 0)
                .forEach(artifact -> policyEvaluation.addEvaluationResult("CoordinatesValidator::rule1", "Artifact has no Coordinates", missingCoordinatesSeverity, artifact));
        return policyEvaluation;
    }

    @Override
    public String getRulesetDescription() {
        return "Coordinates Validator";
    }

    @Override
    public void configure(Map<String, String> configMap) {
        super.configure(configMap);
        missingCoordinatesSeverity = getSeverityFromConfig("failOnMissingCoordinates", configMap, IEvaluationResult.Severity.WARN);
    }
}
