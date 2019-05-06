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
package org.eclipse.sw360.antenna.workflow;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.*;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class AntennaWorkflowConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaWorkflowConfiguration.class);

    private final List<AbstractAnalyzer> analyzers;
    private final List<AbstractProcessor> processors;
    private final List<AbstractGenerator> generators;
    private final List<AbstractOutputHandler> outputHandlers;

    public AntennaWorkflowConfiguration(AntennaContext context) throws AntennaConfigurationException {
        LOGGER.info("Initializing workflow configuration ...");
        Workflow workflow = context.getToolConfiguration().getWorkflow();

        if (workflow == null) {
            throw new AntennaConfigurationException("No workflow was found in configuration.");
        }

        // Initialize analyzers
        analyzers = AnalyzerFactory.getAnalyzers(workflow, context);
        if (analyzers.isEmpty()) {
            LOGGER.warn("No analyzers found. You might want to check your configuration.");
        }

        // initialize processors
        processors = ProcessorFactory.getProcessors(workflow, context);
        if (processors.isEmpty()) {
            LOGGER.warn("No processors found. You might want to check your configuration.");
        }
        debugLogAndWarnOnDuplicateOrders("processors", processors);

        // initialize generators
        generators = GeneratorFactory.getGenerators(workflow, context);
        if (generators.isEmpty()) {
            LOGGER.warn("No generators found. You might want to check your configuration.");
        }

        outputHandlers = OutputHandlerFactory.getOutputHandlers(workflow, context);

        LOGGER.info("Initializing workflow configuration done");
    }

    public Collection<AbstractAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public Collection<AbstractProcessor> getProcessors() {
        return processors;
    }

    public Collection<AbstractGenerator> getGenerators() {
        return generators;
    }

    public List<AbstractOutputHandler> getOutputHandlers() {
        return outputHandlers;
    }

    private void debugLogAndWarnOnDuplicateOrders(String nameOfSteps, List<? extends ConfigurableWorkflowItem> items) {
        LOGGER.info("The calculated order of " + nameOfSteps + " is:");
        for (int i = 0; i < items.size() ; i++) {
            LOGGER.info("    - " + items.get(i).getWorkflowStepOrder() + ": " + items.get(i).getWorkflowItemName());
            if (i > 0 && items.get(i - 1).getWorkflowStepOrder() == items.get(i).getWorkflowStepOrder()) {
                LOGGER.warn("Workflow steps " + items.get(i - 1).getWorkflowItemName() + " and " + items.get(i).getWorkflowItemName() + " have the same step order. The former will be executed before the latter.");
            }
        }
    }
}
