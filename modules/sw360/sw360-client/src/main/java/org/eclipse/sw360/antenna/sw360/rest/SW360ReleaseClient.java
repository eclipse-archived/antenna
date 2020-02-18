/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
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

import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.sw360.utils.SW360ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.sw360.antenna.sw360.utils.SW360ClientUtils.checkRestStatus;
import static org.eclipse.sw360.antenna.sw360.utils.SW360ClientUtils.getSw360SparseReleases;

public class SW360ReleaseClient extends SW360AttachmentAwareClient<SW360Release> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360ReleaseClient.class);
    private static final String RELEASES_ENDPOINT_APPENDIX = "/releases";
    private final String restUrl;

    public SW360ReleaseClient(String restUrl, RestTemplate template) {
        super(template);
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

    public Optional<SW360Release> getRelease(String releaseId, HttpHeaders header) {
        try {
            ResponseEntity<SW360Release> response = doRestGET(getEndpoint() + "/" + releaseId, header,
                    SW360Release.class);

            checkRestStatus(response);
            return Optional.ofNullable(response.getBody());
        } catch (SW360ClientException e) {
            LOGGER.debug(e.getMessage());
            return Optional.empty();
        }
    }

    // KnownLimitation: this can not properly handle e.g. the hashes,
    // which are mapped to numbered keys like `hash_1=...`, `hash_2=...`, ...
    // but can change in the order of the values
    public List<SW360SparseRelease> getReleasesByExternalIds(Map<String, String> externalIds, HttpHeaders header) {
        try {
            String url = getExternalIdUrl(externalIds);

            ResponseEntity<SW360ReleaseList> response = doRestGET(url, header, SW360ReleaseList.class);

            return getSw360SparseReleases(response);
        } catch (HttpClientErrorException e) {
            LOGGER.debug("Request to get releases with externalId {} failed with {}", externalIds, e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return Collections.emptyList();
        } catch (SW360ClientException e) {
            LOGGER.debug("Request to get releases with externalId {} failed with {}", externalIds, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getExternalIdUrl(Map<String, String> externalIds) {
        return externalIds.entrySet().stream()
                .map(e -> {
                    try {
                        return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new SW360ClientException("Failed to generate externalId query URL", ex);
                    }
                })
                .collect(Collectors.joining("&", getEndpoint() + "/searchByExternalIds?", ""));
    }

    public SW360Release createRelease(SW360Release sw360Release, HttpHeaders header) {
        try {
            HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Release, header);

            ResponseEntity<SW360Release> response = doRestPOST(getEndpoint(), httpEntity, SW360Release.class);

            checkRestStatus(response);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("Request to create release {} failed with {}", sw360Release.getName(), e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return sw360Release;
        } catch (SW360ClientException e) {
            LOGGER.error("Request to create release {} failed with {}", sw360Release.getName(), e.getMessage());
            return sw360Release;
        }
    }

    public SW360Release patchRelease(SW360Release sw360Release, HttpHeaders header) {
        try {
            HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Release, header);

            ResponseEntity<SW360Release> response = doRestPATCH(getEndpoint() + "/" + sw360Release.getReleaseId(),
                    httpEntity, SW360Release.class);

            checkRestStatus(response);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("Request to update release {} failed with {}", sw360Release.getName(), e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return sw360Release;
        } catch (SW360ClientException e) {
            LOGGER.error("Request to update release {} failed with {}", sw360Release.getName(), e.getMessage());
            return sw360Release;
        }
    }
}
