/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.policy.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.policy.engine.PolicyEngine;
import org.eclipse.sw360.antenna.policy.engine.PolicyEngineConfigurator;
import org.eclipse.sw360.antenna.policy.engine.PolicyViolation;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;
import org.eclipse.sw360.antenna.workflow.stubs.AbstractComplianceChecker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The workspace step which integrates the {@link PolicyEngine} into the Antenna workflow engine
 */
public class PolicyEngineProcessor extends AbstractComplianceChecker {
    private PolicyEngine policyEngine;

    private static final String RULESET_PROP = "ruleset.classes";

    public PolicyEngineProcessor() {
        this.workflowStepOrder = VALIDATOR_BASE_ORDER + 11000;
    }

    @Override
    public void configure(final Map<String, String> configMap) {
        super.configure(configMap);

        final String rulesetClasses = getConfigValue(RULESET_PROP, configMap).trim();
        final Collection<String> rulesetClassesList = Arrays.stream(rulesetClasses.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        try {
            policyEngine = PolicyEngineConfigurator.configure(rulesetClassesList);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new ConfigurationException("Could not initialize Policy Engine!", e);
        }
    }

    @Override
    public WorkflowStepResult postProcessResult(final WorkflowStepResult result) {
        WorkflowStepResult pResult = super.postProcessResult(result);
        pResult.addAdditionalReportComment("Evaluated with " + getRulesetDescription());
        return pResult;
    }

    @Override
    public IPolicyEvaluation evaluate(final Collection<Artifact> artifacts) {
        final Collection<ThirdPartyArtifact> polengArtifacts = artifacts
                .stream()
                .map(AntennaArtifact::new)
                .collect(Collectors.toList());

        return new PolicyEvaluation(policyEngine.evaluate(polengArtifacts));
    }

    @Override
    public String getRulesetDescription() {
        return policyEngine.getRulesets()
                .stream()
                .map(this::prepareRulesetInfo)
                .collect(Collectors.joining(", "));
    }

    private String prepareRulesetInfo(Ruleset ruleset) {
        return String.format("%s [%s]", ruleset.getName(), ruleset.getVersion());
    }

    /**
     * Implementation for the Antenna {@link IPolicyEvaluation} result based on the {@link PolicyEngine} data
     */
    private static class PolicyEvaluation implements IPolicyEvaluation {
        private final Set<IEvaluationResult> evalResults;

        PolicyEvaluation(final Collection<PolicyViolation> data) {
            evalResults = data.stream().map(AntennaEvaluationResult::new).collect(Collectors.toSet());
        }

        @Override
        public Set<IEvaluationResult> getEvaluationResults() {
            return evalResults;
        }
    }

    /**
     * Implementation for the Antenna {@link IEvaluationResult} result based on the {@link PolicyEngine} result data
     */
    private static class AntennaEvaluationResult implements IEvaluationResult {
        private final PolicyViolation violation;

        AntennaEvaluationResult(final PolicyViolation violation) {
            this.violation = violation;
        }

        @Override
        public String getId() {
            return violation.getId();
        }

        @Override
        public String getDescription() {
            return violation.getDescription();
        }

        @Override
        public Severity getSeverity() {
            switch (violation.getSeverity()) {
                case WARN:
                    return Severity.INFO;
                case SEVERE:
                    return Severity.WARN;
                case CRITICAL:
                    return Severity.FAIL;
                default:
                    throw new IllegalStateException("Programming Error: No other case possible in enum");
            }
        }

        @Override
        public Set<Artifact> getFailedArtifacts() {
            return violation.getFailingArtifacts().stream()
                    .map(element -> ((AntennaArtifact) element).getArtifact())
                    .collect(Collectors.toSet());
        }
    }
}
