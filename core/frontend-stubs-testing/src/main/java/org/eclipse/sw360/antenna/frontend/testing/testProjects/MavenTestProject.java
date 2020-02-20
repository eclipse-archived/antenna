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
package org.eclipse.sw360.antenna.frontend.testing.testProjects;

import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.frontend.testing.testProjects.TestProjectUtils.mkWorkflowStep;

public class MavenTestProject extends ExampleTestProject {

    public static final String LOCAL_REPOSITORY_ROOT = "local-test-repo";

    public MavenTestProject() {
        super();
    }

    @Override
    public List<String> getOtherFilesToCopy() {
        return Stream.of(
                String.format("src%sreportData.json", File.separator),
                String.format("src%sdependencies.csv", File.separator),
                String.format("%s%sorg%seclipse%ssw360%santenna%sexample-dependency%s1.0%sexample-dependency-1.0.jar",
                        LOCAL_REPOSITORY_ROOT, File.separator, File.separator, File.separator,
                        File.separator, File.separator, File.separator, File.separator),
                String.format("%s%sorg%seclipse%ssw360%santenna%sexample-dependency%s1.0%sexample-dependency-1.0.pom",
                        LOCAL_REPOSITORY_ROOT, File.separator, File.separator, File.separator,
                        File.separator, File.separator, File.separator, File.separator))
                .collect(Collectors.toList());
    }

    @Override
    public String getExpectedToolConfigurationProductName() {
        return "Antenna mvn EP";
    }

    @Override
    public String getExpectedToolConfigurationProductFullName() {
        return "Antenna Maven Test Project";
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationAnalyzers() {
        List<WorkflowStep> analyzers = new ArrayList<>();
        analyzers.add(mkWorkflowStep("Analyzing of Antenna configuration", "org.eclipse.sw360.antenna.workflow.analyzers.ConfigurationAnalyzer"));
        analyzers.add(mkWorkflowStep("JSON Analyzer", "org.eclipse.sw360.antenna.workflow.analyzers.JsonAnalyzer",
                "base.dir", this.projectRoot.toString(),
                "file.path", "src/reportData.json"));
        analyzers.add(mkWorkflowStep("CSV Analyzer", "org.eclipse.sw360.antenna.workflow.analyzers.CsvAnalyzer",
                "base.dir", this.projectRoot.toString(),
                "file.path", "src/dependencies.csv"));
        analyzers.add(mkWorkflowStep("Maven dependency analyzer", "org.eclipse.sw360.antenna.maven.workflow.analyzers.MvnDependencyTreeAnalyzer"));
        return analyzers;
    }

    @Override
    public String getExpectedProjectArtifactId() {
        return "mvn-test-project";
    }

    @Override
    public boolean requiresMaven() {
        return true;
    }
}
