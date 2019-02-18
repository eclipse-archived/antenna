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
package org.eclipse.sw360.antenna.frontend.testProjects;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.frontend.testProjects.TestProjectUtils.mkWorkflowStep;
import static org.junit.Assert.assertTrue;

public class ExampleTestProject extends AbstractTestProjectWithExpectations implements ExecutableTestProject {

    public ExampleTestProject() {
        super();
    }

    @Override
    public List<String> getOtherFilesToCopy() {
        return Stream.of(
                "src/reportData.json",
                "src/dependencies.csv",
                "additional_p2_resources/p2.index",
                "additional_p2_resources/artifacts.jar",
                "additional_p2_resources/artifacts.xml.xz",
                "additional_p2_resources/content.jar",
                "additional_p2_resources/content.xml.xz",
                "additional_p2_resources/features/some_feature_0.0.1.201902181544.jar",
                "additional_p2_resources/plugins/some_bundle_0.0.1.201902181544.jar")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getOutOfProjectFilesToCopy() {
        return Stream.of(
                "../example-policies/rules/DummyRule.drl",
                "../example-policies/policies/policies.properties",
                "../example-policies/policies.xml")
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
        return Stream.of("src/antennaconf.xml").collect(Collectors.toList());
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationAnalyzers() {
        WorkflowStep analyzer1 = mkWorkflowStep("JSON Analyzer", "org.eclipse.sw360.antenna.workflow.analyzers.JsonAnalyzer",
                "base.dir", this.projectRoot.toString(),
                "file.path", "src/reportData.json");
        WorkflowStep analyzer2 = mkWorkflowStep("CSV Analyzer", "org.eclipse.sw360.antenna.workflow.analyzers.CsvAnalyzer",
                "base.dir", this.projectRoot.toString(),
                "file.path", "src/dependencies.csv");
        return Stream.of(analyzer1, analyzer2).collect(Collectors.toList());
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationProcessors() {
        final List<WorkflowStep> processors = new BasicConfiguration().getProcessors()
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
        // checkers
        WorkflowStep checker1 = mkWorkflowStep("Drools Policy Engine", "org.eclipse.sw360.antenna.workflow.processors.checkers.AntennaDroolsChecker",
                "base.dir", this.projectRoot.toString(),
                "folder.paths", "../example-policies");
        processors.add(checker1);
        WorkflowStep enricher1 = mkWorkflowStep("P2 Resolver", "org.eclipse.sw360.antenna.workflow.processors.enricher.P2Resolver",
                "repositories", this.projectRoot.toString() + "/additional_p2_resources",
                "filepath", this.projectRoot.toString() + "/target/antenna/dependencies");
        processors.add(enricher1);
        return processors;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationGenerators() {
        WorkflowStep generator1 = mkWorkflowStep("HTML Report Writer", "org.eclipse.sw360.antenna.workflow.generators.HTMLReportGenerator");
        WorkflowStep generator2 = mkWorkflowStep("CSV Report Writer", "org.eclipse.sw360.antenna.workflow.generators.CSVGenerator");
        WorkflowStep generator3 = mkWorkflowStep("SW360 Updater", "org.eclipse.sw360.antenna.workflow.generators.SW360Updater",
                new HashMap<String, String>() {{
                    put("rest.server.url", "http://localhost:8080/resource/api");
                    put("auth.server.url", "http://localhost:8080/authorization");
                    put("username", "admin@sw360.org");
                    put("password", "12345");
                }});
        generator3.setDeactivated(true);

        List<WorkflowStep> result = Stream.of(generator1, generator2, generator3).collect(Collectors.toList());
        result.addAll(new BasicConfiguration().getGenerators(projectRoot.toString()));
        result.stream()
                .filter(g -> "SW360 Report Generator".equals(g.getName()))
                .forEach(g -> g.setConfiguration(StepConfiguration.fromMap(Collections.singletonMap("disclosure.doc.formats", "txt"))));
        return result;
    }

    @Override
    public List<WorkflowStep> getExpectedToolConfigurationOutputHandlers() {
        return Collections.singletonList(mkWorkflowStep(
                "Add disclosure document to jar", "org.eclipse.sw360.antenna.workflow.outputHandlers.FileToArchiveWriter",
                "instructions", "disclosure-sw360-doc-txt:" + projectRoot.toString() + File.separator + "target/example-project-1.0-SNAPSHOT.jar:/legalnotice/DisclosureDoc.txt"));
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
        return Stream.of("artifact-information", "disclosure-doc", "disclosure-sw360-doc-txt", "sources-zip", "antenna-report").collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getExpectedDependencies() {
        return Stream.of("jackson-annotations-2.8.4", "jackson-core-2.8.4", "log4j-core-2.6.2").collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getExpectedP2Dependencies() {
        return Stream.of("some_bundle_0.0.1.201902181544").collect(Collectors.toSet());
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
