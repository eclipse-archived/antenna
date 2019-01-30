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
package org.eclipse.sw360.antenna.workflow.processors.checkers;


import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
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

    public abstract IPolicyEvaluation evaluate(Collection<Artifact> artifacts) throws AntennaException;

    public abstract String getRulesetDescription();

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) throws AntennaException {
        LOGGER.info("Evaluate compliance rule set: {}", getRulesetDescription());
        IPolicyEvaluation evaluation = evaluate(artifacts);
        LOGGER.info("Rule evaluation done");
        LOGGER.info("Check evaluation results...");
        execute(evaluation);
        LOGGER.info("Check evaluation results... done.");
        return artifacts;
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        failOn = getSeverityFromConfig(FAIL_ON_KEY, configMap, IEvaluationResult.Severity.FAIL);
    }

    public void execute(IPolicyEvaluation evaluation) throws AntennaException {
        IProcessingReporter reporter = context.getProcessingReporter();

        reportResults(reporter, evaluation.getEvaluationResults());

        Set<IEvaluationResult> failResults = getResults(evaluation, IEvaluationResult.Severity.FAIL);
        Set<IEvaluationResult> warnResults = getResults(evaluation, IEvaluationResult.Severity.WARN);
        Set<IEvaluationResult> infoResults = getResults(evaluation, IEvaluationResult.Severity.INFO);

        Set<IEvaluationResult> failCausingResults = new HashSet<>();

        /*
         * The severity is ordered: INFO < WARN < FAIL If the user specified
         * <failOn>INFO</failOn> the build will fail on INFO, WARN and FAIL If
         * the user specified <failOn>WARN</failOn> the build will fail on WARN
         * and FAIL If the user specified <failOn>FAIL</failOn> the build will
         * only fail on FAIL
         */
        switch (failOn.value()) {
            case "INFO": failCausingResults.addAll(infoResults);
            case "WARN": failCausingResults.addAll(warnResults);
            case "FAIL": failCausingResults.addAll(failResults);
        }

        // Flag the build as failed if the report engine reports it as so.
        if (failCausingResults.size() > 0) {
            String messagePrefix = "Rule engine=[" + getRulesetDescription() + "] failed evaluation.";
            reporter.add(MessageType.PROCESSING_FAILURE, messagePrefix);
            String fullMessage = makeStringForEvaluationResults(messagePrefix, failCausingResults);
            LOGGER.info(fullMessage);
            throw new AntennaComplianceException(fullMessage);
        }
    }

    protected String makeStringForEvaluationResults(String messagePrefix, Set<IEvaluationResult> failCausingResults) {
        Map<String,Set<IEvaluationResult>> transposedFailCausingResults = new HashMap();
        failCausingResults.forEach(iEvaluationResult ->
                iEvaluationResult.getFailedArtifacts().forEach(artifact -> {
                            String artifactRepresentation = artifact.toString();
                            if(! transposedFailCausingResults.containsKey(artifactRepresentation)){
                                transposedFailCausingResults.put(artifactRepresentation, new HashSet<>());
                            }
                            transposedFailCausingResults.get(artifactRepresentation).add(iEvaluationResult);
                        }
                ));

        String msges = transposedFailCausingResults.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> makeStringForEvaluationResultsForArtifact(entry.getKey(), entry.getValue()))
                .limit(3)
                .reduce("", (s1,s2) -> s1 + s2);
        if (transposedFailCausingResults.size() > 3){
            msges += "\n\t - ... and " + (transposedFailCausingResults.size() - 3) + " artifacts more";
        }
        String header = messagePrefix + " Due to:";
        return header + msges + "\nSee generated report for details.";
    }

    protected String makeStringForEvaluationResultsForArtifact(String artifactRepresentation, Set<IEvaluationResult> failCausingResultsForArtifact) {
        String msg = failCausingResultsForArtifact.stream()
                .map(iEvaluationResult -> "\n\t\t- " + iEvaluationResult.getDescription())
                .sorted()
                .limit(3)
                .reduce("\n\t- the artifact=[" + artifactRepresentation + "] failed, due to:", (s1,s2) -> s1 + s2);
        if (failCausingResultsForArtifact.size() > 3) {
            return msg + "\n\t\t- ... and " + (failCausingResultsForArtifact.size() -3 ) + " fail causing results more";
        }
        return msg;
    }

    private Set<IEvaluationResult> getResults(IPolicyEvaluation evaluation, IEvaluationResult.Severity level) {
        return evaluation.getEvaluationResults().stream()
                .filter(r -> r.getSeverity() == level && r.getFailedArtifacts().size() > 0).collect(Collectors.toSet());
    }

    private void reportResults(IProcessingReporter reporter, Set<IEvaluationResult> results) {
        if (results.size() > 0) {
            results.forEach(r ->
                    r.getFailedArtifacts().forEach(a ->
                            reporter.add(MessageType.RULE_ENGINE,
                                    r.getSeverity() + ": " + r.getDescription())));
        }
    }

    protected IEvaluationResult.Severity getSeverityFromConfig(String key, Map<String,String> configMap, IEvaluationResult.Severity defaultSeverity) {
        return Optional.ofNullable(configMap.get(key))
                .map(IEvaluationResult.Severity::fromValue)
                .orElse(defaultSeverity);
    }

    protected IEvaluationResult.Severity getSeverityFromBool(boolean hasFailSeverity) {
        if(hasFailSeverity) {
            return IEvaluationResult.Severity.FAIL;
        }
        return IEvaluationResult.Severity.WARN;
    }
}
