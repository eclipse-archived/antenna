/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
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
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.SW360ProjectCoordinates;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;

import java.io.IOException;
import java.util.*;

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
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
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
            throw new AntennaExecutionException("The functionality to update releases, activated with the " + UPDATE_RELEASES + " is not yet supported.");
        }
        Boolean uploadSources = getBooleanConfigValue(UPLOAD_SOURCES, configMap);

        sw360MetaDataUpdater = new SW360MetaDataUpdater(sw360ConnectionConfiguration, updateReleases, uploadSources);
    }

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> intermediates) throws AntennaException {

        try {
            List<SW360Release> releases = new ArrayList<>();
            for (Artifact artifact : intermediates) {
                Set<String> licenses = sw360MetaDataUpdater.getOrCreateLicenses(artifact);
                SW360Component component = sw360MetaDataUpdater.getOrCreateComponent(artifact);
                releases.add(sw360MetaDataUpdater.getOrCreateRelease(artifact, licenses, component));
            }
            sw360MetaDataUpdater.createProject(projectName, projectVersion, releases);
        } catch (IOException e) {
            throw new AntennaException("Problem occurred during updating SW360.", e);
        }
        return Collections.emptyMap();
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
