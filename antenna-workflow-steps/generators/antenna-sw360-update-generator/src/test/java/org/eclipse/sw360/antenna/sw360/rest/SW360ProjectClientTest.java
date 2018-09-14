/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
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
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class SW360ProjectClientTest {
    private static final String REST_URL = "http://localhost:8080/resource/api";
    private static final String PROJECTS_ENDPOINT = REST_URL + "/projects";
    private static final String SEARCH_BY_NAME_ENDPOINT = PROJECTS_ENDPOINT + "?name=";

    // JSON property keys
    private static final String EMBEDDED_KEY = "_embedded";
    private static final String LINKS_KEY = "_links";
    private static final String SELF_KEY = "self";
    private static final String HREF_KEY = "href";
    private static final String SW360_PROJECTS_KEY = "sw360:projects";
    private static final String PROJECT_NAME_KEY = "name";
    private static final String PROJECT_PROJECT_TYPE_KEY = "projectType";
    private static final String PROJECT_VERSION_KEY = "version";
    private static final String PROJECT_TYPE_KEY = "type";
    private static final String PROJECT_CREATED_ON_KEY = "createdOn";
    private static final String PROJECT_CREATED_BY_KEY = "createdBy";
    private static final String PROJECT_EMAIL_KEY = "email";

    // Test component data
    // Example project 1
    private static final String PROJECT_NAME_VALUE_1 = "test.project.name1";
    private static final String PROJECT_PROJECT_TYPE_VALUE_1 = "PRODUCT";
    private static final String PROJECT_VERSION_VALUE_1 = "1.0-SNAPSHOT";
    private static final String PROJECT_EMAIL_VALUE_1 = "testemail1@any.com";

    // Example project 2
    private static final String PROJECT_NAME_VALUE_2 = "test.project.name2";
    private static final String PROJECT_PROJECT_TYPE_VALUE_2 = "SERVICE";
    private static final String PROJECT_VERSION_VALUE_2 = "2.5-RELEASE";
    private static final String PROJECT_EMAIL_VALUE_2 = "testemail2@any.com";


    SW360ProjectClient client = new SW360ProjectClient(REST_URL);

    private MockRestServiceServer mockedServer;

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockedServer = MockRestServiceServer.createServer(restTemplate);
        ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
    }


    @Test
    public void testSearchProjectsByNameWithSuccess() throws AntennaException, JsonProcessingException {
        // project 1
        JsonObjectBuilder projectJsonObj1 = Json.createObjectBuilder()
                .add(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_1)
                .add(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_1)
                .add(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_1);

        // project 2
        JsonObjectBuilder projectJsonObj2 = Json.createObjectBuilder()
                .add(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_2)
                .add(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_2)
                .add(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_2);

        // projects array
        JsonArrayBuilder projectsJsonArray = Json.createArrayBuilder()
                .add(projectJsonObj1)
                .add(projectJsonObj2);
        JsonObjectBuilder sw360ProjectsList = Json.createObjectBuilder()
                .add(SW360_PROJECTS_KEY, projectsJsonArray);

        String expectedResponseBody = Json.createObjectBuilder()
                .add(EMBEDDED_KEY, sw360ProjectsList)
                .build()
                .toString();

        String searchName = "test";
        String requestUrl = SEARCH_BY_NAME_ENDPOINT + searchName;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        List<SW360Project> projectsList;
        projectsList = client.searchByName(searchName, new HttpHeaders());
        Supplier<Stream<SW360Project>> streamSupplier = () -> projectsList.stream();

        assertEquals(2, projectsList.size());
        assertTrue(streamSupplier
                .get()
                .anyMatch(p -> p.getName().equals(PROJECT_NAME_VALUE_1)));
        assertTrue(streamSupplier
                .get()
                .anyMatch(p -> p.getProjectType().toString().equals(PROJECT_PROJECT_TYPE_VALUE_1)));
        assertTrue(streamSupplier
                .get()
                .anyMatch(p -> p.getVersion().equals(PROJECT_VERSION_VALUE_1)));
        assertTrue(streamSupplier
                .get()
                .anyMatch(p -> p.getName().equals(PROJECT_NAME_VALUE_2)));
        assertTrue(streamSupplier
                .get()
                .anyMatch(p -> p.getProjectType().toString().equals(PROJECT_PROJECT_TYPE_VALUE_2)));
        assertTrue(streamSupplier
                .get()
                .anyMatch(p -> p.getVersion().equals(PROJECT_VERSION_VALUE_2)));
    }

    @Test( expected = RestClientException.class )
    public void testSearchProjectsByNameWithNotParseableRespond() throws AntennaException, JsonProcessingException {
        String expectedResponseBody = "NotParseableJsonString";
        String searchName = "test";
        String requestUrl = SEARCH_BY_NAME_ENDPOINT + searchName;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));
        client.searchByName(searchName, new HttpHeaders());
    }

    @Test( expected = HttpClientErrorException.class )
    public void testSearchProjectsByNameWithBadStatusCode() throws AntennaException, JsonProcessingException {
        String searchName = "test";
        String requestUrl = SEARCH_BY_NAME_ENDPOINT + searchName;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        client.searchByName(searchName, new HttpHeaders());
    }

    @Test
    public void testCreateProjectWithSuccess() throws AntennaException, JsonProcessingException {
        String newProjectId = "anyNewId";
        String newHref = PROJECTS_ENDPOINT + "/" + newProjectId;
        SW360Project requestProject = new SW360Project()
                .setName(PROJECT_NAME_VALUE_1)
                .setProjectType(SW360ProjectType.valueOf(PROJECT_PROJECT_TYPE_VALUE_1))
                .setVersion(PROJECT_VERSION_VALUE_1);

        // _links property
        JsonObjectBuilder selfContent = Json.createObjectBuilder()
                .add(HREF_KEY, newHref);
        JsonObjectBuilder linksObj = Json.createObjectBuilder()
                .add(SELF_KEY, selfContent);


        // _embedded property
        JsonObjectBuilder createdByContent = Json.createObjectBuilder()
                .add(PROJECT_EMAIL_KEY, PROJECT_EMAIL_VALUE_1);
        JsonObjectBuilder createdByObj = Json.createObjectBuilder()
                .add(PROJECT_CREATED_BY_KEY, createdByContent);

        String expectedResponseBody = Json.createObjectBuilder()
                .add(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_1)
                .add(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_1)
                .add(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_1)
                .add(LINKS_KEY, linksObj)
                .add(EMBEDDED_KEY, createdByObj)
                .build()
                .toString();

        mockedServer.expect(requestTo(PROJECTS_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body(expectedResponseBody)
                        .contentType(MediaType.APPLICATION_JSON));
        SW360Project<LinkObjects, SW360ProjectEmbedded> project = client.createProject(requestProject, new HttpHeaders());

        assertNotNull(project);
        assertEquals(PROJECT_NAME_VALUE_1, project.getName());
        assertEquals(PROJECT_PROJECT_TYPE_VALUE_1, project.getProjectType().toString());
        assertEquals(PROJECT_VERSION_VALUE_1, project.getVersion());

        // _links assertions
        assertNotNull(project.get_Links());
        assertNotNull(project.get_Links().getSelf());
        assertEquals(newHref, project.get_Links().getSelf().getHref());

        // _embedded assertions
        assertNotNull(project.get_Embedded());
        assertNotNull(project.get_Embedded().getCreatedBy());
        assertEquals(PROJECT_EMAIL_VALUE_1, project.get_Embedded().getCreatedBy().getEmail());
    }

    @Test( expected = HttpClientErrorException.class )
    public void testCreateProjectWithBadStatusCode() throws AntennaException, JsonProcessingException {
        mockedServer.expect(requestTo(PROJECTS_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());
        client.createProject(new SW360Project(), new HttpHeaders());
    }

    @Test
    public void testGetProjectWithSuccess() throws AntennaException, JsonProcessingException {
        String projectId = "anyId";
        String requestUrl = PROJECTS_ENDPOINT + "/" + projectId;
        String expectedResponseBody = Json.createObjectBuilder()
                .add(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_1)
                .add(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_1)
                .add(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_1)
                .build()
                .toString();

        mockedServer.expect(requestTo(requestUrl))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        SW360Project project = client.getProject(projectId, new HttpHeaders());

        assertNotNull(project);
        assertEquals(PROJECT_NAME_VALUE_1, project.getName());
        assertEquals(PROJECT_PROJECT_TYPE_VALUE_1, project.getProjectType().toString());
        assertEquals(PROJECT_VERSION_VALUE_1, project.getVersion());
    }

    @Test( expected = HttpClientErrorException.class )
    public void testGetProjectWithBadStatusCode() throws AntennaException, JsonProcessingException {
        String projectId = "anyId";
        String requestUrl = PROJECTS_ENDPOINT + "/" + projectId;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());
        client.getProject(projectId, new HttpHeaders());
    }
}
