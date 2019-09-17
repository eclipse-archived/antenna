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
package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.workflow.processors.checkers.ConfigurationChecker;
import org.eclipse.sw360.antenna.workflow.processors.filter.ConfigurationHandlerOverride;
import org.eclipse.sw360.antenna.workflow.processors.filter.ConfigurationHandlerRemove;

import java.util.*;

public class AntennaConfHandler extends AbstractProcessor {

    private final List<AbstractProcessor> localProcessors = new ArrayList<>();

    public AntennaConfHandler() {
        this.workflowStepOrder = 0;
    }

    @Override
    public void configure(Map<String, String> configMap) {
        IProcessingReporter processingReporter = context.getProcessingReporter();

        //initConfigurationChecker
        ConfigurationChecker confCheck = new ConfigurationChecker(processingReporter, context.getConfiguration());
        localProcessors.add(confCheck);

        //initFilters
        ConfigurationHandlerOverride configurationHandlerOverride = new ConfigurationHandlerOverride(context);
        ConfigurationHandlerRemove configurationHandlerRemove = new ConfigurationHandlerRemove(context);
        localProcessors.addAll(Arrays.asList(configurationHandlerOverride, configurationHandlerRemove));
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) throws ExecutionException {
        for(AbstractProcessor processor : localProcessors) {
            intermediates = processor.process(intermediates);
        }
        return intermediates;
    }
}
