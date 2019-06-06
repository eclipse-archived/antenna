/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class SW360AuthenticationClient {
    private static final String AUTHORIZATION_BASIC_VALUE = "Basic ";
    private static final String AUTHORIZATION_BEARER_VALUE = "Bearer ";

    private static final String GRANT_TYPE_VALUE = "password";
    private static final String GET_ACCESS_TOKEN_ENDPOINT = "/token";
    private static final String TOKEN_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String JSON_TOKEN_KEY = "access_token";

    private final String authServerUrl;
    private RestTemplate restTemplate;

    public SW360AuthenticationClient(String authServerUrl, boolean proxyUse, String proxyHost, int proxyPort) {
        this.authServerUrl = authServerUrl;
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

    public String getOAuth2AccessToken(String username, String password, String clientId, String clientPassword)
            throws AntennaException {
        String requestUrl = authServerUrl + GET_ACCESS_TOKEN_ENDPOINT;

        String body = String.format("%s=%s&%s=%s&%s=%s", SW360Attributes.AUTHENTICATOR_GRANT_TYPE, GRANT_TYPE_VALUE,
                SW360Attributes.AUTHENTICATOR_USERNAME, username, SW360Attributes.AUTHENTICATOR_PASSWORD, password);

        HttpHeaders headers = new HttpHeaders();
        addBasicAuthentication(headers, clientId, clientPassword);
        headers.add(HttpHeaders.CONTENT_TYPE, TOKEN_CONTENT_TYPE);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = this.restTemplate
                .exchange(requestUrl,
                        HttpMethod.POST,
                        httpEntity,
                        String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                return (String) new ObjectMapper().readValue(response.getBody(), HashMap.class).get(JSON_TOKEN_KEY);
            } catch (IOException e) {
                throw new AntennaException("Error when attempting to deserialize the response body.", e);
            }
        } else {
            throw new AntennaException("Could not request OAuth2 access token for [" + username + "].");
        }
    }

    private HttpHeaders addBasicAuthentication(HttpHeaders headers, String liferayClientId, String liferayClientPassword) {
        String liferayClient = liferayClientId + ":" + liferayClientPassword;
        String base64ClientCredentials = Base64.getEncoder().encodeToString(liferayClient.getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BASIC_VALUE + base64ClientCredentials);
        return headers;
    }

    public HttpHeaders getHeadersWithBearerToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BEARER_VALUE + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }
}
