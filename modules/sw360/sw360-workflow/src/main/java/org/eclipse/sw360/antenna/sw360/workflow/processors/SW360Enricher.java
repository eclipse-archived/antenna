/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.workflow.processors;

import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfigurationFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

public class SW360Enricher extends AbstractProcessor {
    private static final String DOWNLOAD_ATTACHMENTS = "download.attachments";
    private static final String DOWNLOAD_ATTACHMENTS_DIR="download.directory";
    private boolean downloadAttachments;
    private Path downloadPath;

    private SW360MetaDataReceiver connector;

    public SW360Enricher() {
        this.workflowStepOrder = 1300;
    }

    @Override
    public void configure(Map<String, String> configMap) {
        super.configure(configMap);

        reporter = context.getProcessingReporter();

        // Proxy configuration
        String sw360ProxyHost = context.getToolConfiguration().getProxyHost();
        int sw360ProxyPort = context.getToolConfiguration().getProxyPort();

        downloadAttachments = getBooleanConfigValue(DOWNLOAD_ATTACHMENTS, configMap);
        if (downloadAttachments) {
            downloadPath = Paths.get(getConfigValue(DOWNLOAD_ATTACHMENTS_DIR, configMap,
                    context.getToolConfiguration().getAntennaTargetDirectory().toString()))
                    .normalize()
                    .toAbsolutePath();
        }

        SW360ConnectionConfigurationFactory configurationFactory = new SW360ConnectionConfigurationFactory();
        SW360ConnectionConfiguration sw360ConnectionConfiguration =
                configurationFactory.createConfiguration(key -> getConfigValue(key, configMap),
                        key -> getBooleanConfigValue(key, configMap),
                        sw360ProxyHost, sw360ProxyPort);

        connector = new SW360MetaDataReceiver(sw360ConnectionConfiguration);
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        return new SW360EnricherImpl(reporter, connector, downloadAttachments, downloadPath).process(intermediates);
    }
}
