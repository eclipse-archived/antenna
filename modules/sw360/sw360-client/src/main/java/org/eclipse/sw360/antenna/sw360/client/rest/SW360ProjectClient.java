/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
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

package org.eclipse.sw360.antenna.sw360.client.rest;

import org.eclipse.sw360.antenna.http.api.RequestBuilder;
import org.eclipse.sw360.antenna.http.utils.HttpUtils;
import org.eclipse.sw360.antenna.sw360.client.SW360ClientConfig;
import org.eclipse.sw360.antenna.sw360.client.auth.AccessTokenProvider;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ResourceUtils;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * An SW360 REST client implementation providing basic functionality related to
 * the {@code /projects} endpoint.
 * </p>
 */
public class SW360ProjectClient extends SW360Client {
    /**
     * Tag for the query that searches projects by their name.
     */
    static final String TAG_GET_BY_NAME = "get_projects_by_name";

    /**
     * Tag for the request to create a new project.
     */
    static final String TAG_CREATE_PROJECT = "post_create_project";

    /**
     * Tag for the query that adds releases to a project.
     */
    static final String TAG_ADD_RELEASES_TO_PROJECT = "post_add_releases_to_project";

    /**
     * Tag for the query that returns the releases linked to a project.
     */
    static final String TAG_GET_LINKED_RELEASES = "get_releases_linked_to_project";

    private static final String PROJECTS_ENDPOINT = "projects";

    /**
     * Creates a new instance of {@code SW360ProjectClient} with the passed in
     * dependencies.
     *
     * @param config   the client configuration
     * @param provider the provider for access tokens
     */
    public SW360ProjectClient(SW360ClientConfig config, AccessTokenProvider provider) {
        super(config, provider);
    }

    /**
     * Returns a future with a list of projects whose name matches the given
     * string.
     *
     * @param name the project name to search for
     * @return a future with the list of the projects that were matched
     */
    public CompletableFuture<List<SW360Project>> searchByName(String name) {
        String queryUrl = HttpUtils.addQueryParameter(resourceUrl(PROJECTS_ENDPOINT),
                SW360Attributes.PROJECT_SEARCH_BY_NAME, name);
        return executeJsonRequest(HttpUtils.get(queryUrl), SW360ProjectList.class, TAG_GET_BY_NAME)
                .thenApply(SW360ResourceUtils::getSw360Projects);
    }

    /**
     * Creates a new project in SW360 based on the given data object and
     * returns a future with the resulting entity.
     *
     * @param sw360Project a data object for the project to be added
     * @return a future with the resulting entity
     */
    public CompletableFuture<SW360Project> createProject(SW360Project sw360Project) {
        return executeJsonRequest(builder -> builder.method(RequestBuilder.Method.POST)
                .uri(resourceUrl(PROJECTS_ENDPOINT))
                .bodyJson(sw360Project),
                SW360Project.class, TAG_CREATE_PROJECT);
    }

    /**
     * Links a number of releases to a specific project and returns a future
     * with the result.
     *
     * @param projectId the ID of the project
     * @param releases  a list with the IDs of the releases to be linked
     * @return a future with the result of the operation
     */
    public CompletableFuture<Void> addReleasesToProject(String projectId, List<String> releases) {
        return executeRequest(builder -> builder.method(RequestBuilder.Method.POST)
                .uri(resourceUrl(PROJECTS_ENDPOINT, projectId, SW360Attributes.PROJECT_RELEASES))
                .bodyJson(releases),
                HttpUtils.nullProcessor(), TAG_ADD_RELEASES_TO_PROJECT);
    }

    /**
     * Returns a future with data about the releases that are linked to a
     * specific project.
     *
     * @param projectId  the ID of the project in question
     * @param transitive the transitive flag
     * @return a future with a list of the releases linked to the project
     */
    public CompletableFuture<List<SW360SparseRelease>> getLinkedReleases(String projectId, boolean transitive) {
        String uri = HttpUtils.addQueryParameter(resourceUrl(PROJECTS_ENDPOINT, projectId,
                SW360Attributes.PROJECT_RELEASES),
                SW360Attributes.PROJECT_RELEASES_TRANSITIVE, transitive);
        return executeJsonRequest(HttpUtils.get(uri), SW360ReleaseList.class, TAG_GET_LINKED_RELEASES)
                .thenApply(SW360ResourceUtils::getSw360SparseReleases);
    }
}
