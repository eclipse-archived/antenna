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

package org.eclipse.sw360.antenna.workflow.processors;

import java.util.*;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.workflow.processors.checkers.AbstractComplianceChecker;
import org.eclipse.sw360.antenna.workflow.processors.checkers.DefaultPolicyEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

/**
 * Validates the MatchStates of an artifacts list. A MatchState is not valid if
 * the artifact does not pass the defined MatchStateArtifactFilter.
 */
public class MatchStateValidator extends AbstractComplianceChecker {
    private final String SIMILAR_Severity_Key = "severityOfSIMILAR";
    private final String UNKNOWN_Severity_Key = "severityOfUNKNOWN";

    private IEvaluationResult.Severity SIMILAR_Severity = IEvaluationResult.Severity.WARN;
    private IEvaluationResult.Severity UNKNOWN_Severity = IEvaluationResult.Severity.WARN;

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
        DefaultPolicyEvaluation policyEvaluation = new DefaultPolicyEvaluation();
        artifacts.stream()
                .filter(artifact -> !artifact.isProprietary())
                .forEach(artifact -> {
                    MatchState artifactsMatchState = artifact.getMatchState();
                    if (MatchState.SIMILAR.equals(artifactsMatchState)) {
                        policyEvaluation.addEvaluationResult("MatchStateValidator::rule", "The match State is SIMILAR", SIMILAR_Severity, artifact);
                    }else if (MatchState.UNKNOWN.equals(artifactsMatchState)) {
                        policyEvaluation.addEvaluationResult("MatchStateValidator::rule", "The match State is UNKNOWN", UNKNOWN_Severity, artifact);
                    }
                });
        return policyEvaluation;
    }

    @Override
    public String getRulesetDescription() {
        return "Match State Validator";
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);

        SIMILAR_Severity = getSeverityFromConfig(SIMILAR_Severity_Key, configMap, SIMILAR_Severity);
        UNKNOWN_Severity = getSeverityFromConfig(UNKNOWN_Severity_Key, configMap, UNKNOWN_Severity);
    }
}
