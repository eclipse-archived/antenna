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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class SW360AuthenticationClient {
    private final String GRANT_TYPE_VALUE = "password";
    private final String LIFERAY_CLIENT_CREDENTIALS = "trusted-sw360-client:sw360-secret";

    private final String AUTHORIZATION_BASIC_VALUE = "Basic ";
    private final String AUTHORIZATION_BEARER_VALUE = "Bearer ";

    private final String GET_ACCESS_TOKEN_ENDPOINT = "/oauth/token";
    private final String CHECK_ACCESS_TOKEN_ENDPOINT = "/oauth/check_token";


    private final String authServerUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public SW360AuthenticationClient(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public String getOAuth2AccessToken(String username, String password) throws AntennaException {
        String requestUrl = authServerUrl + GET_ACCESS_TOKEN_ENDPOINT;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(requestUrl)
                .queryParam(SW360Attributes.AUTHENTICATOR_GRANT_TYPE, GRANT_TYPE_VALUE)
                .queryParam(SW360Attributes.AUTHENTICATOR_USERNAME, username)
                .queryParam(SW360Attributes.AUTHENTICATOR_PASSWORD, password);

        HttpHeaders headers = new HttpHeaders();
            addBasicAuthentication(headers, LIFERAY_CLIENT_CREDENTIALS);

            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = this.restTemplate
                    .exchange(builder.toUriString(),
                            HttpMethod.POST,
                            httpEntity,
                            String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    return (String) new ObjectMapper().readValue(response.getBody(), HashMap.class).get("access_token");
                } catch (IOException e) {
                    throw new AntennaException("Error when attempting to deserialise the response body.", e);
                }
            } else {
                throw new AntennaException("Could not request OAuth2 access token for [" + username + "].");
            }
    }

    private HttpHeaders addBasicAuthentication(HttpHeaders headers, String liferayClient) {
        String base64ClientCredentials = Base64.getEncoder().encodeToString(liferayClient.getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BASIC_VALUE + base64ClientCredentials );
        return headers;
    }

    private HttpHeaders addBearerAuthentication(HttpHeaders headers, String oAuth2AccessToken) {
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BEARER_VALUE + oAuth2AccessToken);
        return headers;
    }

    public HttpHeaders getHeadersForAccessToken() {
        String clientCredentials = "trusted-sw360-client:sw360-secret";
        String base64ClientCredentials =
                Base64.getEncoder().encodeToString(clientCredentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BASIC_VALUE + base64ClientCredentials);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public HttpHeaders getHeadersWithBearerToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BEARER_VALUE + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
