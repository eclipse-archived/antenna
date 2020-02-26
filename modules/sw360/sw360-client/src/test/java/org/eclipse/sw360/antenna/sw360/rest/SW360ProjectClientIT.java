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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpStatus;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class SW360ProjectClientIT extends AbstractMockServerTest {
    /**
     * The names of the projects defined in the test data.
     */
    private static final String[] PROJECT_NAMES = {
            "Central-Waivers_UNKNOWN-License", "Central-Waivers_No-Sources", "SheriffToolTest",
            "Central-Waivers_No-Source-License"
    };

    private SW360ProjectClient projectClient;

    @Before
    public void setUp() {
        projectClient = new SW360ProjectClient(wireMockRule.baseUrl(), createRestTemplate());
    }

    /**
     * Checks whether the expected test projects have been retrieved.
     *
     * @param projects the projects to be checked
     */
    private static void checkTestProjects(Collection<? extends SW360Project> projects) {
        List<String> actualProjectNames = projects.stream()
                .map(SW360Project::getName)
                .collect(Collectors.toList());
        assertThat(actualProjectNames).containsExactly(PROJECT_NAMES);
        assertHasLinks(projects);
    }

    @Test
    public void testSearchByName() {
        final String projectName = "myImportantProject";
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
                .withQueryParam("name", equalTo(projectName))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("all_projects.json")));

        List<SW360Project> projects = projectClient.searchByName(projectName, new HttpHeaders());
        checkTestProjects(projects);
    }

    @Test
    public void testSearchByNameNoContent() {
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
                .willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT)));

        List<SW360Project> projects = projectClient.searchByName("foo", new HttpHeaders());
        assertThat(projects).hasSize(0);
    }

    @Test
    public void testSearchByNameError() {
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
        .willReturn(aJsonResponse(HttpStatus.SC_BAD_REQUEST)));

        List<SW360Project> projects = projectClient.searchByName("foo", new HttpHeaders());
        assertThat(projects).hasSize(0);
    }

    @Test
    public void testCreateProject() throws IOException {
        SW360Project project = readTestJsonFile(resolveTestFileURL("project.json"), SW360Project.class);
        String projectJson = toJson(project);
        wireMockRule.stubFor(post(urlPathEqualTo("/projects"))
                .withRequestBody(equalToJson(projectJson))
                .willReturn(aJsonResponse(HttpStatus.SC_CREATED)
                        .withBody(projectJson)));

        SW360Project createdProject = projectClient.createProject(project, new HttpHeaders());
        assertThat(createdProject).isEqualTo(project);
    }

    @Test
    public void testAddReleasesToProject() throws JsonProcessingException {
        final String projectID = "releasedProject";
        List<String> releases = Arrays.asList("release1", "releaseMe", "releaseParty");
        String urlPath = "/projects/" + projectID + "/releases";
        wireMockRule.stubFor(post(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(HttpStatus.SC_ACCEPTED)));

        projectClient.addReleasesToProject(projectID, releases, new HttpHeaders());
        wireMockRule.verify(postRequestedFor(urlPathEqualTo(urlPath))
                .withRequestBody(equalTo(toJson(releases))));
    }

    @Test
    public void testAddReleasesToProjectError() {
        wireMockRule.stubFor(post(anyUrl())
        .willReturn(aResponse().withStatus(HttpStatus.SC_BAD_REQUEST)));

        projectClient.addReleasesToProject("projectId", Arrays.asList("foo", "bar"),
                new HttpHeaders());
    }

    /**
     * Helper method for testing whether releases linked to a project can be
     * queried.
     *
     * @param transitive flag whether transitive releases should be fetched
     */
    private void checkLinkedReleases(boolean transitive) {
        final String projectID = "linkedProject";
        String urlPath = "/projects/" + projectID + "/releases";
        wireMockRule.stubFor(get(urlPathEqualTo(urlPath))
                .withQueryParam("transitive", equalTo(String.valueOf(transitive)))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("all_releases.json")));

        List<SW360SparseRelease> releases = projectClient.getLinkedReleases(projectID, transitive, new HttpHeaders());
        assertThat(releases).hasSize(6);
        assertHasLinks(releases);
    }

    @Test
    public void testGetLinkedReleases() {
        checkLinkedReleases(false);
    }

    @Test
    public void testGetLinkedReleasesTransitive() {
        checkLinkedReleases(true);
    }

    @Test
    public void testGetLinkedReleasesError() {
        wireMockRule.stubFor(get(anyUrl())
        .willReturn(aResponse().withStatus(HttpStatus.SC_BAD_REQUEST)));

        List<SW360SparseRelease> releases = projectClient.getLinkedReleases("projectID", false,
                new HttpHeaders());
        assertThat(releases).hasSize(0);
    }
}
