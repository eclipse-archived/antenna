/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ProjectClient;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.SW360ProjectAdapterUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.block;

public class SW360ProjectClientAdapter {
    private final SW360ProjectClient projectClient;

    public SW360ProjectClientAdapter(SW360ProjectClient client) {
        projectClient = client;
    }

    public SW360ProjectClient getProjectClient() {
        return projectClient;
    }

    public Optional<String> getProjectIdByNameAndVersion(String projectName, String projectVersion) {
        List<SW360Project> projects = block(getProjectClient().searchByName(projectName));

        return projects.stream()
                .filter(pr -> SW360ProjectAdapterUtils.hasEqualCoordinates(pr, projectName, projectVersion))
                .findAny()
                .map(SW360Project::get_Links)
                .flatMap(SW360HalResourceUtility::getLastIndexOfSelfLink);
    }

    public String addProject(String projectName, String projectVersion) {
        SW360Project sw360Project = new SW360Project();
        SW360ProjectAdapterUtils.prepareProject(sw360Project, projectName, projectVersion);

        if (! SW360ProjectAdapterUtils.isValidProject(sw360Project)) {
            throw new SW360ClientException("Can not write invalid project with name=" + projectName + " and version=" + projectVersion);
        }

        SW360Project responseProject = block(getProjectClient().createProject(sw360Project));

        return SW360HalResourceUtility.getLastIndexOfSelfLink(responseProject.get_Links()).orElse("");
    }

    public void addSW360ReleasesToSW360Project(String id, Collection<SW360Release> releases) {
        List<String> releaseLinks = releases.stream()
                .map(SW360Release::get_Links)
                .filter(Objects::nonNull)
                .map(LinkObjects::getSelf)
                .filter(Objects::nonNull)
                .map(Self::getHref)
                .collect(Collectors.toList());
        getProjectClient().addReleasesToProject(id, releaseLinks);
    }

    public List<SW360SparseRelease> getLinkedReleases(String projectId) {
        return block(getProjectClient().getLinkedReleases(projectId, true));
    }
}
