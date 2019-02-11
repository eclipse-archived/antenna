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
package org.eclipse.sw360.antenna.api.workflow;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ConfigurableWorkflowItem implements IWorkflowable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableWorkflowItem.class);
    protected IProcessingReporter reporter;
    protected AntennaContext context;

    public void setAntennaContext(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    public void configure() throws AntennaConfigurationException {
        configure(Collections.emptyMap());
    }

    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {

    }

    public String getConfigValue(String key, Map<String, String> configMap) throws AntennaConfigurationException {
        return getConfigValue(key, configMap, null);
    }

    public String getConfigValue(String key, Map<String, String> configMap, String defaultValue) throws AntennaConfigurationException {
        String result = configMap.get(key);
        if (result == null && defaultValue != null) {
            return defaultValue;
        } else if (result == null) {
            String error = String.format("%s misconfigured. \"%s\" not supplied", getWorkflowItemName(), key);
            LOGGER.error(error);
            throw new AntennaConfigurationException(error);
        }
        return result;
    }

    public List<String> getCommaSeparatedConfigValue(String key, Map<String,String> configMap) throws  AntennaConfigurationException {
        final String configValue = getConfigValue(key, configMap, "");
        if ("".equals(configValue)) {
            return Collections.emptyList();
        }

        return Arrays.asList(configValue.split(","));
    }
}
