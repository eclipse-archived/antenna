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
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class SW360ComponentClientIT extends AbstractMockServerTest {
    /**
     * An array with the names of test components contained in the test file.
     */
    private static final String[] TEST_COMPONENTS = {
            "jackson-annotations", "jakarta.validation-api", "jsoup"
    };

    private SW360ComponentClient componentClient;

    @Before
    public void setUp() {
        componentClient = new SW360ComponentClient(wireMockRule.baseUrl(), createRestTemplate());
    }

    /**
     * Checks whether a request yields the expected list of test components.
     *
     * @param components the list with components
     */
    private static void checkComponentsList(List<SW360SparseComponent> components) {
        assertThat(components).hasSize(TEST_COMPONENTS.length);
        List<String> componentNames = components.stream().map(SW360SparseComponent::getName)
                .collect(Collectors.toList());
        assertThat(componentNames).containsExactlyInAnyOrder(TEST_COMPONENTS);
        assertHasLinks(components);
    }

    @Test
    public void testGetComponents() {
        wireMockRule.stubFor(get(urlPathEqualTo("/components"))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("all_components.json")));

        List<SW360SparseComponent> components = componentClient.getComponents(new HttpHeaders());
        checkComponentsList(components);
    }

    @Test
    public void testGetComponentsNoContent() {
        wireMockRule.stubFor(get(urlPathEqualTo("/components"))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)));

        List<SW360SparseComponent> components = componentClient.getComponents(new HttpHeaders());
        assertThat(components).hasSize(0);
    }

    @Test
    public void testGetComponentsError() {
        wireMockRule.stubFor(get(urlPathEqualTo("/components"))
                .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        List<SW360SparseComponent> components = componentClient.getComponents(new HttpHeaders());
        assertThat(components).hasSize(0);
    }

    @Test
    public void testSearchByName() {
        final String componentName = "testComponentSearchPattern";
        wireMockRule.stubFor(get(urlPathEqualTo("/components"))
                .withQueryParam(SW360Attributes.COMPONENT_SEARCH_BY_NAME, equalTo(componentName))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("all_components.json")));

        List<SW360SparseComponent> components = componentClient.searchByName(componentName, new HttpHeaders());
        checkComponentsList(components);
    }

    @Test
    public void testSearchByNameNoResults() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBody("{}")));

        List<SW360SparseComponent> components = componentClient.searchByName("foo", new HttpHeaders());
        assertThat(components).hasSize(0);
    }

    @Test
    public void testSearchByNameError() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aJsonResponse(HttpStatus.SC_FORBIDDEN)));

        List<SW360SparseComponent> components = componentClient.searchByName("foo", new HttpHeaders());
        assertThat(components).hasSize(0);
    }

    @Test
    public void testGetComponent() {
        final String componentId = "testComponentID";
        wireMockRule.stubFor(get(urlPathEqualTo("/components/" + componentId))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("component.json")));

        SW360Component component = assertPresent(componentClient.getComponent(componentId, new HttpHeaders()));
        assertThat(component.getName()).isEqualTo("jackson-annotations");
        SW360ComponentEmbedded embedded = component.get_Embedded();
        assertThat(embedded.getCreatedBy().getEmail()).isEqualTo("osi9be@bosch.com");
        List<SW360SparseRelease> releases = embedded.getReleases();
        assertThat(releases).hasSize(10);
    }

    @Test
    public void testGetComponentNotFound() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aJsonResponse(HttpStatus.SC_NOT_FOUND)));

        Optional<SW360Component> optComponent = componentClient.getComponent("unknownComponent", new HttpHeaders());
        assertThat(optComponent).isNotPresent();
    }

    @Test
    public void testGetComponentEmptyBody() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aJsonResponse(HttpStatus.SC_OK)));

        Optional<SW360Component> optComponent = componentClient.getComponent("bar", new HttpHeaders());
        assertThat(optComponent).isNotPresent();
    }

    @Test
    public void testCreateComponent() throws IOException {
        SW360Component component = readTestJsonFile(resolveTestFileURL("component.json"), SW360Component.class);
        wireMockRule.stubFor(post(urlPathEqualTo("/components"))
                .willReturn(aJsonResponse(HttpStatus.SC_CREATED)
                        .withBodyFile("component.json")));

        SW360Component createdComponent = componentClient.createComponent(component, new HttpHeaders());
        assertThat(createdComponent).isEqualTo(component);
    }

    @Test
    public void testCreateComponentError() throws IOException {
        SW360Component component = readTestJsonFile(resolveTestFileURL("component.json"), SW360Component.class);
        wireMockRule.stubFor(post(urlPathEqualTo("/components"))
                .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        SW360Component createdComponent = componentClient.createComponent(component, new HttpHeaders());
        assertThat(createdComponent).isEqualTo(component);
    }
}
