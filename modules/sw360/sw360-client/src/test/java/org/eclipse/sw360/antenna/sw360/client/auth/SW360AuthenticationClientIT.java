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
package org.eclipse.sw360.antenna.sw360.client.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.sw360.antenna.http.utils.HttpUtils;
import org.eclipse.sw360.antenna.sw360.rest.AbstractMockServerTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.http.utils.HttpConstants.CONTENT_TYPE_FORM;
import static org.eclipse.sw360.antenna.http.utils.HttpConstants.HEADER_CONTENT_TYPE;
import static org.eclipse.sw360.antenna.http.utils.HttpConstants.STATUS_ERR_SERVER;
import static org.eclipse.sw360.antenna.http.utils.HttpConstants.STATUS_OK;

public class SW360AuthenticationClientIT extends AbstractMockServerTest {
    private static final String ACCESS_TOKEN = "theSecretAccessToken";

    private SW360AuthenticationClient authenticationClient;

    @Before
    public void setUp() {
        authenticationClient = new SW360AuthenticationClient(createClientConfig());
    }

    @Test
    public void testGetOAuth2AccessToken() throws IOException {
        wireMockRule.stubFor(post(urlPathEqualTo(TOKEN_ENDPOINT))
                .withRequestBody(equalTo("grant_type=password&username=" + USER + "&password=" + PASSWORD))
                .withHeader(HEADER_CONTENT_TYPE, containing(CONTENT_TYPE_FORM))
                .withBasicAuth(CLIENT_ID, CLIENT_PASSWORD)
                .willReturn(aJsonResponse(STATUS_OK)
                        .withBody("{\"access_token\": \"" + ACCESS_TOKEN + "\"}")));

        String accessToken = HttpUtils.waitFor(authenticationClient.getOAuth2AccessToken());
        assertThat(accessToken).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    public void testGetOAuth2AccessTokenFailure() {
        wireMockRule.stubFor(post(urlPathEqualTo(TOKEN_ENDPOINT))
                .willReturn(aJsonResponse(STATUS_ERR_SERVER)));

        CompletableFuture<String> futToken = authenticationClient.getOAuth2AccessToken();
        IOException exception = extractException(futToken, IOException.class);
        assertThat(exception.getMessage()).contains(String.valueOf(STATUS_ERR_SERVER));
    }

    @Test
    public void testGetOAuth2AccessTokenUnexpectedResponse() {
        wireMockRule.stubFor(post(urlPathEqualTo(TOKEN_ENDPOINT))
                .willReturn(aJsonResponse(STATUS_OK)
                        .withBody("{\"unknown_property\": \"" + ACCESS_TOKEN + "\"}")));

        CompletableFuture<String> futToken = authenticationClient.getOAuth2AccessToken();
        extractException(futToken, IOException.class);
    }

    @Test
    public void testGetOAuth2AccessTokenNoJsonResponse() {
        wireMockRule.stubFor(post(urlPathEqualTo(TOKEN_ENDPOINT))
                .willReturn(aJsonResponse(STATUS_OK)
                        .withBody("This is no JSON")));

        CompletableFuture<String> futToken = authenticationClient.getOAuth2AccessToken();
        extractException(futToken, JsonProcessingException.class);
    }
}
