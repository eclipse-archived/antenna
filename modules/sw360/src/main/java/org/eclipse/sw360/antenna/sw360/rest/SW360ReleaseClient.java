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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public SW360Release getRelease(String releaseId, HttpHeaders header) {
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

    // KnownLimitation: this can not properly handle e.g. the hashes,
    // which are mapped to numbered keys like `hash_1=...`, `hash_2=...`, ...
    // but can change in the order of the values
    public List<SW360SparseRelease> getReleasesByExternalIds(Map<String, String> externalIds, HttpHeaders header) {
        String url = externalIds.entrySet().stream()
                .map(e -> {
                    try {
                        return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new ExecutionException("Failed to generate externalId query URL", ex);
                    }
                })
                .collect(Collectors.joining("&", getEndpoint() + "/searchByExternalIds?", ""));

        ResponseEntity<Resource<SW360ReleaseList>> response = doRestGET(url, header,
                new ParameterizedTypeReference<Resource<SW360ReleaseList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return getSw360SparseReleases(response);
        } else {
            throw new ExecutionException("Request to get release with externalId " + url + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360Release createRelease(SW360Release sw360Release, HttpHeaders header) {
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

    public SW360Release patchRelease(SW360Release sw360Release, HttpHeaders header) {
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
