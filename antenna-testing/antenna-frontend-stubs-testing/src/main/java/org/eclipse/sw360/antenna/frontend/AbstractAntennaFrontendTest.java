/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend;

import org.apache.commons.lang.StringUtils;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.frontend.testProjects.*;
import org.eclipse.sw360.antenna.model.util.WorkflowComparator;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.assumeToBeConnectedToTheInternet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
abstract public class AbstractAntennaFrontendTest {

    private final Supplier<AbstractTestProjectWithExpectations> testDataSupplier;
    protected AbstractTestProjectWithExpectations testData;
    protected AntennaFrontend antennaFrontend;
    protected AntennaContext antennaContext;
    protected boolean runExecutionTest;

    @Parameterized.Parameters(name = "{index}: Test data = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {(Supplier<AbstractTestProjectWithExpectations>) MinimalTestProject::new, "minimal configuration"},
                {(Supplier<AbstractTestProjectWithExpectations>) BasicTestProject::new, "basic configuration"},
                {(Supplier<AbstractTestProjectWithExpectations>) ExampleTestProject::new, "example configuration"},
                {(Supplier<AbstractTestProjectWithExpectations>) MavenTestProject::new, "maven configuration"},
        });
    }

    public AbstractAntennaFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name){
        this.testDataSupplier = testDataSupplier;
    }

    @Before
    public void initializeTestProject() {
        this.testData = testDataSupplier.get();
    }

    @After
    public void cleanup()
            throws IOException {
        testData.cleanUpTemporaryProjectFolder();
    }

    @Test
    public void antennaFrontendReadsProject() {
        assertEquals(testData.getExpectedProjectArtifactId(), antennaContext.getProject().getProjectId());
        assertEquals(testData.getExpectedProjectVersion(), antennaContext.getProject().getVersion());
    }

    @Test
    public void checkConfiguredCoordinates() {
        assertEquals(testData.getExpectedToolConfigurationProductName(), antennaContext.getToolConfiguration().getProductName());
        assertEquals(testData.getExpectedToolConfigurationProductFullName(), antennaContext.getToolConfiguration().getProductFullName());
        assertEquals(testData.getExpectedToolConfigurationProductVersion(), antennaContext.getToolConfiguration().getVersion());
    }

    @Test
    public void checkFilesToAttach() {
        List<String> expectedAttachments = testData.getExpectedFilesToAttach();
        assertEquals(expectedAttachments.size(), antennaContext.getToolConfiguration().getFilesToAttach().size());
        expectedAttachments.forEach(k -> assertTrue(antennaContext.getToolConfiguration().getFilesToAttach().stream()
                .anyMatch(f -> f.equals(k))));
        antennaContext.getToolConfiguration().getFilesToAttach()
                .forEach(a -> assertEquals(2, StringUtils.countMatches(a, ",")));
    }

    @Test
    public void checkConfigFiles() {
        List<String> expectedConfigFilesEndings = testData.getExpectedToolConfigurationConfigFilesEndings();
        List<File> actualConfigFiles = antennaContext.getToolConfiguration().getConfigFiles();
        assertEquals(expectedConfigFilesEndings.size(), actualConfigFiles.size());
        assertTrue(actualConfigFiles.stream()
                .map(File::toString)
                .allMatch(s -> expectedConfigFilesEndings.stream()
                        .anyMatch(s::endsWith)));
    }

    @Test
    public void checkParsedConfigFiles() {
        List<String> expectedConfigFiles = testData.getExpectedToolConfigurationConfigFiles();
        assertEquals(expectedConfigFiles.size(), antennaContext.getToolConfiguration().getConfigFiles().size());
        expectedConfigFiles.stream()
                .map(f -> new File(f).getName())
                .forEach(basename -> assertTrue(antennaContext.getToolConfiguration().getConfigFiles().stream()
                        .anyMatch(file -> file.getName().equals(basename))));
    }

    private void assertThatStepsAreEqualUpToOrder(List<WorkflowStep> l1, List<WorkflowStep> l2) {
        try {
            assertTrue(WorkflowComparator.areEqual(l1,l2));
        } catch (AssertionError e) {
            final Function<List<WorkflowStep>, String> lToStringFunction = l -> l.stream()
                    .map(s -> s.getName() + "(" + s.getClassHint() + ")")
                    .map(s -> "[" + s + "]")
                    .collect(Collectors.joining(",\n\t\t"));
            String l1String = lToStringFunction.apply(l1);
            String l2String = lToStringFunction.apply(l2);
            String msg = "Step Configurations are not equal:\n" +
                    "\texpected contains: " + l1String +
                    "\n\tactual contains: " + l2String;
            throw new AssertionError(msg ,e);
        }
    }

    @Test
    public void checkParsedWorkflowForAnalyzers() {
        List<WorkflowStep> expectedAnalyzers = testData.getExpectedToolConfigurationAnalyzers();
        if(antennaContext.getToolConfiguration().getWorkflow().getAnalyzers() == null){
            assertEquals(0, expectedAnalyzers.size());
            return;
        }
        List<WorkflowStep> actualAnalyzers = antennaContext.getToolConfiguration().getWorkflow().getAnalyzers().getStep();
        assertThatStepsAreEqualUpToOrder(expectedAnalyzers, actualAnalyzers);
    }

    @Test
    public void checkParsedWorkflowForProcessors() {
        List<WorkflowStep> expectedProcessors = testData.getExpectedToolConfigurationProcessors();
        if(antennaContext.getToolConfiguration().getWorkflow().getProcessors() == null){
            assertEquals(0, expectedProcessors.size());
            return;
        }
        List<WorkflowStep> actualProcessors = antennaContext.getToolConfiguration().getWorkflow().getProcessors().getStep();
        assertThatStepsAreEqualUpToOrder(expectedProcessors, actualProcessors);
    }

    @Test
    public void checkParsedWorkflowForGenerators() {
        List<WorkflowStep> expectedGenerators = testData.getExpectedToolConfigurationGenerators();
        if(antennaContext.getToolConfiguration().getWorkflow().getGenerators() == null){
            assertEquals(0, expectedGenerators.size());
            return;
        }
        List<WorkflowStep> actualGenerators = antennaContext.getToolConfiguration().getWorkflow().getGenerators().getStep();
        assertThatStepsAreEqualUpToOrder(expectedGenerators, actualGenerators);
    }

    @Test
    public void checkParsedWorkflowForOutputHandlers() {
        List<WorkflowStep> expectedOutputHandlers = testData.getExpectedToolConfigurationOutputHandlers();
        if(antennaContext.getToolConfiguration().getWorkflow().getOutputHandlers() == null){
            assertEquals(0, expectedOutputHandlers.size());
            return;
        }
        List<WorkflowStep> actualOutputHandlers = antennaContext.getToolConfiguration().getWorkflow().getOutputHandlers().getStep();
        assertThatStepsAreEqualUpToOrder(expectedOutputHandlers, actualOutputHandlers);
    }

    @Test
    public void checkProxyPort() {
        assertEquals(testData.getExpectedProxyPort(), antennaContext.getToolConfiguration().getProxyPort());
    }

    @Test
    public void checkProxyHost() {
        assertEquals(testData.getExpectedProxyHost(), antennaContext.getToolConfiguration().getProxyHost());
    }

    @Test
    public void checkBooleans() {
        assertEquals(testData.getExpectedToolConfigurationMavenInstalled(), antennaContext.getToolConfiguration().isMavenInstalled());
        assertEquals(testData.getExpectedToolConfigurationAttachAll(), antennaContext.getToolConfiguration().isAttachAll());
        assertEquals(testData.getExpectedToolConfigurationSkip(), antennaContext.getToolConfiguration().isSkipAntennaExecution());
    }

    @Test
    public void testExecution()
            throws Exception {
        assumeTrue(runExecutionTest);
        protoypeExecutionTest(antennaFrontend::execute, AntennaFrontend::getOutputs);
    }

    protected void protoypeExecutionTest(RunnableWithExceptions executor, Function<AntennaFrontend,Map<String, IAttachable>> buildArtifactsGetter)
            throws Exception{
        assumeTrue("The test data " + testData.getClass().getSimpleName() + " is not executable", testData instanceof ExecutableTestProject);
        assumeToBeConnectedToTheInternet();

        executor.run();

        Path pathToTarget = testData.projectRoot.resolve("target");
        assertTrue(pathToTarget.toFile().exists());

        Map<String,IAttachable> buildArtifacts =  buildArtifactsGetter.apply(antennaFrontend);

        ((ExecutableTestProject)testData).assertExecutionResult(pathToTarget, buildArtifacts, antennaContext);
    }

    @FunctionalInterface
    public interface RunnableWithExceptions{
        void run() throws Exception;
    }
}