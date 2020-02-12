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

import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentList;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.sw360.utils.SW360ClientException;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.utils.SW360ClientUtils.*;

public class SW360ComponentClient extends SW360Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360ComponentClient.class);
    private static final String COMPONENTS_ENDPOINT = "/components";
    private final String restUrl;

    public SW360ComponentClient(String restUrl, RestTemplate template) {
        super(template);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + COMPONENTS_ENDPOINT;
    }

    public Optional<SW360Component> getComponent(String componentId, HttpHeaders header) {
        try {
            ResponseEntity<Resource<SW360Component>> response = doRestGET(getEndpoint() + "/" + componentId, header,
                    new ParameterizedTypeReference<Resource<SW360Component>>() {});

            checkRestStatus(response);
            return Optional.of(getSaveOrThrow(response.getBody(), Resource::getContent));
        } catch (SW360ClientException e) {
            LOGGER.error("Request to get component {} failed with {}",
                    componentId, e.getMessage());
            return Optional.empty();
        }
    }

    public List<SW360SparseComponent> getComponents(HttpHeaders header) {
        try {
            ResponseEntity<Resource<SW360ComponentList>> response = doRestGET(getEndpoint(), header,
                    new ParameterizedTypeReference<Resource<SW360ComponentList>>() {
                    });

            checkRestStatus(response);
            return getSw360SparseComponents(response);
        } catch (SW360ClientException e) {
            LOGGER.error("Request to get components failed with {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SW360SparseComponent> searchByName(String name, HttpHeaders header) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(getEndpoint())
                    .queryParam(SW360Attributes.COMPONENT_SEARCH_BY_NAME, name);

            ResponseEntity<Resource<SW360ComponentList>> response = doRestGET(builder.build(false).toUriString(), header,
                    new ParameterizedTypeReference<Resource<SW360ComponentList>>() {});

            return getSw360SparseComponents(response);
        } catch (SW360ClientException e) {
            LOGGER.debug("Request to get sparse components failed with {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public SW360Component createComponent(SW360Component sw360Component, HttpHeaders header) {
        try {
            HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Component, header);

            ResponseEntity<Resource<SW360Component>> response = doRestPOST(getEndpoint(), httpEntity,
                    new ParameterizedTypeReference<Resource<SW360Component>>() {});

            checkRestStatus(response);
            return getSaveOrThrow(response.getBody(), Resource::getContent);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("Request to create component {} failed with {}",
                    sw360Component.getName(), e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return sw360Component;
        } catch (SW360ClientException e) {
            LOGGER.error("Request to create component {} failed with {}",
                    sw360Component.getName(), e.getMessage());
            return sw360Component;
        }
    }
}
