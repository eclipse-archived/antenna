/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
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
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SW360ReleaseClient {
    private static final String RELEASES_ENDPOINT = "/releases";

    private RestTemplate restTemplate = new RestTemplate();

    private String releasesRestUrl;

    public SW360ReleaseClient(String restUrl) { releasesRestUrl = restUrl + RELEASES_ENDPOINT; }

    public SW360Release getRelease(String releaseId, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360Release>> response =
                this.restTemplate.exchange(this.releasesRestUrl + "/" + releaseId,
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360Release>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to get release " + releaseId + " failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseRelease> getReleases(HttpHeaders header) throws IOException, AntennaException {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360ReleaseList>> response =
                this.restTemplate.exchange(this.releasesRestUrl,
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360ReleaseList>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            SW360ReleaseList resource = response.getBody().getContent();
            if ((resource.get_Embedded() != null) && (resource.get_Embedded().getReleases() != null)) {
                return resource.get_Embedded().getReleases();
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new AntennaException("Request to get all releases failed with " + response.getStatusCode());
        }
    }

    public SW360Release createRelease(SW360Release sw360Release, HttpHeaders header) throws IOException, AntennaException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Release, header);

        ResponseEntity<Resource<SW360Release>> response =
                this.restTemplate.exchange(this.releasesRestUrl,
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360Release>>() {});

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to create release " + sw360Release.getName() + " failed with "
                    + response.getStatusCode());
        }
    }
}
