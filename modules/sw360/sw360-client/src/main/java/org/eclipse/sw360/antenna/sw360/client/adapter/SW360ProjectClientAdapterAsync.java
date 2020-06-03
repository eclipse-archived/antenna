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
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ProjectClient;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Service interface for an adapter supporting asynchronous operations on SW360
 * project entities.
 * </p>
 */
public interface SW360ProjectClientAdapterAsync {
    /**
     * Returns the {@code SW360ProjectClient} used by this adapter. This client
     * can be used for low-level operations on project entities.
     *
     * @return the {@code SW360ProjectClient}
     */
    SW360ProjectClient getProjectClient();

    /**
     * Creates a new {@code SW360Project} entity based on the given data
     * object. The passed in object is validated whether it contains all
     * mandatory properties.
     *
     * @param project the data object defining the project properties
     * @return a future with the newly created {@code SW360Project} entity
     */
    CompletableFuture<SW360Project> createProject(SW360Project project);

    CompletableFuture<Optional<String>> getProjectIdByNameAndVersion(String projectName, String projectVersion);

    CompletableFuture<Void> addSW360ReleasesToSW360Project(String id, Collection<SW360Release> releases);

    CompletableFuture<List<SW360SparseRelease>> getLinkedReleases(String projectId);
}
