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
package org.eclipse.sw360.antenna.workflow;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;
import org.eclipse.sw360.antenna.xml.XMLValidator;
import org.eclipse.sw360.antenna.util.TemplateRenderer;
import org.eclipse.sw360.antenna.util.XmlSettingsReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class WorkflowFileLoader {

    public static Workflow loadWorkflowFromClassPath(Optional<File> workflowOverride, TemplateRenderer tr) throws AntennaConfigurationException {
        Workflow workflow = new Workflow();
        Optional<String> renderedWorkflow = tr.renderClassPathWorkflow();
        if(renderedWorkflow.isPresent()) {
            Workflow workflowFromClasspath = loadRenderedWorkflow(renderedWorkflow.get());
            overrideWorkflow(workflow, workflowFromClasspath);
        }
        if(workflowOverride.isPresent()){
            validateWorkflowByXSD(workflowOverride.get());

            String renderedWorkflowOverride = tr.renderTemplateFile(workflowOverride.get());
            Workflow workflowFromOverride = loadRenderedWorkflow(renderedWorkflowOverride);
            overrideWorkflow(workflow, workflowFromOverride);
        }
        return workflow;
    }

    public static Workflow loadRenderedWorkflow(String renderedWorkflow) {
        try {
            XmlSettingsReader workflowReader = new XmlSettingsReader(renderedWorkflow);
            return workflowReader.getComplexType("workflow", Workflow.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("IO exception when reading the workflow definition: " + e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalArgumentException("Problem parsing the workflow definition: " + e.getMessage());
        }
    }

    public static void overrideWorkflow(Workflow workflowFromClasspath, Workflow workflowFromOverride) {
        if (workflowFromOverride.getAnalyzers() != null) {
            if (workflowFromClasspath.getAnalyzers() == null) {
                workflowFromClasspath.setAnalyzers(new Workflow.Analyzers());
            }
            overrideWorkflowItems(workflowFromClasspath, workflowFromOverride, w -> w.getAnalyzers().getStep());
        }
        if (workflowFromOverride.getProcessors() != null) {
            if (workflowFromClasspath.getProcessors() == null) {
                workflowFromClasspath.setProcessors(new Workflow.Processors());
            }
            overrideWorkflowItems(workflowFromClasspath, workflowFromOverride, w -> w.getProcessors().getStep());
        }
        if (workflowFromOverride.getGenerators() != null) {
            if (workflowFromClasspath.getGenerators() == null) {
                workflowFromClasspath.setGenerators(new Workflow.Generators());
            }
            overrideWorkflowItems(workflowFromClasspath, workflowFromOverride, w -> w.getGenerators().getStep());
        }
        if (workflowFromOverride.getOutputHandlers() != null) {
            if (workflowFromClasspath.getOutputHandlers() == null) {
                workflowFromClasspath.setOutputHandlers(new Workflow.OutputHandlers());
            }
            overrideWorkflowItems(workflowFromClasspath, workflowFromOverride, w -> w.getOutputHandlers().getStep());
        }
    }

    private static <T> void overrideWorkflowItems(Workflow workflowFromClasspath,
                                              Workflow workflowFromOverride,
                                              Function<Workflow,List<WorkflowStep>> getter) {
        List<WorkflowStep> steps = getter.apply(workflowFromClasspath);
        List<WorkflowStep> stepsOverrides = getter.apply(workflowFromOverride);
        if(stepsOverrides == null || stepsOverrides.size() == 0){
            return;
        }
        stepsOverrides.forEach(override -> {
            Optional<WorkflowStep> actual = steps.stream()
                    .filter(a -> override.getName().equals(a.getName()))
                    .findAny();
            if (actual.isPresent()) {
                overrideStep(actual.get(), override);
            } else {
                steps.add(override);
            }
        });
    }

    private static <T extends WorkflowStep> void overrideWorkflowitem(T actual, T override) {
        if(override.getClassHint() != null && !"".equals(override.getClassHint())){
            actual.setClassHint(override.getClassHint());
        }
    }

    private static void overrideStep(WorkflowStep actual, WorkflowStep override) {
        overrideWorkflowitem(actual, override);
        StepConfiguration overrideConfiguration = override.getConfiguration();
        StepConfiguration actualConfiguration = actual.getConfiguration();
        actual.setConfiguration(overrideItemConfiguration(actualConfiguration, overrideConfiguration));
        actual.setDeactivated(Optional.ofNullable(override.isDeactivated()).orElse(false));
    }

    private static StepConfiguration overrideItemConfiguration(StepConfiguration actual, StepConfiguration override) {
        if (override == null) {
            return actual;
        }
        if(actual == null) {
            return override;
        }

        actual.getEntry().addAll(override.getEntry());
        return actual;
    }

    private static void validateWorkflowByXSD(File workflowOverride) throws AntennaConfigurationException {
        new XMLValidator().
                validateXML(workflowOverride, XmlSettingsReader.class.getClassLoader().
                        getResource("workflow.xsd"));

    }
}
