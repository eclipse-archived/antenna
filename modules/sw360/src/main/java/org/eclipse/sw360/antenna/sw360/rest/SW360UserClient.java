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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;

import java.util.Optional;

public class SW360UserClient extends SW360Client {
    private static final String USERS_ENDPOINT = "/users";
    private final String restUrl;

    public SW360UserClient(String restUrl, boolean proxyUse, String proxyHost, int proxyPort) {
        super(proxyUse, proxyHost, proxyPort);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + USERS_ENDPOINT;
    }

    public SW360User getUserByEmail(String email, HttpHeaders header) throws AntennaException {
        ResponseEntity<Resource<SW360User>> response = doRestGET(getEndpoint() + "/" + email, header,
                new ParameterizedTypeReference<Resource<SW360User>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new AntennaException("Body was null"))
                    .getContent();
        }
        else {
            throw new AntennaException("Request to get user " + email + " failed with "
                    + response.getStatusCode());
        }
    }
}
