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
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Service interface for an adapter supporting operations on SW360 project
 * entities.
 * </p>
 */
public interface SW360ProjectClientAdapter {
    SW360ProjectClient getProjectClient();

    Optional<String> getProjectIdByNameAndVersion(String projectName, String projectVersion);

    String addProject(String projectName, String projectVersion);

    void addSW360ReleasesToSW360Project(String id, Collection<SW360Release> releases);

    List<SW360SparseRelease> getLinkedReleases(String projectId);
}
