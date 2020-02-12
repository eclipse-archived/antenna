/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
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
    private static final String USERS_ENDPOINT = REST_URL + "/users";
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


    private SW360ProjectClient client;

    private MockRestServiceServer mockedServer;

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockedServer = MockRestServiceServer.createServer(restTemplate);
        client = new SW360ProjectClient(REST_URL, restTemplate);
    }


    @Test
    public void testSearchProjectsByNameWithSuccess() {
        // project 1
        JsonObject projectJsonObj1 = new JsonObject(new HashMap<String, String>() {{
            put(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_1);
            put(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_1);
            put(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_1);
        }});

        // project 2
        JsonObject projectJsonObj2 = new JsonObject(new HashMap<String, String>() {{
            put(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_2);
            put(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_2);
            put(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_2);
        }});

        // projects array
        JsonArray projectsJsonArray = new JsonArray(Arrays.asList(projectJsonObj1, projectJsonObj2));

        JsonObject sw360ProjectsList = new JsonObject(new HashMap<String, JsonArray>() {{
            put(SW360_PROJECTS_KEY, projectsJsonArray);
        }});

        String expectedResponseBody = new JsonObject(new HashMap<String, JsonObject>() {{
            put(EMBEDDED_KEY, sw360ProjectsList);
        }}).toJson();

        String searchName = "test";
        String requestUrl = SEARCH_BY_NAME_ENDPOINT + searchName;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        List<SW360Project> projectsList;
        projectsList = client.searchByName(searchName, new HttpHeaders());
        Supplier<Stream<SW360Project>> streamSupplier = projectsList::stream;

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
    public void testSearchProjectsByNameWithNotParseableRespond() {
        String expectedResponseBody = "NotParseableJsonString";
        String searchName = "test";
        String requestUrl = SEARCH_BY_NAME_ENDPOINT + searchName;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));
        client.searchByName(searchName, new HttpHeaders());
    }

    @Test( expected = HttpClientErrorException.class )
    public void testSearchProjectsByNameWithBadStatusCode() {
        String searchName = "test";
        String requestUrl = SEARCH_BY_NAME_ENDPOINT + searchName;
        mockedServer.expect(requestTo(requestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        client.searchByName(searchName, new HttpHeaders());
    }

    @Test
    public void testCreateProjectWithSuccess() {
        String newProjectId = "anyNewId";
        String newHref = PROJECTS_ENDPOINT + "/" + newProjectId;
        SW360Project requestProject = new SW360Project()
                .setName(PROJECT_NAME_VALUE_1)
                .setProjectType(SW360ProjectType.valueOf(PROJECT_PROJECT_TYPE_VALUE_1))
                .setVersion(PROJECT_VERSION_VALUE_1);

        // _links property
        JsonObject selfContent = new JsonObject(new HashMap<String, String>() {{
            put(HREF_KEY, newHref);
        }});
        JsonObject linksObj = new JsonObject(new HashMap<String, JsonObject>() {{
            put(SELF_KEY, selfContent);
        }});

        // _embedded property
        JsonObject createdBySelfContent = new JsonObject(new HashMap<String, String>() {{
            put(HREF_KEY, USERS_ENDPOINT + "/12345");
        }});
        JsonObject createdByLinksObj = new JsonObject(new HashMap<String, JsonObject>() {{
            put(SELF_KEY, createdBySelfContent);
        }});
        JsonObject createdByContent = new JsonObject(new HashMap<String, Object>() {{
            put(PROJECT_EMAIL_KEY, PROJECT_EMAIL_VALUE_1);
            put(LINKS_KEY, createdByLinksObj);
        }});
        JsonObject createdByObj = new JsonObject(new HashMap<String, JsonObject>() {{
            put(PROJECT_CREATED_BY_KEY, createdByContent);
        }});

        String expectedResponseBody = new JsonObject(new HashMap<String, Object>() {{
            put(PROJECT_NAME_KEY, PROJECT_NAME_VALUE_1);
            put(PROJECT_PROJECT_TYPE_KEY, PROJECT_PROJECT_TYPE_VALUE_1);
            put(PROJECT_VERSION_KEY, PROJECT_VERSION_VALUE_1);
            put(LINKS_KEY, linksObj);
            put(EMBEDDED_KEY, createdByObj);
        }}).toJson();

        mockedServer.expect(requestTo(PROJECTS_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body(expectedResponseBody)
                        .contentType(MediaType.APPLICATION_JSON));
        SW360Project project = client.createProject(requestProject, new HttpHeaders());

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

    @Test
    public void testCreateProjectWithBadStatusCode() {
        mockedServer.expect(requestTo(PROJECTS_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());
        SW360Project sw360Project = new SW360Project();
        SW360Project projectAfter = client.createProject(sw360Project, new HttpHeaders());
        assertEquals(sw360Project.hashCode(), projectAfter.hashCode());
        assertEquals(sw360Project, projectAfter);
    }
}
