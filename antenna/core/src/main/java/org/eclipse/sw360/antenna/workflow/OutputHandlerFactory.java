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
package org.eclipse.sw360.antenna.workflow;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.workflow.AbstractOutputHandler;
import org.eclipse.sw360.antenna.api.workflow.ConfigurableWorkflowItem;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OutputHandlerFactory extends WorkflowItemFactory {
    public static List<AbstractOutputHandler> getOutputHandlers(Workflow workflow, AntennaContext context) {
        if (workflow.getOutputHandlers() == null) {
            return Collections.EMPTY_LIST;
        }

        List<WorkflowStep> outputHandlerRequested = workflow.getOutputHandlers().getStep();

        return outputHandlerRequested.stream().parallel()
                .filter(rg -> !Optional.ofNullable(rg.isDeactivated()).orElse(false))
                .map(rg -> {
                    LOGGER.debug("Loading the {} output handler", rg.getName());
                    return WorkflowItemFactory.<AbstractOutputHandler>buildWorkflowItem(rg, rg.getConfiguration(), context, rg.getWorkflowStepOrder());
                })
                .sorted(Comparator.comparingInt(ConfigurableWorkflowItem::getWorkflowStepOrder))
                .collect(Collectors.toList());
    }
}
