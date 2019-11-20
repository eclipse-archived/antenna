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
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.ConfigurableWorkflowItem;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public abstract class WorkflowItemFactory {
    protected static final Logger LOGGER = LoggerFactory.getLogger(WorkflowItemFactory.class);

    protected static <T extends ConfigurableWorkflowItem> T buildWorkflowItem(WorkflowStep wfi, StepConfiguration stepConfiguration, AntennaContext context, Short stepOrderOveride) {
        Map<String, String> workflowitemConfig = Optional.ofNullable(stepConfiguration)
                .map(StepConfiguration::getAsMap)
                .orElse(Collections.emptyMap());

        String classHint = wfi.getClassHint().trim();
        String name = wfi.getName().trim();
        try {
            @SuppressWarnings("unchecked")
            Class<T> workflowitemClazz = (Class<T>) Class.forName(classHint);
            T instance = workflowitemClazz.newInstance();
            instance.setAntennaContext(context);
            instance.overrideStepOrder(stepOrderOveride);
            instance.configure(workflowitemConfig);
            LOGGER.debug("{} loaded and configured", name);
            return instance;
        } catch (ClassNotFoundException e) {
            throw new ExecutionException("Could not initialize workflow item [" + name + "], because the class could not be found, reason=[" + e.getMessage() + "]", e);
        } catch (InstantiationException e) {
            throw new ExecutionException("Could not initialize workflow item [" + name + "], because the class could not be instantiated, reason=[" + e.getMessage() + "]", e);
        } catch (LinkageError | IllegalAccessException | ConfigurationException e) {
            throw new ExecutionException("Could not initialize workflow item [" + name + "], reason=[" + e.getMessage() + "]", e);
        }
    }
}
