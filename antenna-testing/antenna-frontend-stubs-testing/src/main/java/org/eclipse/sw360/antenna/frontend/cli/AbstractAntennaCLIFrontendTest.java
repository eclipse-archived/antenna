/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.cli;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.frontend.AbstractAntennaFrontendTest;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProjectWithExpectations;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


public abstract class AbstractAntennaCLIFrontendTest extends AbstractAntennaFrontendTest {

    public AbstractAntennaCLIFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name) {
        super(testDataSupplier, name);
    }

    public abstract AbstractAntennaCLIFrontend getAntennaFrontend(File pomFile) throws Exception;

    @Before
    public void loadTestContext() throws Exception {
        File pomFile = testData.getProjectPom().toFile();

        antennaFrontend = getAntennaFrontend(pomFile);
        assertNotNull(antennaFrontend);

        antennaContext = antennaFrontend.init().buildAntennaContext();
        assertNotNull(antennaContext);
        assertNotNull(antennaContext.getProject());

        runExecutionTest = !testData.requiresMaven();
    }

    @Ignore("would fail due to unrelated templating issues")
    @Override
    public void checkParsedWorkflowForOutputHandlers() {
    }


    @Test
    public void checkForUnrederedXMLproperties() {
        ToolConfiguration toolconfig = new AntennaCLISettingsReader("antenna-maven-plugin")
                .readSettingsToToolConfiguration((DefaultProject) antennaContext.getProject());

        Workflow workflow = toolconfig.getWorkflow();
        List<StepConfiguration.Entry> all = new ArrayList<>();

        if(workflow.getAnalyzers() != null) {
            List<WorkflowStep> analyzers = workflow.getAnalyzers().getStep();
            analyzers.forEach(step -> all.addAll(getEntriesOfStep(step)));
        }

        if(workflow.getProcessors() != null) {
            List<WorkflowStep> processors = workflow.getProcessors().getStep();
            processors.forEach(step -> all.addAll(getEntriesOfStep(step)));
        }

        if(workflow.getGenerators() != null) {
            List<WorkflowStep> generators = workflow.getGenerators().getStep();
            generators.forEach(step -> all.addAll(getEntriesOfStep(step)));
        }

        if(workflow.getOutputHandlers() != null) {
            List<WorkflowStep> outputHandlers = workflow.getOutputHandlers().getStep();
            outputHandlers.forEach(step -> all.addAll(getEntriesOfStep(step)));
        }

        assertEquals(0, all.stream()
                .filter(this::entryHasUnrenderedProperties)
                .collect(Collectors.toList()).size());
    }

    @Test
    public void checkAntennaContextProjectResolution() {
        assertFalse(((DefaultProject) antennaContext.getProject()).getBuild().getDirectory().contains("${"));
        assertFalse(((DefaultProject) antennaContext.getProject()).getBuild().getOutputDirectory().contains("${"));
        assertFalse((antennaContext.getProject()).getBasedir().toString().contains("${"));
        assertFalse(antennaContext.getProject().getVersion().contains("${"));
    }

    private List<StepConfiguration.Entry> getEntriesOfStep(WorkflowStep step) {
        if(step.getConfiguration() != null) {
            return step.getConfiguration().getEntry();
        }
        else {
            return Collections.emptyList();
        }
    }

    private boolean entryHasUnrenderedProperties(StepConfiguration.Entry entry) {
        final String nono = String.format("(.*)\\$\\{(.*)");

        if(entry.getValue() != null && entry.getValue().matches(nono)) {
            return true;
        } else {
            return entry.getEntryValue() != null && entry.getEntryValue().matches(nono);
        }
    }
}