/*
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

import org.apache.http.HttpStatus;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class SW360ReleaseClientIT extends AbstractMockServerTest {
    /**
     * Defines the expected test release data from the test JSON file.
     */
    private static final String[][] TEST_RELEASE_DATA = {
            {"handlebars", "4.0.1"},
            {"springdoc-openapi-ui", "1.1.45"},
            {"resteasy-rxjava2", "3.7.0.Final"},
            {"akka-actor_2.11", "2.4.12"},
            {"spring-boot-starter-web", "2.2.1.RELEASE"},
            {"jackson-module-kotlin", "2.9.8"}
    };

    private SW360ReleaseClient releaseClient;

    @Before
    public void setUp() {
        releaseClient = new SW360ReleaseClient(wireMockRule.baseUrl(), createRestTemplate());
    }

    /**
     * Checks whether a request for multiple releases yields the expected
     * result.
     *
     * @param releases the list with releases returned by the test client
     */
    private static void checkReleaseData(List<SW360SparseRelease> releases) {
        assertThat(releases).hasSize(TEST_RELEASE_DATA.length);
        List<String> expData = Arrays.stream(TEST_RELEASE_DATA)
                .map(release -> release[0] + ":" + release[1])
                .collect(Collectors.toList());
        List<String> releaseData = releases.stream()
                .map(release -> release.getName() + ":" + release.getVersion())
                .collect(Collectors.toList());
        assertThat(releaseData).isEqualTo(expData);

        assertHasLinks(releases);
    }

    @Test
    public void testGetRelease() {
        final String releaseId = "testRelease";
        wireMockRule.stubFor(get(urlPathEqualTo("/releases/" + releaseId))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("release.json")));

        SW360Release release = assertPresent(releaseClient.getRelease(releaseId, new HttpHeaders()));
        assertThat(release.getName()).isEqualTo("akka-actor_2.11");
        assertThat(release.getVersion()).isEqualTo("2.4.12");
        assertThat(release.getExternalIds()).contains(new AbstractMap.SimpleEntry<>("hash_1", "501887b9053ef9f4341a"));
    }

    @Test
    public void testGetReleaseEmptyBody() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aJsonResponse(HttpStatus.SC_OK)));

        Optional<SW360Release> optRelease = releaseClient.getRelease("foo", new HttpHeaders());
        assertThat(optRelease).isNotPresent();
    }

    @Test
    public void testGetReleasesByExternalIds() {
        Map<String, String> idMap = new LinkedHashMap<>();
        idMap.put("id 1", "testRelease");
        idMap.put("id2", "otherFilter");
        wireMockRule.stubFor(get(urlPathEqualTo("/releases/searchByExternalIds"))
                .withQueryParam("id+1", equalTo("testRelease"))
                .withQueryParam("id2", equalTo("otherFilter"))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("all_releases.json")));

        List<SW360SparseRelease> releases = releaseClient.getReleasesByExternalIds(idMap, new HttpHeaders());
        checkReleaseData(releases);
    }

    @Test
    public void testGetReleasesByExternalIdsError() {
        wireMockRule.stubFor(get(urlPathEqualTo("/releases/searchByExternalIds"))
                .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        List<SW360SparseRelease> releases = releaseClient.getReleasesByExternalIds(new HashMap<>(), new HttpHeaders());
        assertThat(releases).hasSize(0);
    }

    @Test
    public void testCreateRelease() throws IOException {
        SW360Release release = readTestJsonFile(resolveTestFileURL("release.json"), SW360Release.class);
        String releaseJson = toJson(release);
        wireMockRule.stubFor(post(urlEqualTo("/releases"))
                .withRequestBody(equalToJson(releaseJson))
                .willReturn(aJsonResponse(HttpStatus.SC_CREATED)
                        .withBody(releaseJson)));

        SW360Release createdRelease = releaseClient.createRelease(release, new HttpHeaders());
        assertThat(createdRelease).isEqualTo(release);
    }

    @Test
    public void testCreateReleaseError() throws IOException {
        SW360Release release = readTestJsonFile(resolveTestFileURL("release.json"), SW360Release.class);
        wireMockRule.stubFor(post(urlEqualTo("/releases"))
                .withRequestBody(equalToJson(toJson(release)))
                .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        SW360Release createdRelease = releaseClient.createRelease(release, new HttpHeaders());
        assertThat(createdRelease).isEqualTo(release);
    }

    @Test
    public void testPatchRelease() throws IOException {
        SW360Release release = readTestJsonFile(resolveTestFileURL("release.json"), SW360Release.class);
        String releaseJson = toJson(release);
        wireMockRule.stubFor(patch(urlEqualTo("/releases/" + release.getReleaseId()))
                .withRequestBody(equalToJson(releaseJson))
                .willReturn(aJsonResponse(HttpStatus.SC_ACCEPTED)
                        .withBody(releaseJson)));

        SW360Release patchedRelease = releaseClient.patchRelease(release, new HttpHeaders());
        assertThat(patchedRelease).isEqualTo(release);
    }

    @Test
    public void testPatchReleaseError() throws IOException {
        SW360Release release = readTestJsonFile(resolveTestFileURL("release.json"), SW360Release.class);
        wireMockRule.stubFor(patch(urlEqualTo("/releases/" + release.getReleaseId()))
                .withRequestBody(equalToJson(toJson(release)))
                .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        SW360Release patchedRelease = releaseClient.patchRelease(release, new HttpHeaders());
        assertThat(patchedRelease).isEqualTo(release);
    }
}
