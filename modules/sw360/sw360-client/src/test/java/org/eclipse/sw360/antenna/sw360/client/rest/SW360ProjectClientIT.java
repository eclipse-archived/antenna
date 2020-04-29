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
package org.eclipse.sw360.antenna.sw360.client.rest;

import org.eclipse.sw360.antenna.http.utils.FailedRequestException;
import org.eclipse.sw360.antenna.http.utils.HttpConstants;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import static org.eclipse.sw360.antenna.http.utils.HttpUtils.waitFor;

public class SW360ProjectClientIT extends AbstractMockServerTest {
    /**
     * The names of the projects defined in the test data.
     */
    private static final String[] PROJECT_NAMES = {
            "Project_Foo", "Project_Bar", "Project_other", "Project_test"
    };

    private SW360ProjectClient projectClient;

    @Before
    public void setUp() {
        projectClient = new SW360ProjectClient(createClientConfig(), createMockTokenProvider());
        prepareAccessTokens(projectClient.getTokenProvider(), CompletableFuture.completedFuture(ACCESS_TOKEN));
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
    public void testSearchByName() throws IOException {
        final String projectName = "my Important Project";
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
                .withQueryParam("name", equalTo(projectName))
                .willReturn(aJsonResponse(HttpConstants.STATUS_OK)
                        .withBodyFile("all_projects.json")));

        List<SW360Project> projects = waitFor(projectClient.searchByName(projectName));
        checkTestProjects(projects);
    }

    @Test
    public void testSearchByNameEmptyResult() {
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_ACCEPTED)));

        extractException(projectClient.searchByName("foo"), IOException.class);
    }

    @Test
    public void testSearchByNameNoContent() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_NO_CONTENT)));

        List<SW360Project> projects = waitFor(projectClient.searchByName("some project"));
        assertThat(projects).isEmpty();
    }

    @Test
    public void testSearchByNameError() {
        wireMockRule.stubFor(get(urlPathEqualTo("/projects"))
                .willReturn(aJsonResponse(HttpConstants.STATUS_ERR_BAD_REQUEST)));

        FailedRequestException exception =
                expectFailedRequest(projectClient.searchByName("foo"), HttpConstants.STATUS_ERR_BAD_REQUEST);
        assertThat(exception.getTag()).isEqualTo(SW360ProjectClient.TAG_GET_BY_NAME);
    }

    @Test
    public void testCreateProject() throws IOException {
        SW360Project project = readTestJsonFile(resolveTestFileURL("project.json"), SW360Project.class);
        String projectJson = toJson(project);
        wireMockRule.stubFor(post(urlPathEqualTo("/projects"))
                .withRequestBody(equalToJson(projectJson))
                .willReturn(aJsonResponse(HttpConstants.STATUS_CREATED)
                        .withBody(projectJson)));

        SW360Project createdProject = waitFor(projectClient.createProject(project));
        assertThat(createdProject).isEqualTo(project);
    }

    @Test
    public void testAddReleasesToProject() throws IOException {
        final String projectID = "releasedProject";
        List<String> releases = Arrays.asList("release1", "releaseMe", "releaseParty");
        String urlPath = "/projects/" + projectID + "/releases";
        wireMockRule.stubFor(post(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_ACCEPTED)));

        waitFor(projectClient.addReleasesToProject(projectID, releases));
        wireMockRule.verify(postRequestedFor(urlPathEqualTo(urlPath))
                .withRequestBody(equalTo(toJson(releases))));
    }

    @Test
    public void testAddReleasesToProjectError() {
        wireMockRule.stubFor(post(anyUrl())
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_ERR_BAD_REQUEST)));

        FailedRequestException exception =
                expectFailedRequest(projectClient.addReleasesToProject("projectId",
                        Arrays.asList("foo", "bar")), HttpConstants.STATUS_ERR_BAD_REQUEST);
        assertThat(exception.getTag()).isEqualTo(SW360ProjectClient.TAG_ADD_RELEASES_TO_PROJECT);
    }

    /**
     * Helper method for testing whether releases linked to a project can be
     * queried.
     *
     * @param transitive flag whether transitive releases should be fetched
     */
    private void checkLinkedReleases(boolean transitive) throws IOException {
        final String projectID = "linkedProject";
        String urlPath = "/projects/" + projectID + "/releases";
        wireMockRule.stubFor(get(urlPathEqualTo(urlPath))
                .withQueryParam("transitive", equalTo(String.valueOf(transitive)))
                .willReturn(aJsonResponse(HttpConstants.STATUS_OK)
                        .withBodyFile("all_releases.json")));

        List<SW360SparseRelease> releases = waitFor(projectClient.getLinkedReleases(projectID, transitive));
        assertThat(releases).hasSize(6);
        assertHasLinks(releases);
    }

    @Test
    public void testGetLinkedReleases() throws IOException {
        checkLinkedReleases(false);
    }

    @Test
    public void testGetLinkedReleasesTransitive() throws IOException {
        checkLinkedReleases(true);
    }

    @Test
    public void testGetLinkedReleasesError() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_ERR_BAD_REQUEST)));

        FailedRequestException exception =
                expectFailedRequest(projectClient.getLinkedReleases("projectID", false),
                        HttpConstants.STATUS_ERR_BAD_REQUEST);
        assertThat(exception.getTag()).isEqualTo(SW360ProjectClient.TAG_GET_LINKED_RELEASES);
    }

    @Test
    public void testGetLinkedReleasesNoContent() throws IOException {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_NO_CONTENT)));

        List<SW360SparseRelease> releases = waitFor(projectClient.getLinkedReleases(PROJECT_NAMES[0], false));
        assertThat(releases).isEmpty();
    }
}
