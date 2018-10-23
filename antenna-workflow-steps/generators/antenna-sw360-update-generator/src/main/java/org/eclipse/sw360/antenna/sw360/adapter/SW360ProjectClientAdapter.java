/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.sw360.rest.SW360ProjectClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.sw360.antenna.sw360.utils.SW360ProjectAdapterUtils;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SW360ProjectClientAdapter {
    private final SW360ProjectClient projectClient;

    public SW360ProjectClientAdapter(String restUrl) {
        this.projectClient = new SW360ProjectClient(restUrl);
    }

    public Optional<String> getProjectIdByNameAndVersion(IProject project, HttpHeaders header) throws AntennaException {
        return getProjectIdByNameAndVersion(project.getProjectId(), project.getVersion(), header);
    }


    public Optional<String> getProjectIdByNameAndVersion(String projectName, String projectVersion, HttpHeaders header) throws AntennaException {
        List<SW360Project> projects = projectClient.searchByName(projectName, header);

        return projects.stream()
                .filter(pr -> hasEqualCoordinates(pr, projectName, projectVersion))
                .findAny()
                .map(SW360Project<LinkObjects, Embedded>::get_Links)
                .flatMap(SW360HalResourceUtility::getLastIndexOfLinkObject);
    }

    public String addProject(IProject project, SW360User user, HttpHeaders header) throws AntennaException, JsonProcessingException {
        SW360Project sw360Project = new SW360Project();
        SW360ProjectAdapterUtils.prepareProject(sw360Project, project, user);
        SW360Project responseProject = projectClient.createProject(sw360Project, header);

        return SW360HalResourceUtility.getLastIndexOfLinkObject(responseProject.get_Links()).orElse("");
    }

    public boolean hasEqualCoordinates(SW360Project sw360Project, String projectName, String projectVersion) {
        boolean isAppIdEqual = sw360Project.getName().equalsIgnoreCase(projectName);
        boolean isProjectVersionEqual = sw360Project.getVersion().equalsIgnoreCase(projectVersion);
        return isAppIdEqual && isProjectVersionEqual;
    }

    public void addSW360ReleasesToSW360Project(String id, Collection<SW360Release> releases, HttpHeaders header) throws IOException, AntennaException {
        List<String> releaseLinks = releases.stream()
                .map(r -> r.get_Links())
                .filter(Objects::nonNull)
                .map(LinkObjects::getSelf)
                .filter(Objects::nonNull)
                .map(Self::getHref)
                .collect(Collectors.toList());
        projectClient.addReleasesToProject(id, releaseLinks, header);
    }

    public List<SW360Release> getLinkedReleases(String projectId, HttpHeaders header) throws AntennaException {
        return projectClient.getLinkedReleases(projectId, true, header);
    }
}
