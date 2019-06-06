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
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.SW360ProjectCoordinates;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;

import java.io.IOException;
import java.util.*;

public class SW360Updater extends AbstractGenerator {
    private static final String REST_SERVER_URL_KEY = "rest.server.url";
    private static final String AUTH_SERVER_URL_KEY = "auth.server.url";
    private static final String USERNAME_KEY = "user.id";
    private static final String PASSWORD_KEY = "user.password";
    private static final String CLIENT_USER_KEY = "client.id";
    private static final String CLIENT_PASSWORD_KEY = "client.password";
    private static final String PROXY_USE = "proxy.use";

    private String sw360RestServerUrl;
    private String sw360AuthServerUrl;
    private String sw360User;
    private String sw360Password;
    private String sw360ClientId;
    private String sw360ClientPassword;
    private String sw360ProxyHost;
    private int sw360ProxyPort;
    private boolean sw360ProxyUse;

    private String projectName;
    private String projectVersion;

    public SW360Updater() {
        this.workflowStepOrder = 1100;
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        SW360ProjectCoordinates configuredSW360Project = context.getConfiguration().getConfiguredSW360Project();
        projectName = configuredSW360Project.getName();
        projectVersion = configuredSW360Project.getVersion();

        if (configuredSW360Project.getName() == null || configuredSW360Project.getName().isEmpty()) {
            projectName = context.getProject().getProjectId();
        }
        if (configuredSW360Project.getVersion() == null || configuredSW360Project.getVersion().isEmpty()) {
            projectVersion = context.getProject().getVersion();
        }

        sw360RestServerUrl = getConfigValue(REST_SERVER_URL_KEY, configMap);
        sw360AuthServerUrl = getConfigValue(AUTH_SERVER_URL_KEY, configMap);
        sw360User = getConfigValue(USERNAME_KEY, configMap);
        sw360Password = getConfigValue(PASSWORD_KEY, configMap);
        sw360ClientId = getConfigValue(CLIENT_USER_KEY, configMap);
        sw360ClientPassword = getConfigValue(CLIENT_PASSWORD_KEY, configMap);
        sw360ProxyUse = Boolean.parseBoolean(getConfigValue(PROXY_USE, configMap, "false"));
        sw360ProxyHost = context.getToolConfiguration().getProxyHost();
        sw360ProxyPort = context.getToolConfiguration().getProxyPort();
    }

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> intermediates) throws AntennaException {
        SW360MetaDataUpdater sw360MetaDataUpdater = new SW360MetaDataUpdater(sw360RestServerUrl, sw360AuthServerUrl, sw360User,
                sw360Password, sw360ClientId, sw360ClientPassword,
                sw360ProxyUse, sw360ProxyHost, sw360ProxyPort);

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
}
