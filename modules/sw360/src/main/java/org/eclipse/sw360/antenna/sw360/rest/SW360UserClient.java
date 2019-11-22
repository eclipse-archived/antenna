/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
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

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.checkRestStatus;
import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.getSaveOrThrow;

public class SW360UserClient extends SW360Client {
    private static final String USERS_ENDPOINT = "/users";
    private final String restUrl;

    public SW360UserClient(String restUrl, ProxySettings proxySettings) {
        super(proxySettings);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + USERS_ENDPOINT;
    }

    public SW360User getUserByEmail(String email, HttpHeaders header) {
        try {
            ResponseEntity<Resource<SW360User>> response = doRestGET(getEndpoint() + "/" + email, header,
                    new ParameterizedTypeReference<Resource<SW360User>>() {});

            checkRestStatus(response);
            return getSaveOrThrow(response.getBody(), Resource::getContent);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ExecutionException("Request to get user " + email + " failed with "
                    + e.getStatusCode());
        }
    }
}
