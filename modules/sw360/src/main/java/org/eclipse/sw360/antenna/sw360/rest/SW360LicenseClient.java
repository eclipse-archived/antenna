/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Verifa Oy 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360LicenseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.checkRestStatus;
import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.getSaveOrThrow;

public class SW360LicenseClient extends SW360Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360LicenseClient.class);
    private static final String LICENSES_ENDPOINT = "/licenses";
    private final String restUrl;

    public SW360LicenseClient(String restUrl, RestTemplate template) {
        super(template);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + LICENSES_ENDPOINT;
    }

    public List<SW360SparseLicense> getLicenses(HttpHeaders header) {
        try {
            ResponseEntity<Resource<SW360LicenseList>> response = doRestGET(getEndpoint(), header,
                    new ParameterizedTypeReference<Resource<SW360LicenseList>>() {});

            checkRestStatus(response);
            SW360LicenseList resource = getSaveOrThrow(response.getBody(), Resource::getContent);
            if (resource.get_Embedded() != null &&
                    resource.get_Embedded().getLicenses() != null) {
                return new ArrayList<>(resource.get_Embedded().getLicenses());
            } else {
                return new ArrayList<>();
            }
        } catch (ExecutionException e) {
            LOGGER.debug("Request to get all licenses failed with {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<SW360License> getLicenseByName(String name, HttpHeaders header) {
        try {
            ResponseEntity<Resource<SW360License>> response = doRestGET(getEndpoint() + "/" + name, header,
                    new ParameterizedTypeReference<Resource<SW360License>>() {});

            checkRestStatus(response);
            return Optional.of(getSaveOrThrow(response.getBody(), Resource::getContent));
        } catch (ExecutionException e) {
            LOGGER.debug("Request to get license {} failed with {}",
                    name, e.getMessage());
            return Optional.empty();
        }
    }

    public SW360License createLicense(SW360License sw360License, HttpHeaders header) {
        try {
            HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360License, header);
            ResponseEntity<Resource<SW360License>> response = doRestPOST(getEndpoint(), httpEntity,
                new ParameterizedTypeReference<Resource<SW360License>>() {});

            checkRestStatus(response);
            return getSaveOrThrow(response.getBody(), Resource::getContent);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("Request to create license {} failed with {}",
                    sw360License.getFullName(), e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return sw360License;
        } catch (ExecutionException e) {
            LOGGER.error("Request to create license {} failed with {}",
                    sw360License.getFullName(), e.getMessage());
            return sw360License;
        }
    }
}
