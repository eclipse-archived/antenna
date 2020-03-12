/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.testing.testProjects;

import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.frontend.testing.testProjects.TestProjectUtils.mkDeactivatedWorkflowStep;
import static org.eclipse.sw360.antenna.frontend.testing.testProjects.TestProjectUtils.mkWorkflowStep;

public abstract class AbstractTestProjectWithExpectations extends AbstractTestProject {
    public AbstractTestProjectWithExpectations() {
        super();
    }

    // Expected parsed parameters:
    public abstract String getExpectedProjectVersion();
    public abstract String getExpectedToolConfigurationProductName();
    public abstract String getExpectedToolConfigurationProductFullName();
    public abstract String getExpectedToolConfigurationProductVersion();

    public List<String> getExpectedFilesToAttach() {
        return new ArrayList<>();
    }

    public List<String> getExpectedToolConfigurationConfigFiles() {
        return new ArrayList<>();
    }

    public List<WorkflowStep> getExpectedToolConfigurationAnalyzers() {
        WorkflowStep confAnalyzer = mkWorkflowStep("Analyzing of Antenna configuration", "org.eclipse.sw360.antenna.workflow.analyzers.ConfigurationAnalyzer");
        return Stream.of(confAnalyzer).collect(Collectors.toList());
    }

    public List<WorkflowStep> getExpectedToolConfigurationGenerators() {
        WorkflowStep generator1 = mkWorkflowStep("HTML Report Writer", "org.eclipse.sw360.antenna.workflow.generators.HTMLReportGenerator");
        WorkflowStep generator2 = mkWorkflowStep("CSV Report Writer", "org.eclipse.sw360.antenna.workflow.generators.CSVGenerator");
        WorkflowStep generator3 = mkWorkflowStep("Source Zip Writer", "org.eclipse.sw360.antenna.workflow.generators.SourceZipWriter");
        return Stream.of(generator1, generator2, generator3).collect(Collectors.toList());
    }

    public List<WorkflowStep> getExpectedToolConfigurationProcessors() {
        WorkflowStep confHandler = mkWorkflowStep("Processing of Antenna configuration", "org.eclipse.sw360.antenna.workflow.processors.AntennaConfHandler");
        // enricher
        WorkflowStep enricher1 = mkWorkflowStep("Maven Artifact Resolver", "org.eclipse.sw360.antenna.maven.workflow.processors.enricher.MavenArtifactResolver");
        WorkflowStep enricher4 = mkDeactivatedWorkflowStep("Manifest Resolver", "org.eclipse.sw360.antenna.maven.workflow.processors.enricher.ManifestResolver");
        WorkflowStep enricher5 = mkWorkflowStep("License Resolver", "org.eclipse.sw360.antenna.workflow.processors.LicenseResolver");
        WorkflowStep enricher6 = mkWorkflowStep("License Knowledgebase Resolver", "org.eclipse.sw360.antenna.workflow.processors.LicenseKnowledgeBaseResolver");
        // validators
        WorkflowStep validator1 = mkWorkflowStep("Coordinates Validator", "org.eclipse.sw360.antenna.validators.workflow.processors.CoordinatesValidator",
                "failOnMissingCoordinates", "WARN");
        WorkflowStep validator2 = mkWorkflowStep("Source Validator", "org.eclipse.sw360.antenna.validators.workflow.processors.SourceValidator",
                "missingSourcesSeverity", "WARN",
                "incompleteSourcesSeverity", "WARN");

        Map<String,String> configMap = new HashMap<>();
        configMap.put("forbiddenLicenseSeverity", "FAIL");
        configMap.put("missingLicenseInformationSeverity", "WARN");
        configMap.put("missingLicenseTextSeverity", "WARN");
        configMap.put("forbiddenLicenses", "");
        configMap.put("ignoredLicenses", "");
        WorkflowStep validator3 = mkWorkflowStep("License Validator", "org.eclipse.sw360.antenna.validators.workflow.processors.LicenseValidator",
                configMap);
        WorkflowStep validator4 = mkWorkflowStep("Match State Validator", "org.eclipse.sw360.antenna.validators.workflow.processors.MatchStateValidator",
                "severityOfSIMILAR", "INFO",
                "severityOfUNKNOWN", "WARN");
        configMap = new HashMap<>();
        configMap.put("forbiddenSecurityIssueStatusSeverity", "FAIL");
        configMap.put("securityIssueSeverityLimitSeverity", "FAIL");
        configMap.put("forbiddenSecurityIssueStatuses", "Open");
        configMap.put("securityIssueSeverityLimit", "5.0");
        WorkflowStep validator5 = mkWorkflowStep("Security Issue Validator", "org.eclipse.sw360.antenna.validators.workflow.processors.SecurityIssueValidator",
                configMap);
        return Stream.of(confHandler,
                enricher1, enricher4, enricher5, enricher6,
                validator1, validator2, validator3, validator4, validator5).collect(Collectors.toList());
    }

    public List<WorkflowStep> getExpectedToolConfigurationOutputHandlers() {
        return new ArrayList<>();
    }

    public int getExpectedProxyPort() {
        return 0;
    }

    public String getExpectedProxyHost() {
        return null;
    }

    public boolean getExpectedToolConfigurationMavenInstalled() {
        return false;
    }

    public boolean getExpectedToolConfigurationAttachAll() {
        return true;
    }

    public boolean getExpectedToolConfigurationSkip() { return false; }

    public List<String> getExpectedToolConfigurationConfigFilesEndings() {
        return new ArrayList<>();
    }

    public boolean requiresMaven() {
        return false;
    }
}
