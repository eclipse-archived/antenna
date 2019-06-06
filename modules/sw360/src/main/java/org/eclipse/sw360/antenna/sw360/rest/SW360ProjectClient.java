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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SW360ProjectClient {
    private static final String PROJECTS_ENDPOINT = "/projects";

    private String projectsRestUrl;
    private RestTemplate restTemplate;

    public SW360ProjectClient(String restUrl, boolean proxyUse, String proxyHost, int proxyPort) {
        projectsRestUrl = restUrl + PROJECTS_ENDPOINT;
        this.restTemplate = restTemplate(proxyUse, proxyHost, proxyPort);
    }

    private RestTemplate restTemplate(boolean proxyUse, String proxyHost, int proxyPort) {
        if (proxyUse && proxyHost != null) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
            return new RestTemplate(requestFactory);
        } else {
            return new RestTemplate();
        }
    }

    public List<SW360Project> searchByName(String name, HttpHeaders header) throws AntennaException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(projectsRestUrl)
                .queryParam(SW360Attributes.PROJECT_SEARCH_BY_NAME, name);

        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360ProjectList>> response = this.restTemplate.
                exchange(builder.build(false).toUriString(),
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360ProjectList>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            SW360ProjectList resource = response.getBody().getContent();
            if ((resource.get_Embedded() != null) && (resource.get_Embedded().getProjects() != null)) {
                return resource.get_Embedded().getProjects();
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new AntennaException("Request to search for projects with the name " + name + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360Project createProject(SW360Project sw360Project, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360Project, header);

        ResponseEntity<Resource<SW360Project>> response = this.restTemplate
                .exchange(this.projectsRestUrl,
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360Project>>() {});

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to create project " + sw360Project.getName() + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360Project getProject(String projectId, HttpHeaders header) throws AntennaException {
        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.EMPTY_MAP, header);
        ResponseEntity<Resource<SW360Project>> response =
                this.restTemplate.exchange(this.projectsRestUrl + "/" + projectId,
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360Project>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getContent();
        } else {
            throw new AntennaException("Request to get project " + projectId + " failed with "
                    + response.getStatusCode());
        }
    }

    public void addReleasesToProject(String projectId, List<String> releases, HttpHeaders header) throws AntennaException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(projectsRestUrl)
                .pathSegment(projectId, SW360Attributes.PROJECT_RELEASES);

        HttpEntity<List<String>> httpEntity = new HttpEntity<>(releases, header);

        ResponseEntity<String> response = this.restTemplate
                .exchange(builder.build(false).toUriString(),
                        HttpMethod.POST,
                        httpEntity,
                        String.class);

        if (!(response.getStatusCode() == HttpStatus.CREATED)) {
            throw new AntennaException("Request to add linked releases to project " + projectId + " failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseRelease> getLinkedReleases(String projectId, boolean transitive, HttpHeaders header) throws AntennaException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(projectsRestUrl)
                .pathSegment(projectId, SW360Attributes.PROJECT_RELEASES)
                .queryParam(SW360Attributes.PROJECT_RELEASES_TRANSITIVE, transitive);

        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360ReleaseList>> response = this.restTemplate.
                exchange(builder.build(false).toUriString(),
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360ReleaseList>>() {});
        if (response.getStatusCode() == HttpStatus.OK) {
            SW360ReleaseList resource = response.getBody().getContent();
            if ((resource.get_Embedded() != null) && (resource.get_Embedded().getReleases() != null)) {
                return resource.get_Embedded().getReleases();
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new AntennaException("Request to get linked releases of project with id=[ " + projectId + "] failed with "
                    + response.getStatusCode());
        }
    }

    public List<SW360SparseRelease> getLinkedReleases(String projectId, HttpHeaders header) throws AntennaException {
        return getLinkedReleases(projectId, false, header);
    }
}
