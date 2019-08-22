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

public abstract class ConfigurableWorkflowItem {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableWorkflowItem.class);
    protected IProcessingReporter reporter;
    protected AntennaContext context;
    protected short workflowStepOrder = 1000;
    protected final short VALIDATOR_BASE_ORDER = 10000;

    public void setAntennaContext(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    public String getWorkflowItemName() {
        return getClass().getName();
    }

    public void cleanup() {
        // NO OP
    }

    public void overrideStepOrder(Short override) {
        if(override != null && override >= 0) {
            workflowStepOrder = override;
        }
    }

    public void configure() throws AntennaConfigurationException {
        configure(Collections.emptyMap());
    }

    public short getWorkflowStepOrder() {
        return workflowStepOrder;
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

    public Boolean getBooleanConfigValue(String key, Map<String, String> configMap) throws AntennaConfigurationException {
        return "true".equals(getConfigValue(key, configMap, "false").toLowerCase());
    }

    public List<String> getCommaSeparatedConfigValue(String key, Map<String,String> configMap) throws  AntennaConfigurationException {
        final String configValue = getConfigValue(key, configMap, "");
        if ("".equals(configValue)) {
            return Collections.emptyList();
        }

        return Arrays.asList(configValue.split(","));
    }
}
