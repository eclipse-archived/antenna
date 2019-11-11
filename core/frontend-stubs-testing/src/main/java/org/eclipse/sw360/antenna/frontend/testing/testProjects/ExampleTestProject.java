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

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.frontend.testing.testProjects.TestProjectUtils.mkWorkflowStep;
import static org.junit.Assert.assertTrue;

public class ExampleTestProject extends AbstractTestProjectWithExpectations implements ExecutableTestProject {

    public ExampleTestProject() {
        super();
    }

    @Override
    public List<String> getOtherFilesToCopy() {
        return Stream.of("src/reportData.json",
                "src/dependencies.csv",
                "src/analyzer-result.yml")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getOutOfProjectFilesToCopy() {
        return Stream.of(
                "./src/example-policies/rules/DummyRule.drl",
                "./src/example-policies/policies.properties",
                "./src/example-policies/policies.xml")
                .collect(Collectors.toList());
    }

    @Override
    public String getExpectedProjectVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public String getExpectedToolConfigurationProductName() {
        return "Antenna EP";
    }

    @Override
    public String getExpectedToolConfigurationProductFullName() {
        return "Antenna Example Project";
    }

    @Override
    public String getExpectedToolConfigurationProductVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public List<String> getExpectedFilesToAttach() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExpectedToolConfigurationConfigFiles() {
        return Stream.of("src" + File.separator + "antennaconf.xml").collect(Collectors.toList());
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationAnalyzers() {
        List<WorkflowStep> analyzers = BasicConfiguration.getAnalyzers();
        analyzers.add(mkWorkflowStep("JSON Analyzer", "org.eclipse.sw360.antenna.workflow.analyzers.JsonAnalyzer",
                "base.dir", projectRoot.toString(),
                "file.path", "src/reportData.json"));
        analyzers.add(mkWorkflowStep("CSV Analyzer", "org.eclipse.sw360.antenna.workflow.analyzers.CsvAnalyzer",
                Stream.of(new String[][] {
                        { "base.dir", projectRoot.toString() },
                        { "file.path", "src/dependencies.csv" },
                        { "delimiter", "," }})
                        .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]))));
        analyzers.add(mkWorkflowStep("ORT Result Analyzer", "org.eclipse.sw360.antenna.ort.workflow.analyzers.OrtResultAnalyzer",
                "base.dir", projectRoot.toString(),
                "file.path", "src/analyzer-result.yml"));
        return analyzers;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationProcessors() {
        final List<WorkflowStep> processors = BasicConfiguration.getProcessors()
                .stream()
                .map(s -> {
                    if (!"Source Validator".equals(s.getName())) {
                        return s;
                    }
                    final Map<String, String> map = Stream.of(s.getConfiguration().getAsMap(), Collections.singletonMap("missingSourcesSeverity", "FAIL"))
                            .flatMap(m -> m.entrySet().stream())
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (v1, v2) -> v2));
                    WorkflowStep newS = new WorkflowStep();
                    newS.setName(s.getName());
                    newS.setClassHint(s.getClassHint());
                    newS.setConfiguration(StepConfiguration.fromMap(map));
                    newS.setDeactivated(true);
                    return newS;
                })
                .collect(Collectors.toList());

        Map<String, String> droolsConfig = Stream.of(new String[][] {
                { "base.dir", projectRoot.toString() },
                { "folder.paths", "./src/example-policies" },
                { "failOn", "WARN" }})
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));

        WorkflowStep checker1 = mkWorkflowStep("Drools Policy Engine", "org.eclipse.sw360.antenna.drools.workflow.processors.AntennaDroolsChecker", droolsConfig);
        processors.add(checker1);
        WorkflowStep enricher = mkWorkflowStep("SW360 Enricher", "org.eclipse.sw360.antenna.sw360.workflow.processors.SW360Enricher",
                Stream.of(new String[][] {
                        { "rest.server.url", "http://localhost:8080/resource/api" },
                        { "auth.server.url", "http://localhost:8080/authorization/oauth" },
                        { "user.id", "admin@sw360.org" },
                        { "user.password", "12345" },
                        { "client.id", "trusted-sw360-client" },
                        { "client.password", "sw360-secret" },
                        { "proxy.use", "false" }})
                        .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1])));

        enricher.setDeactivated(true);
        processors.add(enricher);
        return processors;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationGenerators() {
        List<WorkflowStep> result = BasicConfiguration.getGenerators();
        WorkflowStep generator = mkWorkflowStep("SW360 Updater", "org.eclipse.sw360.antenna.sw360.workflow.generators.SW360Updater",
                Stream.of(new String[][] {
                        { "rest.server.url", "http://localhost:8080/resource/api" },
                        { "auth.server.url", "http://localhost:8080/authorization/oauth" },
                        { "user.id", "admin@sw360.org" },
                        { "user.password", "12345" },
                        { "client.id", "trusted-sw360-client" },
                        { "client.password", "sw360-secret" },
                        { "proxy.use", "false" },
                        { "upload_sources", "true" }})
                        .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1])));
        generator.setDeactivated(true);
        result.add(generator);
        generator = mkWorkflowStep("Attribution Document", "org.eclipse.sw360.antenna.attribution.document.workflow.generators.AttributionDocumentGenerator",
                Stream.of(new String[][] {
                        { "attribution.doc.templateKey", "basic-pdf-template" },
                        { "attribution.doc.productVersion", "1.0.0" },
                        { "attribution.doc.productName", "Example Project" },
                        { "attribution.doc.copyrightHolder", "Copyright (c) 2013-2019 Bosch Software Innovations GmbH" }})
                        .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1])));
        result.add(generator);
        return result;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationOutputHandlers() {
        return Collections.singletonList(mkWorkflowStep(
                "Add disclosure document to jar", "org.eclipse.sw360.antenna.workflow.outputHandlers.FileToArchiveWriter",
                "instructions", "disclosure-doc:" + projectRoot.toString() + File.separator + "target/" + getExpectedProjectArtifactId() + "-" +getExpectedProjectVersion() + ".jar:/legalnotice/DisclosureDoc.html"));
    }

    @Override
    public int getExpectedProxyPort() {
        return 0;
    }

    @Override
    public String getExpectedProxyHost() {
        return null;
    }

    @Override
    public boolean getExpectedToolConfigurationMavenInstalled() {
        return false;
    }

    @Override
    public boolean getExpectedToolConfigurationAttachAll() {
        return true;
    }

    @Override
    public boolean getExpectedToolConfigurationSkip() {
        return false;
    }

    @Override
    public String getExpectedProjectArtifactId() {
        return "example-project";
    }

    @Override
    public Collection<String> getExpectedBuildArtifacts() {
        return Stream.of("artifact-information", "disclosure-doc", "sources-zip", "antenna-report").collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getExpectedDependencies() {
        return Stream.of("jackson-annotations-2.8.4", "jackson-core-2.8.4", "log4j-core-2.6.2").collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getExpectedP2Dependencies() {
        return Collections.emptyList();
    }

    @Override
    public void assertExecutionResult(Path pathToTarget, Map<String, IAttachable> buildArtifacts, AntennaContext context) throws Exception {
        final Path antennaOutDir = pathToTarget.resolve("antenna");
        assertTrue(antennaOutDir.toFile().exists());

        final Path report = antennaOutDir.resolve("Antenna_3rdPartyAnalysisReport.txt");
        assertTrue(report.toFile().exists());

        final Path depsDir = antennaOutDir.resolve("dependencies");
        assertTrue(depsDir.toFile().exists());
        assertDependenciesExistence(depsDir);

        if (buildArtifacts != null) {
            assertBuildArtifactsExistence(context.getProject(), buildArtifacts.entrySet());
            assertSourceZipContents(buildArtifacts.entrySet());
        }
    }

    @Override
    public boolean getExpectedConfigurationFailOnIncompleteSources() {
        return true;
    }

    @Override
    public boolean getExpectedConfigurationFailOnMissingSources() {
        return false;
    }

    @Override
    public List<String> getExpectedToolConfigurationConfigFilesEndings() {
        return Collections.singletonList(File.separator + "src" + File.separator + "antennaconf.xml");
    }

    @Override
    public boolean requiresMaven() {
        return false;
    }
}
