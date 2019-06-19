/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Verifa Oy 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360LicenseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

public class SW360LicenseClient extends SW360Client {
    private static final String LICENSES_ENDPOINT = "/licenses";

    private String licensesRestUrl;

    public SW360LicenseClient(String restUrl, boolean proxyUse, String proxyHost, int proxyPort) {
        super(proxyUse, proxyHost, proxyPort);
        licensesRestUrl = restUrl + LICENSES_ENDPOINT;
    }

    public List<SW360SparseLicense> getLicenses(HttpHeaders header) throws AntennaException {
        ResponseEntity<Resource<SW360LicenseList>> response = doRestGET(this.licensesRestUrl, header,
                new ParameterizedTypeReference<Resource<SW360LicenseList>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            SW360LicenseList resource = response.getBody().getContent();
            if (resource.get_Embedded() != null &&
                    resource.get_Embedded().getLicenses() != null) {
                return resource.get_Embedded().getLicenses();
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new AntennaException("Request to get all licenses failed with " + response.getStatusCode());
        }
    }

    public SW360License getLicenseByName(String name, HttpHeaders header) throws AntennaException {
        ResponseEntity<Resource<SW360License>> response = doRestGET(this.licensesRestUrl + "/" + name, header,
                new ParameterizedTypeReference<Resource<SW360License>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to get license " + name + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360License createLicense(SW360License sw360License, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360License, header);
        ResponseEntity<Resource<SW360License>> response;
        try {
            response = doRestPOST(this.licensesRestUrl, httpEntity,
                new ParameterizedTypeReference<Resource<SW360License>>() {});
        } catch (HttpClientErrorException e) {
            throw new AntennaException("Request to create license " + sw360License.getFullName() + " failed with "
                    + e.getStatusCode());
        }

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to create license " + sw360License.getFullName() + " failed with "
                    + response.getStatusCode());
        }
    }
}
