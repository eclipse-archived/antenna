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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.adapter.*;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

public class SW360MetaDataReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360MetaDataReceiver.class);

    // rest service adapters
    private SW360AuthenticationClient authenticationClient;
    private SW360ComponentClientAdapter componentClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;
    private SW360LicenseClientAdapter licenseClientAdapter;

    private String userId;
    private String password;

    public SW360MetaDataReceiver(String restServerUrl, String authServerUrl, String userId, String password) {
        this.userId = userId;
        this.password = password;

        authenticationClient = new SW360AuthenticationClient(authServerUrl);
        componentClientAdapter = new SW360ComponentClientAdapter(restServerUrl);
        releaseClientAdapter = new SW360ReleaseClientAdapter(restServerUrl);
        licenseClientAdapter = new SW360LicenseClientAdapter(restServerUrl);
    }

    public Optional<SW360Release> findReleaseForArtifact(Artifact artifact) throws AntennaException {
        HttpHeaders headers = createHttpsHeaders(userId, password);
        Optional<SW360Component> component = componentClientAdapter.getComponentByArtifact(artifact, headers);
        if (component.isPresent()) {
            return releaseClientAdapter.getReleaseByArtifact(component.get(), artifact, headers);
        }
        return Optional.empty();
    }

    public Optional<SW360License> getLicenseDetails(SW360SparseLicense sparseLicense) throws AntennaException {
        HttpHeaders headers = createHttpsHeaders(userId, password);
        return licenseClientAdapter.getLicenseDetails(sparseLicense, headers);
    }

    private HttpHeaders createHttpsHeaders(String userId, String password) throws AntennaException {
        return authenticationClient.getHeadersWithBearerToken(authenticationClient.getOAuth2AccessToken(userId, password));
    }
}
