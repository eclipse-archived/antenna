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

package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

public class SW360MetaDataReceiver {
    // rest service adapters
    private SW360AuthenticationClient authenticationClient;
    private SW360ComponentClientAdapter componentClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;
    private SW360LicenseClientAdapter licenseClientAdapter;

    private String userId;
    private String password;
    private String clientId;
    private String clientPassword;

    public SW360MetaDataReceiver(String restServerUrl, String authServerUrl, String userId, String password,
                                 String clientId, String clientPassword,
                                 boolean proxyEnable, String proxyHost, int proxyPort) {
        this.userId = userId;
        this.password = password;
        this.clientId = clientId;
        this.clientPassword = clientPassword;

        authenticationClient = new SW360AuthenticationClient(authServerUrl, proxyEnable, proxyHost, proxyPort);
        componentClientAdapter = new SW360ComponentClientAdapter(restServerUrl, proxyEnable, proxyHost, proxyPort);
        releaseClientAdapter = new SW360ReleaseClientAdapter(restServerUrl, proxyEnable, proxyHost, proxyPort);
        licenseClientAdapter = new SW360LicenseClientAdapter(restServerUrl, proxyEnable, proxyHost, proxyPort);
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
        return authenticationClient.getHeadersWithBearerToken(authenticationClient.getOAuth2AccessToken(userId, password, clientId, clientPassword));
    }
}
