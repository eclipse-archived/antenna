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

package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIssues;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.Issues;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;
import org.eclipse.sw360.antenna.workflow.processors.checkers.AbstractComplianceChecker;
import org.eclipse.sw360.antenna.workflow.processors.checkers.DefaultPolicyEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityIssueValidator extends AbstractComplianceChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityIssueValidator.class);
    static final String FORBIDDEN_SECURITY_ISSUE_STATUS_SEVERITY_KEY = "forbiddenSecurityIssueStatusSeverity";
    static final String SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY = "securityIssueSeverityLimitSeverity";
    static final String FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY = "forbiddenSecurityIssueStatuses";
    static final String SECURITY_ISSUE_SEVERITY_LIMIT_KEY = "securityIssueSeverityLimit";
    private static final String IGNORE_SECURITY_ISSUE_REFERENCES_KEY = "ignoreSecurityIssueReferences";
    private IEvaluationResult.Severity forbiddenSecurityIssueStatusSeverity = IEvaluationResult.Severity.FAIL;
    private IEvaluationResult.Severity securityIssueSeverityLimitSeverity = IEvaluationResult.Severity.FAIL;
    private List<SecurityIssueStatus> forbiddenSecurityIssueStatusesList;
    private List<String> ignoreSecurityIssueReferences;
    private double securityIssueSeverityLimit;
    private Map<ArtifactSelector, Issues> configuredSecurityIssues;
    private Map<String, Map<ArtifactSelector, GregorianCalendar>> suppressedSecurityIssues;

    public List<IEvaluationResult> validate(Artifact artifact) {
        List<Issue> configuredIssueList = configuredSecurityIssues.entrySet().stream()
                .filter(entry -> entry.getKey().matches(artifact))
                .map(Map.Entry::getValue)
                .map(Issues::getIssue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Issues issues = new Issues();
        issues.getIssue().addAll(mergeIssues(artifact.askForGet(ArtifactIssues.class).orElse(Collections.emptyList()), configuredIssueList));

        return new ArrayList<>(checkSecurityIssue(artifact, issues.getIssue()));
    }

    private List<Issue> mergeIssues(List<Issue> actualIssueList, List<Issue> configuredIssueList) {
        return Stream.concat(actualIssueList.stream(), configuredIssueList.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<IEvaluationResult> checkSecurityIssue(Artifact artifact, List<Issue> issuesList) {
        List<IEvaluationResult> results = new ArrayList<>();
        for (Issue issue : issuesList) {
            if (ignoreSecurityIssueReferences.contains(issue.getReference())) {
                LOGGER.debug("Do not validate security issue=[" + issue.getReference() + "], since it is ignored for validation");
                continue;
            }
            if(SecurityIssueStatus.NOT_APPLICABLE.equals(issue.getStatus())) {
                LOGGER.debug("Ignore not applicable issue=[" + issue.getReference() + "]");
                continue;
            }
            if (suppressedSecurityIssues != null && suppressedSecurityIssues.containsKey(issue.getReference())) {
                final Map<ArtifactSelector, GregorianCalendar> issueSuppressors = suppressedSecurityIssues.get(issue.getReference());
                final Optional<GregorianCalendar> suppressedUntil = issueSuppressors.entrySet().stream()
                        .filter(e -> e.getKey().matches(artifact))
                        .map(Map.Entry::getValue)
                        .max(Comparator.naturalOrder());
                if (suppressedUntil.isPresent() && new GregorianCalendar().compareTo(suppressedUntil.get()) < 0) {
                    continue;
                }
            }
            if (forbiddenSecurityIssueStatusesList.contains(issue.getStatus())) {
                results.add(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                        "SecurityIssueValidator::forbiddenSecurityIssueStatus",
                        "The artifact has a security issue [" + issue.getReference() +
                                "] with forbidden status " + issue.getStatus() + ".",
                        forbiddenSecurityIssueStatusSeverity,
                        artifact));
            }
            if (issue.getSeverity() >= securityIssueSeverityLimit) {
                results.add(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                        "SecurityIssueValidator::securityIssueSeverityLimit",
                        "The artifact has a security issue [" + issue.getReference() +
                                "] with a severity " + issue.getSeverity() +
                                ", which is above the limit " + securityIssueSeverityLimit + ".",
                        securityIssueSeverityLimitSeverity,
                        artifact));
            }
        }
        return results;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
        DefaultPolicyEvaluation policyEvaluation = new DefaultPolicyEvaluation();

        artifacts.stream()
                .map(this::validate)
                .flatMap(Collection::stream)
                .forEach(policyEvaluation::addEvaluationResult);

        return policyEvaluation;
    }

    @Override
    public String getRulesetDescription() { return "Security Issue Validator"; }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);

        configuredSecurityIssues = context.getConfiguration().getSecurityIssues();
        suppressedSecurityIssues = context.getConfiguration().getSuppressedSecurityIssues();

        List<String> statusStrList = getCommaSeparatedConfigValue(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, configMap);
        forbiddenSecurityIssueStatusesList = statusStrList.stream()
                .map(SecurityIssueStatus::fromValue)
                .collect(Collectors.toList());

        forbiddenSecurityIssueStatusSeverity = getSeverityFromConfig(FORBIDDEN_SECURITY_ISSUE_STATUS_SEVERITY_KEY, configMap, IEvaluationResult.Severity.FAIL);
        securityIssueSeverityLimitSeverity = getSeverityFromConfig(SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY, configMap, IEvaluationResult.Severity.FAIL);
        securityIssueSeverityLimit = Double.valueOf(getConfigValue(SECURITY_ISSUE_SEVERITY_LIMIT_KEY, configMap, String.valueOf(Double.MAX_VALUE)));
        ignoreSecurityIssueReferences = getCommaSeparatedConfigValue(IGNORE_SECURITY_ISSUE_REFERENCES_KEY, configMap);
    }
}
