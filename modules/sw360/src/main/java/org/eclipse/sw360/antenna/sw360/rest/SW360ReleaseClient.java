/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
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
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.getSw360SparseReleases;

public class SW360ReleaseClient extends SW360AttachmentAwareClient<SW360Release> {
    private static final String RELEASES_ENDPOINT_APPENDIX = "/releases";
    private final String restUrl;

    public SW360ReleaseClient(String restUrl, ProxySettings proxySettings) {
        super(proxySettings);
        this.restUrl = restUrl;
    }

    @Override
    public Class<SW360Release> getHandledClassType() {
        return SW360Release.class;
    }

    @Override
    public String getEndpoint() {
        return restUrl + RELEASES_ENDPOINT_APPENDIX;
    }

    public SW360Release getRelease(String releaseId, HttpHeaders header) throws ExecutionException {
        ResponseEntity<Resource<SW360Release>> response = doRestGET(getEndpoint() + "/" + releaseId, header,
                new ParameterizedTypeReference<Resource<SW360Release>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
        } else {
            throw new ExecutionException("Request to get release " + releaseId + " failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseRelease> getReleases(HttpHeaders header) throws ExecutionException {
        ResponseEntity<Resource<SW360ReleaseList>> response = doRestGET(getEndpoint(), header,
                new ParameterizedTypeReference<Resource<SW360ReleaseList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return getSw360SparseReleases(response);
        } else {
            throw new ExecutionException("Request to get all releases failed with " + response.getStatusCode());
        }
    }

    public SW360Release createRelease(SW360Release sw360Release, HttpHeaders header) throws ExecutionException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Release, header);

        ResponseEntity<Resource<SW360Release>> response = doRestPOST(getEndpoint(), httpEntity,
                new ParameterizedTypeReference<Resource<SW360Release>>() {});

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
        } else {
            throw new ExecutionException("Request to create release " + sw360Release.getName() + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360Release patchRelease(SW360Release sw360Release, HttpHeaders header) throws ExecutionException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Release, header);

        ResponseEntity<Resource<SW360Release>> response = doRestPATCH(getEndpoint(), httpEntity,
                new ParameterizedTypeReference<Resource<SW360Release>>() {});

        if (! response.getStatusCode().is2xxSuccessful()) {
            throw new ExecutionException("Request to create release " + sw360Release.getName() + " failed with "
                    + response.getStatusCode());
        }
        Resource<SW360Release> body = response.getBody();
        if (body == null) {
            throw new ExecutionException("Request to create release " + sw360Release.getName() + " returned empty body");
        }

        return body.getContent();
    }
}
