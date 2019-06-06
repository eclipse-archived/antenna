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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SW360ComponentClient {
    private static final String COMPONENTS_ENDPOINT = "/components";

    private final String componentsRestUrl;
    private RestTemplate restTemplate;

    public SW360ComponentClient(String restUrl, boolean proxyUse, String proxyHost, int proxyPort) {
        this.componentsRestUrl = restUrl + COMPONENTS_ENDPOINT;
        this.restTemplate = restTemplate(proxyUse, proxyHost, proxyPort);
    }

    private RestTemplate restTemplate(boolean proxyUse, String proxyHost, int proxyPort) {
        if (proxyUse && proxyHost != null) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
            return new RestTemplate(requestFactory);
        } else {
            return new RestTemplate();
        }
    }

    public SW360Component getComponent(String componentId, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360Component>> response =
                this.restTemplate.exchange(this.componentsRestUrl + "/" + componentId,
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360Component>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to get component " + componentId + " failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseComponent> getComponents(HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360ComponentList>> response =
                this.restTemplate.exchange(this.componentsRestUrl,
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360ComponentList>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            SW360ComponentList resource = response.getBody().getContent();
            if ((resource.get_Embedded() != null) && (resource.get_Embedded().getComponents() != null)) {
                return resource.get_Embedded().getComponents();
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new AntennaException("Request to get all components failed with " + response.getStatusCode());
        }
    }

    public List<SW360SparseComponent> searchByName(String name, HttpHeaders header) throws AntennaException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(componentsRestUrl)
                .queryParam(SW360Attributes.COMPONENT_SEARCH_BY_NAME, name);

        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360ComponentList>> response = this.restTemplate
                .exchange(builder.build(false).toUriString(),
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360ComponentList>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            SW360ComponentList resource = response.getBody().getContent();
            if ((resource.get_Embedded() != null) && (resource.get_Embedded().getComponents() != null)) {
                return resource.get_Embedded().getComponents();
            } else {
                return new ArrayList<>();
            }
        }
        else {
            return new ArrayList<>();
        }
    }

    public SW360Component createComponent(SW360Component sw360Component, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Component, header);

        ResponseEntity<Resource<SW360Component>> response;
        try {
            response = this.restTemplate
                    .exchange(this.componentsRestUrl,
                            HttpMethod.POST,
                            httpEntity,
                            new ParameterizedTypeReference<Resource<SW360Component>>() {});
        } catch(HttpServerErrorException e) {
            throw new AntennaException("Request to create component " + sw360Component.getName() + " failed with "
                    + e.getStatusCode());
        }

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to create component " + sw360Component.getName() + " failed with "
                    + response.getStatusCode());
        }
    }
}
