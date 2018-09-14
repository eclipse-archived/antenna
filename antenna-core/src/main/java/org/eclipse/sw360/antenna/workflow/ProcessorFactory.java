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

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;

import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns appropriate types of Processors depending on the user's configuration.
 */
public class ProcessorFactory extends WorkflowItemFactory {

    public static List<AbstractProcessor> getProcessors(Workflow workflow, AntennaContext context)  throws AntennaConfigurationException {
        if (workflow == null) {
            throw new AntennaConfigurationException("No <workflow> section was found in your configuration.");
        }

        if(workflow.getProcessors() == null) {
            return Collections.EMPTY_LIST;
        }

        List<WorkflowStep> processorsRequested = workflow.getProcessors().getStep();
        if (processorsRequested.size() == 0) {
            LOGGER.warn("No processors were found in your <workflow> section.");
        }

        // Go through the list of processors requests and instantiate each one.
        return processorsRequested.stream().parallel()
                .filter(pr -> ! pr.isDeactivated())
                .map(pr -> {
                    LOGGER.debug("Loading the {} processor", pr.getName());
                    return WorkflowItemFactory.<AbstractProcessor>buildWorkflowItem(pr, pr.getConfiguration(), context);
                })
                .collect(Collectors.toList());
    }
}