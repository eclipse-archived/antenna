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

package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectEmbedded;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SW360ProjectClient {
    private static final String PROJECTS_ENDPOINT = "/projects";

    private RestTemplate restTemplate = new RestTemplate();

    private String projectsRestUrl;

    public SW360ProjectClient(String restUrl) {
        projectsRestUrl = restUrl + PROJECTS_ENDPOINT;
    }

    public List<SW360Project> searchByName(String name, HttpHeaders header) throws JsonProcessingException, AntennaException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(projectsRestUrl)
                .queryParam(SW360Attributes.PROJECT_SEARCH_BY_NAME, name);

        HttpEntity<String> httpEntity = RestUtils.getHttpEntity(Collections.emptyMap(), header);

        ResponseEntity<Resource<SW360Project>> response = this.restTemplate.
                exchange(builder.build(false).toUriString(),
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<Resource<SW360Project>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            SW360Project<LinkObjects, SW360ProjectEmbedded> resource = response.getBody().getContent();
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

    public SW360Project createProject(SW360Project sw360Project, HttpHeaders header) throws JsonProcessingException, AntennaException {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ProjectToHttpEntity(sw360Project, header);

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

    public SW360Project getProject(String projectId, HttpHeaders header) throws JsonProcessingException, AntennaException {
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
}
