/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.stubs;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractComplianceChecker extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractComplianceChecker.class);
    private IEvaluationResult.Severity failOn;
    protected static final String FAIL_ON_KEY = "failOn";
    private Set<IEvaluationResult> evaluationResults = new HashSet<>();

    public AbstractComplianceChecker() {
        this.workflowStepOrder = VALIDATOR_BASE_ORDER;
    }

    public abstract IPolicyEvaluation evaluate(Collection<Artifact> artifacts);

    public abstract String getRulesetDescription();

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Evaluate compliance rule set: {}", getRulesetDescription());
        IPolicyEvaluation evaluation = evaluate(artifacts);
        LOGGER.debug("Rule evaluation done");
        LOGGER.debug("Check evaluation results...");
        execute(evaluation);
        LOGGER.debug("Check evaluation results... done.");
        return artifacts;
    }

    @Override
    public WorkflowStepResult postProcessResult(WorkflowStepResult result) {
        WorkflowStepResult pResult = super.postProcessResult(result);
        if (evaluationResults.size() > 0) {
            pResult.addFailCausingResults(getWorkflowItemName(), evaluationResults);
        }
        return pResult;
    }

    @Override
    public void configure(Map<String, String> configMap) {
        failOn = getSeverityFromConfig(FAIL_ON_KEY, configMap, IEvaluationResult.Severity.FAIL);
    }

    public void execute(IPolicyEvaluation evaluation) {
        IProcessingReporter reporter = context.getProcessingReporter();

        reportResults(reporter, evaluation.getEvaluationResults());

        Set<IEvaluationResult> failResults = getResults(evaluation, IEvaluationResult.Severity.FAIL);
        Set<IEvaluationResult> warnResults = getResults(evaluation, IEvaluationResult.Severity.WARN);
        Set<IEvaluationResult> infoResults = getResults(evaluation, IEvaluationResult.Severity.INFO);

        /*
         * The severity is ordered: INFO < WARN < FAIL If the user specified
         * <entry key="failOn" value="INFO"/> the build will fail on INFO, WARN and FAIL If
         * the user specified <entry key="failOn" value="WARN"/> the build will fail on WARN
         * and FAIL If the user specified <entry key="failOn" value="WARN"/> the build will
         * only fail on FAIL (this is the default)
         */
        if (failOn == IEvaluationResult.Severity.INFO) {
            evaluationResults.addAll(infoResults);
            evaluationResults.addAll(warnResults);
            evaluationResults.addAll(failResults);
        } else if (failOn == IEvaluationResult.Severity.WARN) {
            evaluationResults.addAll(warnResults);
            evaluationResults.addAll(failResults);
        } else if (failOn == IEvaluationResult.Severity.FAIL) {
            evaluationResults.addAll(failResults);
        }
    }

    private Set<IEvaluationResult> getResults(IPolicyEvaluation evaluation, IEvaluationResult.Severity level) {
        return evaluation.getEvaluationResults().stream()
                .filter(r -> r.getSeverity() == level && r.getFailedArtifacts().size() > 0).collect(Collectors.toSet());
    }

    private void reportResults(IProcessingReporter reporter, Set<IEvaluationResult> results) {
        results.stream()
                .sorted(Comparator.comparing(IEvaluationResult::getId))
                .forEach(result -> reportSingleResult(reporter, result));
        }

    private void reportSingleResult(IProcessingReporter reporter, IEvaluationResult result) {
        String resultString = "Policy Violation: " + result.resultAsMessage();
        if (result.getSeverity().equals(IEvaluationResult.Severity.FAIL)) {
            LOGGER.error(resultString);
            result.getFailedArtifacts().forEach(a -> reporter.add(MessageType.PROCESSING_FAILURE, resultString));
        } else {
            LOGGER.warn(resultString);
            result.getFailedArtifacts().forEach(a -> reporter.add(MessageType.RULE_ENGINE, resultString));
        }
    }

    protected IEvaluationResult.Severity getSeverityFromConfig(String key, Map<String, String> configMap, IEvaluationResult.Severity defaultSeverity) {
        return Optional.ofNullable(configMap.get(key))
                .map(IEvaluationResult.Severity::fromValue)
                .orElse(defaultSeverity);
    }
}
