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
package org.eclipse.sw360.antenna.sw360.workflow;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.api.HttpClient;
import org.eclipse.sw360.antenna.http.api.HttpClientFactory;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.eclipse.sw360.antenna.http.impl.HttpClientFactoryImpl;
import org.eclipse.sw360.antenna.sw360.client.SW360ConnectionFactory;
import org.eclipse.sw360.antenna.sw360.client.api.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;

/**
 * <p>
 * A class for setting up the SW360 client library from the Antenna
 * configuration.
 * </p>
 * <p>
 * This class processes configuration settings to extract all the information
 * required by the client library. It then uses the correct factories to
 * create an {@code SW360Connection} object.
 * </p>
 */
public class SW360ConnectionConfigurationFactory {
    /**
     * The name of the configuration property that defines the base URL of the
     * REST API of the SW360 server to be accessed. The URLs of concrete
     * resources (like {@code /components} or {@code /releases} are resolved
     * relative to this URL.
     */
    public static final String REST_SERVER_URL_KEY = "rest.server.url";

    /**
     * The name of the configuration property that defines the endpoint of the
     * authentication server to obtain an access token. The value must be the
     * full URL including the {@code /token} suffix.
     */
    public static final String AUTH_SERVER_URL_KEY = "auth.server.url";

    /**
     * The name of the configuration property that sets the user name.
     * Access tokens are requested on behalf of this user.
     */
    public static final String USERNAME_KEY = "user.id";

    /**
     * The name of the configuration property for the password of the user.
     * This password is needed when requesting an access token for the current
     * user.
     */
    public static final String PASSWORD_KEY = "user.password";

    /**
     * The name of the configuration property defining the ID of the OAuth 2
     * client. This property is checked by the authorization server when an
     * access token is requested.
     */
    public static final String CLIENT_USER_KEY = "client.id";

    /**
     * The name of the configuration property defining the password of the
     * OAuth 2 client to be passed to the authorization server when requesting
     * an access token.
     */
    public static final String CLIENT_PASSWORD_KEY = "client.password";

    /**
     * The name of the configuration property that determines whether an HTTP
     * proxy should be used. Only if this property has a value of
     * <strong>true</strong>, the configured proxy host and port are used. The
     * default value is <strong>false</strong>.
     */
    public static final String PROXY_USE = "proxy.use";

    /**
     * The factory for creating a new HTTP client.
     */
    private final HttpClientFactory httpClientFactory;

    /**
     * The factory for creating a new connection.
     */
    private final SW360ConnectionFactory connectionFactory;

    /**
     * Creates a new instance of {@code SW360ConnectionConfigurationFactory}
     * with default settings.
     */
    public SW360ConnectionConfigurationFactory() {
        this(new HttpClientFactoryImpl(), new SW360ConnectionFactory());
    }

    /**
     * Creates a new instance of {@code SW360ConnectionConfigurationFactory}
     * and sets the factory objects required for the setup of the client
     * library. This constructor is mainly used for testing purposes.
     *
     * @param httpClientFactory the factory to create an HTTP client
     * @param connectionFactory the factory to create a client connection
     */
    SW360ConnectionConfigurationFactory(HttpClientFactory httpClientFactory,
                                        SW360ConnectionFactory connectionFactory) {
        this.httpClientFactory = httpClientFactory;
        this.connectionFactory = connectionFactory;
    }

    /**
     * Creates a new {@code SW360Connection} object that is initialized from
     * configuration data. The passed in {@code Getter} objects are used to
     * read in configuration settings. Other helper objects are created using
     * the factories associated with this instance.
     *
     * @param getConfigValue        getter for string config settings
     * @param getBooleanConfigValue getter for boolean config settings
     * @param proxyHost             the proxy host
     * @param proxyPort             the proxy port
     * @return the new {@code SW360Connection}
     */
    public SW360Connection createConnection(Getter<String> getConfigValue,
                                            Getter<Boolean> getBooleanConfigValue,
                                            String proxyHost, int proxyPort) {
        ProxySettings settings = createProxySettings(getBooleanConfigValue, proxyHost, proxyPort);
        String restUrl = getConfigValue.apply(REST_SERVER_URL_KEY);
        String authUrl = getConfigValue.apply(AUTH_SERVER_URL_KEY);
        String user = getConfigValue.apply(USERNAME_KEY);
        String password = getConfigValue.apply(PASSWORD_KEY);
        String clientId = getConfigValue.apply(CLIENT_USER_KEY);
        String clientPassword = getConfigValue.apply(CLIENT_PASSWORD_KEY);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HttpClient httpClient = createHttpClient(settings, mapper);

        SW360ClientConfig clientConfig =
                SW360ClientConfig.createConfig(restUrl, authUrl, user, password, clientId, clientPassword,
                        httpClient, mapper);
        return connectionFactory.newConnection(clientConfig);
    }

    /**
     * Returns the {@code HttpClientFactory} used by this object.
     *
     * @return the factory to create a new HTTP client
     */
    HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }

    /**
     * Returns the {@code SW360ConnectionFactory} used by this object.
     *
     * @return the factory to create an SW360 client connection
     */
    SW360ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Creates the HTTP client to be used for interactions with the SW360
     * server.
     *
     * @param settings proxy settings
     * @param mapper   the object mapper for JSON serialization
     * @return the HTTP client
     */
    private HttpClient createHttpClient(ProxySettings settings, ObjectMapper mapper) {
        HttpClientConfig httpClientConfig = HttpClientConfig.basicConfig()
                .withObjectMapper(mapper)
                .withProxySettings(settings);
        return httpClientFactory.newHttpClient(httpClientConfig);
    }

    /**
     * Creates the proxy settings to be used from the given configuration data.
     *
     * @param getBooleanConfigValue getter for boolean config settings
     * @param proxyHost             the proxy host
     * @param proxyPort             the proxy port
     * @return the {@code ProxySettings} to be used
     */
    private static ProxySettings createProxySettings(Getter<Boolean> getBooleanConfigValue,
                                                     String proxyHost, int proxyPort) {
        Boolean useProxy = getBooleanConfigValue.apply(PROXY_USE);
        return ProxySettings.fromConfig(useProxy, proxyHost, proxyPort);
    }

    @FunctionalInterface
    public interface Getter<T> {
        T apply(String s);
    }
}
