/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
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

import org.eclipse.sw360.antenna.http.RequestBuilder;
import org.eclipse.sw360.antenna.http.utils.HttpUtils;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;
import org.eclipse.sw360.antenna.sw360.client.auth.AccessTokenProvider;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ResourceUtils;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentList;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * An SW360 REST client implementation providing basic functionality related to
 * the {@code /components} endpoint.
 * </p>
 */
public class SW360ComponentClient extends SW360Client {
    /**
     * Tag for the query that returns all components in the system.
     */
    static final String TAG_GET_COMPONENTS = "get_components";

    /**
     * Tag for the query that returns details about a specific component.
     */
    static final String TAG_GET_COMPONENT = "get_component";

    /**
     * Tag for the query that searches for components by name.
     */
    static final String TAG_GET_COMPONENTS_BY_NAME = "get_components_by_name";

    /**
     * Tag for the request to create a new component.
     */
    static final String TAG_CREATE_COMPONENT = "post_create_component";

    private static final String COMPONENTS_ENDPOINT = "components";

    /**
     * Creates a new instance of {@code SW360ComponentClient} and initializes
     * it with the passed in dependencies.
     *
     * @param config   the client configuration
     * @param provider the provider for access tokens
     */
    public SW360ComponentClient(SW360ClientConfig config, AccessTokenProvider provider) {
        super(config, provider);
    }

    /**
     * Returns a future with detail information about the component with the ID
     * provided. If the component cannot be found, the future fails with a
     * {@link org.eclipse.sw360.antenna.http.utils.FailedRequestException} with
     * status code 404.
     *
     * @param componentId the ID of the component in question
     * @return a future with details about this component
     */
    public CompletableFuture<SW360Component> getComponent(String componentId) {
        return executeJsonRequest(HttpUtils.get(resourceUrl(COMPONENTS_ENDPOINT, componentId)),
                SW360Component.class, TAG_GET_COMPONENT);
    }

    /**
     * Returns a future with a list containing all the components known to the
     * system.
     *
     * @return a future with a list with all existing components
     */
    public CompletableFuture<List<SW360SparseComponent>> getComponents() {
        return executeJsonRequestWithDefault(HttpUtils.get(resourceUrl(COMPONENTS_ENDPOINT)), SW360ComponentList.class,
                TAG_GET_COMPONENTS, SW360ComponentList::new)
                .thenApply(SW360ResourceUtils::getSw360SparseComponents);
    }

    /**
     * Returns a future with a list containing all the components whose name
     * matches the given pattern.
     *
     * @param name the search pattern for the component name
     * @return a future with a list with all matching components
     */
    public CompletableFuture<List<SW360SparseComponent>> searchByName(String name) {
        String url = HttpUtils.addQueryParameter(resourceUrl(COMPONENTS_ENDPOINT),
                SW360Attributes.COMPONENT_SEARCH_BY_NAME, name);
        return executeJsonRequestWithDefault(HttpUtils.get(url), SW360ComponentList.class,
                TAG_GET_COMPONENTS_BY_NAME, SW360ComponentList::new)
                .thenApply(SW360ResourceUtils::getSw360SparseComponents);
    }

    /**
     * Creates a new component based on the data object passed in and returns a
     * future with the result.
     *
     * @param sw360Component a data object for the component to be created
     * @return a future with the new entity that has been created
     */
    public CompletableFuture<SW360Component> createComponent(SW360Component sw360Component) {
        return executeJsonRequest(builder -> builder.method(RequestBuilder.Method.POST)
                        .uri(resourceUrl(COMPONENTS_ENDPOINT))
                        .body(body -> body.json(sw360Component)),
                SW360Component.class, TAG_CREATE_COMPONENT);
    }
}
