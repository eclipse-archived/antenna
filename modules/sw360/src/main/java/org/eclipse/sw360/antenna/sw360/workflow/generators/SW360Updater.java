/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.workflow.generators;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.SW360ProjectCoordinates;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SW360Updater extends AbstractGenerator {
    private static final String UPDATE_RELEASES = "update_releases";
    private static final String UPLOAD_SOURCES = "upload_sources";

    private String projectName;
    private String projectVersion;
    private SW360MetaDataUpdater sw360MetaDataUpdater;

    public SW360Updater() {
        this.workflowStepOrder = 1100;
    }

    @Override
    public void configure(Map<String, String> configMap) {
        Optional<SW360ProjectCoordinates> configuredSW360Project = Optional.ofNullable(context.getConfiguration().getConfiguredSW360Project());

        projectName = retrieveName(configuredSW360Project);
        projectVersion = retrieveVersion(configuredSW360Project);

        // Proxy configuration
        final String sw360ProxyHost = context.getToolConfiguration().getProxyHost();
        final int sw360ProxyPort = context.getToolConfiguration().getProxyPort();

        SW360ConnectionConfiguration sw360ConnectionConfiguration = new SW360ConnectionConfiguration(key -> getConfigValue(key, configMap),
                key -> getBooleanConfigValue(key, configMap),
                sw360ProxyHost, sw360ProxyPort);

        // General configuration
        final boolean updateReleases = getBooleanConfigValue(UPDATE_RELEASES, configMap);
        if (updateReleases) {
            throw new ExecutionException("The functionality to update releases, activated with the " + UPDATE_RELEASES + " is not yet supported.");
        }
        Boolean uploadSources = getBooleanConfigValue(UPLOAD_SOURCES, configMap);

        sw360MetaDataUpdater = new SW360MetaDataUpdater(sw360ConnectionConfiguration, updateReleases, uploadSources);
    }

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> intermediates) {
        return new SW360UpdaterImpl(sw360MetaDataUpdater,projectName, projectVersion)
                .produce(intermediates);
    }

    private String retrieveName(Optional<SW360ProjectCoordinates> sw360ProjectCoordinates) {
        return sw360ProjectCoordinates.map(SW360ProjectCoordinates::getName)
                .orElse(context.getProject()
                        .getProjectId());
    }

    private String retrieveVersion(Optional<SW360ProjectCoordinates> sw360ProjectCoordinates) {
        return sw360ProjectCoordinates.map(SW360ProjectCoordinates::getVersion)
                .orElse(context.getProject()
                        .getVersion());
    }

}
