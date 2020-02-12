/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class SW360ClientTest {
    private static final String TEST_URI = "https://test.server.org/v1/foo";
    private static final String TEST_RESPONSE = "ok";
    private static final String HEADER_NAME = "X-Test";
    private static final String HEADER_VALUE = "success";

    private static final ParameterizedTypeReference<String> TYPE_REFERENCE = new ParameterizedTypeReference<String>() {
    };

    private MockRestServiceServer mockServer;

    private SW360Client client;

    @Before
    public void setUp() {
        RestTemplate template = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(template);
        client = new SW360Client(template) {
            @Override
            public String getEndpoint() {
                return null;
            }
        };
    }

    /**
     * Returns a map with test headers to check whether headers are passed
     * correctly.
     *
     * @return the test headers
     */
    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_NAME, HEADER_VALUE);
        return headers;
    }

    /**
     * Checks a test response returned by the mock server.
     *
     * @param response the response to be checked
     */
    private static void checkTestResponse(ResponseEntity<String> response) {
        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody()).isEqualTo(TEST_RESPONSE);
    }

    @Test
    public void testRestGET() {
        mockServer.expect(requestTo(TEST_URI))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HEADER_NAME, HEADER_VALUE))
                .andExpect(content().string(""))
                .andRespond(withSuccess(TEST_RESPONSE, MediaType.TEXT_PLAIN));

        ResponseEntity<String> response = client.doRestGET(TEST_URI, createHeaders(), TYPE_REFERENCE);
        checkTestResponse(response);
    }

    @Test
    public void testRestPOST() {
        String content = "test POST content";
        HttpEntity<String> entity = new HttpEntity<>(content, createHeaders());
        mockServer.expect(requestTo(TEST_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HEADER_NAME, HEADER_VALUE))
                .andExpect(content().string(content))
                .andRespond(withSuccess(TEST_RESPONSE, MediaType.TEXT_PLAIN));

        ResponseEntity<String> response = client.doRestPOST(TEST_URI, entity, TYPE_REFERENCE);
        checkTestResponse(response);
    }

    @Test
    public void testRestPATCH() {
        String content = "the PATCH content";
        HttpEntity<String> entity = new HttpEntity<>(content, createHeaders());
        mockServer.expect(requestTo(TEST_URI))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(HEADER_NAME, HEADER_VALUE))
                .andExpect(content().string(content))
                .andRespond(withSuccess(TEST_RESPONSE, MediaType.TEXT_PLAIN));

        ResponseEntity<String> response = client.doRestPATCH(TEST_URI, entity, TYPE_REFERENCE);
        checkTestResponse(response);
    }
}