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
import org.eclipse.sw360.antenna.sw360.client.api.SW360Connection;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfigurationFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

public class SW360Enricher extends AbstractProcessor {
    private static final String DOWNLOAD_ATTACHMENTS = "download.attachments";
    private static final String DOWNLOAD_ATTACHMENTS_DIR = "download.directory";

    private final SW360ConnectionConfigurationFactory connectionFactory;

    private boolean downloadAttachments;
    private Path downloadPath;

    private SW360MetaDataReceiver connector;

    public SW360Enricher() {
        this(new SW360ConnectionConfigurationFactory());
    }

    /**
     * Creates a new instance of {@code SW360Enricher} and sets the factory for
     * creating the SW360 connection. This constructor is used for testing
     * purposes.
     *
     * @param factory the factory for the SW360 connection
     */
    SW360Enricher(SW360ConnectionConfigurationFactory factory) {
        connectionFactory = factory;
        this.workflowStepOrder = 1300;
    }

    @Override
    public void configure(Map<String, String> configMap) {
        super.configure(configMap);

        reporter = context.getProcessingReporter();

        downloadAttachments = getBooleanConfigValue(DOWNLOAD_ATTACHMENTS, configMap);
        if (downloadAttachments) {
            downloadPath = Paths.get(getConfigValue(DOWNLOAD_ATTACHMENTS_DIR, configMap,
                    context.getToolConfiguration().getAntennaTargetDirectory().toString()))
                    .normalize()
                    .toAbsolutePath();
        }

        connector = createMetaDataReceiver(configMap);
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        return new SW360EnricherImpl(reporter, connector, downloadAttachments, downloadPath).process(intermediates);
    }

    SW360MetaDataReceiver createMetaDataReceiver(Map<String, String> configMap) {
        SW360Connection connection =
                getConnectionFactory().createConnection(key -> getConfigValue(key, configMap),
                        context.getHttpClient(), context.getObjectMapper());
        return new SW360MetaDataReceiver(connection);
    }

    SW360ConnectionConfigurationFactory getConnectionFactory() {
        return connectionFactory;
    }
}
