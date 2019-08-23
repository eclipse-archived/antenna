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
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentList;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.getSw360SparseComponents;

public class SW360ComponentClient extends SW360Client {
    private static final String COMPONENTS_ENDPOINT = "/components";
    private final String restUrl;

    public SW360ComponentClient(String restUrl, ProxySettings proxySettings) {
        super(proxySettings);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + COMPONENTS_ENDPOINT;
    }

    public SW360Component getComponent(String componentId, HttpHeaders header) throws AntennaException {
        ResponseEntity<Resource<SW360Component>> response = doRestGET(getEndpoint() + "/" + componentId, header,
                new ParameterizedTypeReference<Resource<SW360Component>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new AntennaException("Request to get component " + componentId + " returned empty body"))
                    .getContent();
        } else {
            throw new AntennaException("Request to get component " + componentId + " failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseComponent> getComponents(HttpHeaders header) throws AntennaException {
        ResponseEntity<Resource<SW360ComponentList>> response = doRestGET(getEndpoint(), header,
                new ParameterizedTypeReference<Resource<SW360ComponentList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return getSw360SparseComponents(response);
        } else {
            throw new AntennaException("Request to get all components failed with " + response.getStatusCode());
        }
    }

    public List<SW360SparseComponent> searchByName(String name, HttpHeaders header) throws AntennaException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(getEndpoint())
                .queryParam(SW360Attributes.COMPONENT_SEARCH_BY_NAME, name);

        ResponseEntity<Resource<SW360ComponentList>> response = doRestGET(builder.build(false).toUriString(), header,
                new ParameterizedTypeReference<Resource<SW360ComponentList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return getSw360SparseComponents(response);
        }
        else {
            return new ArrayList<>();
        }
    }

    public SW360Component createComponent(SW360Component sw360Component, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Component, header);

        ResponseEntity<Resource<SW360Component>> response;
        try {
            response = doRestPOST(getEndpoint(), httpEntity,
                new ParameterizedTypeReference<Resource<SW360Component>>() {});
        } catch(HttpServerErrorException e) {
            throw new AntennaException("Request to create component " + sw360Component.getName() + " failed with "
                    + e.getStatusCode());
        }

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new AntennaException("Body was null"))
                    .getContent();
        } else {
            throw new AntennaException("Request to create component " + sw360Component.getName() + " failed with "
                    + response.getStatusCode());
        }
    }
}
