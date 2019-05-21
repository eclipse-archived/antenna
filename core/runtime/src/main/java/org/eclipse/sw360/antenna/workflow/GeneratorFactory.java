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

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.api.workflow.ConfigurableWorkflowItem;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeneratorFactory extends WorkflowItemFactory {

    public static List<AbstractGenerator> getGenerators(Workflow workflow, AntennaContext context) throws AntennaConfigurationException {
        if (workflow == null) {
            throw new AntennaConfigurationException("No <workflow> section was found in your configuration.");
        }

        if(workflow.getGenerators() == null) {
            return Collections.EMPTY_LIST;
        }

        List<WorkflowStep> generatorsRequested = workflow.getGenerators().getStep();
        if (generatorsRequested.size() == 0) {
            throw new AntennaConfigurationException("No generators were found in your <workflow> section.");
        }

        // Go through the list of generators requests and instantiate each one.
        return generatorsRequested.stream().parallel()
                .filter(rg -> !Optional.ofNullable(rg.isDeactivated()).orElse(false))
                .map(rg -> {
                    LOGGER.debug("Loading the {} generator", rg.getName());
                    return WorkflowItemFactory.<AbstractGenerator>buildWorkflowItem(rg, rg.getConfiguration(), context, rg.getWorkflowStepOrder());
                })
                .sorted(Comparator.comparingInt(ConfigurableWorkflowItem::getWorkflowStepOrder))
                .collect(Collectors.toList());
    }
}
