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

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.workflow.processors.checkers.AbstractComplianceChecker;
import org.eclipse.sw360.antenna.workflow.processors.checkers.DefaultPolicyEvaluation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Validates the MatchStates of an artifacts list. A MatchState is not valid if
 * the artifact does not pass the defined MatchStateArtifactFilter.
 */
public class MatchStateValidator extends AbstractComplianceChecker {
    private static final String SIMILAR_SEVERITY_KEY = "severityOfSIMILAR";
    private static final String UNKNOWN_SEVERITY_KEY = "severityOfUNKNOWN";

    private IEvaluationResult.Severity SIMILAR_Severity = IEvaluationResult.Severity.WARN;
    private IEvaluationResult.Severity UNKNOWN_Severity = IEvaluationResult.Severity.WARN;

    public MatchStateValidator() {
        this.workflowStepOrder = 10300;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
        DefaultPolicyEvaluation policyEvaluation = new DefaultPolicyEvaluation();
        artifacts.stream()
                .filter(artifact -> !artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY))
                .forEach(artifact -> {
                    final Optional<ArtifactMatchingMetadata> artifactMatchingMetadata = artifact.askFor(ArtifactMatchingMetadata.class);
                    if(! artifactMatchingMetadata.isPresent()) {
                        return;
                    }
                    MatchState artifactsMatchState = artifactMatchingMetadata.get().getMatchState();
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

        SIMILAR_Severity = getSeverityFromConfig(SIMILAR_SEVERITY_KEY, configMap, SIMILAR_Severity);
        UNKNOWN_Severity = getSeverityFromConfig(UNKNOWN_SEVERITY_KEY, configMap, UNKNOWN_Severity);
    }
}
