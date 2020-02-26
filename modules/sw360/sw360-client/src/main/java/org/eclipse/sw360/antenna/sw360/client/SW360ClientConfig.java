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
package org.eclipse.sw360.antenna.sw360.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.eclipse.sw360.antenna.http.api.HttpClient;

import java.util.Objects;

/**
 * <p>
 * A class that stores the configuration settings for the SW360 client library.
 * </p>
 * <p>
 * An instance of this class holds all the information required to interact
 * with a specific SW360 instance, such as the base URL or credentials. It also
 * contains an {@code HttpClient} object that is to be used for sending
 * requests to the SW360 server. All of this information is mandatory; so the
 * factory method that creates new instances checks whether all settings are
 * present.
 * </p>
 * <p>
 * Authentication against the SW360 server is done via the OAuth 2.0 Password
 * Grant. So the credentials must be provided as well as data to uniquely
 * identify the OAuth client. Based on this information, the SW360 client
 * library can request the mandatory access tokens.
 * </p>
 * <p>
 * Implementation note: Instances are immutable and can be shared between
 * multiple components.
 * </p>
 */
public final class SW360ClientConfig {
    /**
     * The base URL of the SW360 REST API.
     */
    private final String restURL;

    /**
     * The URL of the token endpoint to query access tokens.
     */
    private final String authURL;

    /**
     * The user name of the SW360 credentials.
     */
    private final String user;

    /**
     * The password of the SW360 credentials.
     */
    private final String password;

    /**
     * The SW360 OAuth client ID.
     */
    private final String clientId;

    /**
     * The SW360 OAuth client password.
     */
    private final String clientPassword;

    /**
     * The HTTP client to interact with the SW360 server.
     */
    private final HttpClient httpClient;

    /**
     * The JSON object mapper.
     */
    private final ObjectMapper objectMapper;

    private SW360ClientConfig(String restURL, String authURL, String user, String password, String clientId,
                              String clientPassword, HttpClient httpClient, ObjectMapper objectMapper) {
        this.restURL = restURL;
        this.authURL = authURL;
        this.user = user;
        this.password = password;
        this.clientId = clientId;
        this.clientPassword = clientPassword;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new instance of {@code SW360ClientConfig} with the settings
     * provided.
     *
     * @param restURL        the base URL for REST requests to the SW360 instance
     * @param authURL        the URL to request access tokens
     * @param user           the SW360 user
     * @param password       the password of the user
     * @param clientId       the ID of the OAuth client to obtain an access token
     * @param clientPassword the password of the OAuth client
     * @param httpClient     the HTTP client to interact with the SW360 server
     * @param mapper         the JSON object mapper
     * @return the newly created instance
     * @throws NullPointerException     if a required parameter is missing
     * @throws IllegalArgumentException if a required string parameter is empty
     */
    public static SW360ClientConfig createConfig(String restURL, String authURL, String user,
                                                 String password, String clientId, String clientPassword,
                                                 HttpClient httpClient, ObjectMapper mapper) {
        return new SW360ClientConfig(
                Validate.notEmpty(restURL, "Undefined REST URL"),
                Validate.notEmpty(authURL, "Undefined authentication URL"),
                Validate.notEmpty(user, "Undefined user"),
                Validate.notEmpty(password, "Undefined password"),
                Validate.notEmpty(clientId, "Undefined client ID"),
                Validate.notEmpty(clientPassword, "Undefined client password"),
                Validate.notNull(httpClient),
                Validate.notNull(mapper));
    }

    /**
     * Returns the base REST endpoint of the SW360 server to be accessed.
     * Concrete REST requests append the resource to be queried to this base
     * URL.
     *
     * @return the SW360 base REST URL
     */
    public String getRestURL() {
        return restURL;
    }

    /**
     * Returns the URL to obtain an access token. To authenticate itself, this
     * client sends a query to this URL following the OAuth 2.0 Password Grant.
     *
     * @return the URL for querying access tokens
     */
    public String getAuthURL() {
        return authURL;
    }

    /**
     * Returns the user name to login into the SW360 server. This string is
     * passed as user name parameter to the OAuth 2.0 Password Grant.
     *
     * @return the user name
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the password to login into the SW360 server. This string is
     * passed as password parameter to the OAuth 2.0 Password Grant.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the OAuth client ID. This information is needed to obtain a
     * valid access token.
     *
     * @return the OAuth client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns the OAuth client password. This information is needed to obtain
     * a valid access token.
     *
     * @return the OAuth client password
     */
    public String getClientPassword() {
        return clientPassword;
    }

    /**
     * Returns a fully configured HTTP client to send requests to the SW360
     * server.
     *
     * @return the client for sending HTTP requests
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Returns the JSON object mapper to be used by the library. The
     * communication with the SW360 server uses JSON as protocol. This mapper
     * instance is used to serialize or deserialize the message payloads.
     *
     * @return the JSON object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SW360ClientConfig that = (SW360ClientConfig) o;
        return getRestURL().equals(that.getRestURL()) &&
                getAuthURL().equals(that.getAuthURL()) &&
                getUser().equals(that.getUser()) &&
                getPassword().equals(that.getPassword()) &&
                getClientId().equals(that.getClientId()) &&
                getClientPassword().equals(that.getClientPassword()) &&
                getHttpClient().equals(that.getHttpClient()) &&
                getObjectMapper().equals(that.getObjectMapper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRestURL(), getAuthURL(), getUser(), getPassword(), getClientId(), getClientPassword(),
                getHttpClient(), getObjectMapper());
    }
}
