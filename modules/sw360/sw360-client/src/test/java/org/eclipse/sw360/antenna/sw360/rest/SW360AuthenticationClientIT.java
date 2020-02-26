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
package org.eclipse.sw360.antenna.sw360.rest;

import org.apache.http.HttpStatus;
import org.eclipse.sw360.antenna.sw360.utils.SW360ClientException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class SW360AuthenticationClientIT extends AbstractMockServerTest {
    private static final String ACCESS_TOKEN = "theSecretAccessToken";

    private SW360AuthenticationClient authenticationClient;

    @Before
    public void setUp() {
        authenticationClient = new SW360AuthenticationClient(wireMockRule.baseUrl(), createRestTemplate());
    }

    @Test
    public void testGetOAuth2AccessToken() {
        final String user = "scott";
        final String password = "tiger";
        final String clientId = "testClient";
        final String clientPassword = "testClientPass";
        wireMockRule.stubFor(post(urlPathEqualTo("/token"))
                .withRequestBody(equalTo("grant_type=password&username=" + user + "&password=" + password))
                .withBasicAuth(clientId, clientPassword)
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBody("{\"access_token\": \"" + ACCESS_TOKEN + "\"}")));

        String accessToken = authenticationClient.getOAuth2AccessToken(user, password, clientId, clientPassword);
        assertThat(accessToken).isEqualTo(ACCESS_TOKEN);
    }

    @Test(expected = SW360ClientException.class)
    public void testGetOAuth2AccessTokenFailure() {
        wireMockRule.stubFor(post(urlPathEqualTo("/token"))
                .willReturn(aJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        authenticationClient.getOAuth2AccessToken("u", "p", "c", "cp");
    }

    @Test
    public void testHeadersWithBearerToken() {
        HttpHeaders headers = authenticationClient.getHeadersWithBearerToken(ACCESS_TOKEN);

        assertThat(headers.get("Authorization")).containsExactly("Bearer " + ACCESS_TOKEN);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(headers.getAccept()).containsExactly(MediaType.APPLICATION_JSON_UTF8);
    }
}
