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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpStatus;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SW360ComponentClientIT {
    /**
     * An array with the names of test components contained in the test file.
     */
    private static final String[] TEST_COMPONENTS = {
            "jackson-annotations", "jakarta.validation-api", "jsoup"
    };

    /**
     * The mapper for JSON serialization.
     */
    private static ObjectMapper objectMapper;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private SW360ComponentClient componentClient;

    @BeforeClass
    public static void setUpOnce() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Before
    public void setUp() {
        componentClient = new SW360ComponentClient(wireMockRule.baseUrl(), new RestTemplate());
    }

    /**
     * Convenience method to generate an initialized response builder for a
     * JSON response with the given status code.
     *
     * @param status the status code
     * @return the initialized {@code ResponseDefinitionBuilder}
     */
    private static ResponseDefinitionBuilder aJsonResponse(int status) {
        return aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json");
    }

    /**
     * Returns a URL to the test file with the given name. The file is
     * looked up in the files directory of the Wiremock server.
     *
     * @param name the name of the desired test file
     * @return the URL to the test file specified
     */
    private static URL resolveTestFileURL(String name) {
        URL url = SW360ComponentClientIT.class.getResource("/__files/" + name);
        assertThat(url).isNotNull();
        return url;
    }

    /**
     * Parses a JSON file specified by the given URL and returns its
     * de-serialized content.
     *
     * @param url  the URL pointing to the file
     * @param type the class representing the content type
     * @param <T>  the content type
     * @return the de-serialized content representation
     * @throws IOException if an error occurs
     */
    private static <T> T readTestJsonFile(URL url, Class<T> type) throws IOException {
        return objectMapper.readValue(url, type);
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
        boolean hasLinks = components.stream()
                .allMatch(comp -> comp.get_Links().getSelf() != null);
        assertThat(hasLinks).isTrue();
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
    public void testGetComponent() {
        final String componentId = "testComponentID";
        wireMockRule.stubFor(get(urlPathEqualTo("/components/" + componentId))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("component.json")));

        Optional<SW360Component> optComponent = componentClient.getComponent(componentId, new HttpHeaders());
        if (!optComponent.isPresent()) {
            fail("No component returned!");
        } else {
            SW360Component component = optComponent.get();
            assertThat(component.getName()).isEqualTo("jackson-annotations");
            SW360ComponentEmbedded embedded = component.get_Embedded();
            assertThat(embedded.getCreatedBy().getEmail()).isEqualTo("osi9be@bosch.com");
            List<SW360SparseRelease> releases = embedded.getReleases();
            assertThat(releases).hasSize(10);
        }
    }

    @Test(expected = HttpClientErrorException.class)
    public void testGetComponentNotFound() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aJsonResponse(HttpStatus.SC_NOT_FOUND)));

        componentClient.getComponent("unknownComponent", new HttpHeaders());
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
}