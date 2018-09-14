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
package org.eclipse.sw360.antenna.workflow.processors.checkers;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.model.Artifact;

import java.util.*;

public class DefaultPolicyEvaluation implements IPolicyEvaluation {

    private final Set<IEvaluationResult> evaluationResults = new HashSet<>();

    public void addEvaluationResult(String ruleId, String description, IEvaluationResult.Severity severity, Artifact failedArtifact) {
        addEvaluationResult(new DefaultEvaluationResult(ruleId, description, severity, failedArtifact));
    }

    public void addEvaluationResult(String ruleId, String description, IEvaluationResult.Severity severity, Collection<Artifact> failedArtifacts) {
        addEvaluationResult(new DefaultEvaluationResult(ruleId, description, severity, failedArtifacts));
    }

    public void addEvaluationResult(IEvaluationResult iEvaluationResult) {
        evaluationResults.add(iEvaluationResult);
    }

    @Override
    public Set<IEvaluationResult> getEvaluationResults() {
        return evaluationResults;
    }

    public static class DefaultEvaluationResult implements IEvaluationResult {

        private final String ruleId;
        private final String description;
        private final Severity severity;
        private final Set<Artifact> failedArtifacts;

        public DefaultEvaluationResult(String ruleId, String description, Severity severity, Artifact failedArtifact) {
            this.ruleId = ruleId;
            this.description = description;
            this.severity = severity;
            this.failedArtifacts = Collections.singleton(failedArtifact);
        }

        public DefaultEvaluationResult(String ruleId, String description, Severity severity, Collection<Artifact> failedArtifacts) {
            this.ruleId = ruleId;
            this.description = description;
            this.severity = severity;
            this.failedArtifacts = new HashSet<>(failedArtifacts);
        }

        @Override
        public String getId() {
            return ruleId;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Severity getSeverity() {
            return severity;
        }

        @Override
        public Set<Artifact> getFailedArtifacts() {
            return failedArtifacts;
        }
    }
}
