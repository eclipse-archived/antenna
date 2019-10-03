/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 * Copyright (c) Verifa Oy 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.getSw360SparseReleases;

public class SW360ProjectClient extends SW360Client {
    private static final String PROJECTS_ENDPOINT = "/projects";
    private final String restUrl;

    public SW360ProjectClient(String restUrl, ProxySettings proxySettings) {
        super(proxySettings);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + PROJECTS_ENDPOINT;
    }

    public List<SW360Project> searchByName(String name, HttpHeaders header) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(getEndpoint())
                .queryParam(SW360Attributes.PROJECT_SEARCH_BY_NAME, name);

        ResponseEntity<Resource<SW360ProjectList>> response = doRestGET(builder.build(false).toUriString(), header,
                new ParameterizedTypeReference<Resource<SW360ProjectList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            SW360ProjectList resource = Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
            if (resource != null &&
                    resource.get_Embedded() != null &&
                    resource.get_Embedded().getProjects() != null) {
                return resource.get_Embedded().getProjects();
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new ExecutionException("Request to search for projects with the name " + name + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360Project createProject(SW360Project sw360Project, HttpHeaders header) {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Project, header);
        ResponseEntity<Resource<SW360Project>> response = doRestPOST(getEndpoint(), httpEntity,
                new ParameterizedTypeReference<Resource<SW360Project>>() {});

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
        } else {
            throw new ExecutionException("Request to create project " + sw360Project.getName() + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360Project getProject(String projectId, HttpHeaders header) {
        ResponseEntity<Resource<SW360Project>> response = doRestGET(getEndpoint() + "/" + projectId, header,
                new ParameterizedTypeReference<Resource<SW360Project>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
        } else {
            throw new ExecutionException("Request to get project " + projectId + " failed with "
                    + response.getStatusCode());
        }
    }

    public void addReleasesToProject(String projectId, List<String> releases, HttpHeaders header) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(getEndpoint())
                .pathSegment(projectId, SW360Attributes.PROJECT_RELEASES);

        HttpEntity<List<String>> httpEntity = new HttpEntity<>(releases, header);
        ResponseEntity<String> response = doRestCall(builder.build(false).toUriString(), HttpMethod.POST, httpEntity, String.class);

        if (!(response.getStatusCode() == HttpStatus.CREATED)) {
            throw new ExecutionException("Request to add linked releases to project " + projectId + " failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseRelease> getLinkedReleases(String projectId, boolean transitive, HttpHeaders header) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(getEndpoint())
                .pathSegment(projectId, SW360Attributes.PROJECT_RELEASES)
                .queryParam(SW360Attributes.PROJECT_RELEASES_TRANSITIVE, transitive);

        ResponseEntity<Resource<SW360ReleaseList>> response = doRestGET(builder.build(false).toUriString(), header,
                new ParameterizedTypeReference<Resource<SW360ReleaseList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return getSw360SparseReleases(response);
        } else {
            throw new ExecutionException("Request to get linked releases of project with id=[ " + projectId + "] failed with "
                    + response.getStatusCode());
        }
    }
}
