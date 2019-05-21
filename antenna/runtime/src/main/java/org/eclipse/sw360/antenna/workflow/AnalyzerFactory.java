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
import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.ConfigurableWorkflowItem;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Returns appropriate types of Analyzer depending on the user's configuration.
 */
public class AnalyzerFactory extends WorkflowItemFactory {

    public static List<AbstractAnalyzer> getAnalyzers(Workflow workflow, AntennaContext context) throws AntennaConfigurationException {
        if (workflow == null) {
            throw new AntennaConfigurationException("No <workflow> section was found in your configuration.");
        }

        if(workflow.getAnalyzers() == null) {
            return Collections.EMPTY_LIST;
        }

        List<WorkflowStep> analyzersRequested = workflow.getAnalyzers().getStep();

        // Go through the list of analyzer requests and instantiate each one.
        return analyzersRequested.stream()
                .filter(ar -> !Optional.ofNullable(ar.isDeactivated()).orElse(false))
                .map(ar -> {
                    LOGGER.debug("Loading the {} analyzer", ar.getName());
                    return WorkflowItemFactory.<AbstractAnalyzer>buildWorkflowItem(ar, ar.getConfiguration(), context, ar.getWorkflowStepOrder());
                })
                .sorted(Comparator.comparingInt(ConfigurableWorkflowItem::getWorkflowStepOrder))
                .collect(Collectors.toList());
    }
}
