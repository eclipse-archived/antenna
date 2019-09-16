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

package org.eclipse.sw360.antenna.validators.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIssues;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.Issues;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.validators.workflow.processors.SecurityIssueValidator.*;
import static org.mockito.Mockito.when;

public class SecurityIssueValidatorTest extends AntennaTestWithMockedContext {
    private Issue openIssue;
    private Issue acknowledgedIssue;
    private Issue confirmedIssue;
    private Issue notApplicableIssue;
    private SecurityIssueValidator validator;
    private Map<String, String> configMap;

    @Before
    public void init() {
        openIssue = new Issue();
        openIssue.setStatus(SecurityIssueStatus.OPEN);
        openIssue.setSeverity(6.0);

        acknowledgedIssue = new Issue();
        acknowledgedIssue.setStatus(SecurityIssueStatus.ACKNOWLEDGED);
        acknowledgedIssue.setSeverity(4.0);

        confirmedIssue = new Issue();
        confirmedIssue.setStatus(SecurityIssueStatus.CONFIRMED);
        confirmedIssue.setSeverity(5.5);

        notApplicableIssue = new Issue();
        notApplicableIssue.setStatus(SecurityIssueStatus.NOT_APPLICABLE);
        notApplicableIssue.setSeverity(9.0);

        validator = new SecurityIssueValidator();
        validator.setAntennaContext(antennaContextMock);

        configMap = new HashMap<>();
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUS_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open");
    }

    @After
    public void after() {
        Mockito.verify(configMock, Mockito.atLeast(1)).getSecurityIssues();
        Mockito.verify(configMock, Mockito.atLeast(1)).getSuppressedSecurityIssues();
    }

    private Artifact mkArtifact(Issue issue) {
        return mkArtifact(Collections.singletonList(issue));
    }

    private Artifact mkArtifact(List<Issue> issueList) {
        Issues issues = new Issues();
        issues.getIssue().addAll(issueList);
        return mkArtifact(issues);
    }

    private Artifact mkArtifact(Issues issues) {
        Artifact artifact = new Artifact();
        artifact.addFact(new ArtifactIssues(issues));
        artifact.addFact(mkArtifactIdentifier());
        return artifact;
    }

    private ArtifactIdentifier mkArtifactIdentifier() {
        return new MavenCoordinates("com.test","test-artifact","1.0");
    }

    @Test
    public void validateWithoutForbiddenStatus() {
        Artifact artifact = mkArtifact(openIssue);

        validator.configure(Collections.emptyMap());
        assertThat(validator.validate(artifact).size()).isEqualTo(0);
    }

    @Test
    public void validateWithOpenStatus() {
        Artifact artifact = mkArtifact(openIssue);

        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUS_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open");
        validator.configure(configMap);

        assertThat(validator.validate(artifact).size()).isEqualTo(1);
        assertThat(validator.validate(artifact).stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.FAIL);
    }

    @Test
    public void validateWithMultipleForbiddenStatus() {
        Artifact artifact = mkArtifact(Arrays.asList(openIssue, acknowledgedIssue, confirmedIssue));

        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open,Acknowledged");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUS_SEVERITY_KEY, "WARN");
        validator.configure(configMap);

        assertThat(validator.validate(artifact).size()).isEqualTo(2);
        assertThat(validator.validate(artifact).stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.WARN);
    }

    @Test
    public void validateArtifactFromConfiguration() {
        Artifact emptyArtifact = new Artifact();
        emptyArtifact.addFact(mkArtifactIdentifier());

        Issues issues = new Issues();
        issues.getIssue().add(openIssue);

        Map<ArtifactSelector, Issues> configuredSecurityIssues = Collections.singletonMap(mkArtifactIdentifier(), issues);

        when(configMock.getSecurityIssues()).thenReturn(configuredSecurityIssues);
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open,Acknowledged");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUS_SEVERITY_KEY, "WARN");
        validator.configure(configMap);

        assertThat(validator.validate(emptyArtifact).size()).isEqualTo(1);
    }

    @Test
    public void validateWithSeverityLimit() {
        Artifact artifact = mkArtifact(Arrays.asList(openIssue, acknowledgedIssue, confirmedIssue));

        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_KEY, "5.5");
        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open,Acknowledged,Confirmed");
        validator.configure(configMap);

        assertThat(validator.validate(artifact).size()).isEqualTo(2);
        assertThat(validator.validate(artifact).stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.FAIL);
    }

    @Test
    public void validateDontFindNotApplicable() {
        Artifact artifact = mkArtifact(Arrays.asList(notApplicableIssue));
        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_KEY, "1.0");
        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open");
        validator.configure(configMap);

        assertThat(validator.validate(artifact).size()).isEqualTo(0);
    }

    @Test
    public void validateOnlyFindOpenIssues() {
        Artifact artifact = mkArtifact(Arrays.asList(notApplicableIssue, acknowledgedIssue, openIssue, confirmedIssue));

        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_KEY, "1.0");
        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open");
        validator.configure(configMap);

        List<IEvaluationResult> list = validator.validate(artifact);
        assertThat(list.size()).isEqualTo(1);
        IEvaluationResult result = list.get(0);
        assertThat(result.getDescription())
                .isEqualTo("The artifact has a security issue [null] with forbidden status OPEN.");
    }

    @Test
    public void validateDoesNotFindIssuesWithAllowedSecurityIssueStatus() {
        Artifact artifact = mkArtifact(Arrays.asList(notApplicableIssue, acknowledgedIssue, confirmedIssue));

        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_KEY, "1.0");
        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open");
        validator.configure(configMap);

        assertThat(validator.validate(artifact)).isEmpty();
    }

    @Test
    public void validateFindsOpenIssuesAccordingToSeverityConfiguration() {
        Issue issueWithLowSeverity = new Issue();
        issueWithLowSeverity.setStatus(SecurityIssueStatus.OPEN);
        issueWithLowSeverity.setSeverity(3.0);
        issueWithLowSeverity.setReference("DO-NOT-FIND-ME");

        Artifact ignoredArtifact = mkArtifact(Arrays.asList(issueWithLowSeverity));
        Artifact artifact = mkArtifact(Arrays.asList(openIssue));

        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_KEY, "5.0");
        configMap.put(SECURITY_ISSUE_SEVERITY_LIMIT_SEVERITY_KEY, "FAIL");
        configMap.put(FORBIDDEN_SECURITY_ISSUE_STATUSES_KEY, "Open");
        validator.configure(configMap);

        assertThat(validator.validate(ignoredArtifact)).isEmpty();
        assertThat(validator.validate(artifact)).hasSize(1);

    }
}
