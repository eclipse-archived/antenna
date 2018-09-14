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

package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360UserClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Optional;

public class SW360Connector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Connector.class);

    // rest service adapters
    private SW360AuthenticationClient authenticationClient;
    private SW360ProjectClientAdapter projectClientAdapter;
    private SW360UserClientAdapter userClientAdapter;

    private String restServerUrl;
    private String authServerUrl;
    private String userId;
    private String password;

    public SW360Connector(String restServerUrl, String authServerUrl, String userId, String password) {
        this.restServerUrl = restServerUrl;
        this.authServerUrl = authServerUrl;
        this.userId = userId;
        this.password = password;

        authenticationClient = new SW360AuthenticationClient(authServerUrl);
        projectClientAdapter = new SW360ProjectClientAdapter(restServerUrl);
        userClientAdapter = new SW360UserClientAdapter(restServerUrl);
    }

    public void updateSW360Project(IProject project) throws AntennaException, IOException {
        String authToken = authenticationClient.getOAuth2AccessToken(userId, password);
        HttpHeaders header = authenticationClient.getHeadersWithBearerToken(authToken);
        SW360User user = userClientAdapter.getUserById(userId, header);

        Optional<String> projectId = projectClientAdapter.getProjectIdByNameAndVersion(project, header);
        if (projectId.isPresent()) {
            LOGGER.debug("Could not update project " + projectId.get() + ", because the endpoint is not available.");
            // TODO: Update project on sw360
            // Needs endpoint on sw360
        } else {
            // TODO: return projectId
            // projectId = projectClientAdapter.addProject(appId, projectName, projectVersion, user, header);
            projectClientAdapter.addProject(project, user, header);
        }
    }
}
