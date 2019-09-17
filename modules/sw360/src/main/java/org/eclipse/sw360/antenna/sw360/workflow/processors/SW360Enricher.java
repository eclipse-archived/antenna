/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.workflow.processors;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;

import java.util.Collection;
import java.util.Map;

public class SW360Enricher extends AbstractProcessor {
    private IProcessingReporter reporter;

    private SW360MetaDataReceiver connector;

    public SW360Enricher() {
        this.workflowStepOrder = 1300;
    }

    @Override
    public void configure(Map<String, String> configMap) throws ConfigurationException {
        super.configure(configMap);

        reporter = context.getProcessingReporter();

        // Proxy configuration
        String sw360ProxyHost = context.getToolConfiguration().getProxyHost();
        int sw360ProxyPort = context.getToolConfiguration().getProxyPort();

        SW360ConnectionConfiguration sw360ConnectionConfiguration = new SW360ConnectionConfiguration(key -> getConfigValue(key, configMap),
                key -> getBooleanConfigValue(key, configMap),
                sw360ProxyHost, sw360ProxyPort);

        connector = new SW360MetaDataReceiver(sw360ConnectionConfiguration);
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) throws ExecutionException {
        return new SW360EnricherImpl(reporter, connector).process(intermediates);
    }
}
