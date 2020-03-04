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
        String restUrl = getConfigValue.apply(SW360ConnectionConfiguration.REST_SERVER_URL_KEY);
        String authUrl = getConfigValue.apply(SW360ConnectionConfiguration.AUTH_SERVER_URL_KEY);
        String user = getConfigValue.apply(SW360ConnectionConfiguration.USERNAME_KEY);
        String password = getConfigValue.apply(SW360ConnectionConfiguration.PASSWORD_KEY);
        String clientId = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_USER_KEY);
        String clientPassword = getConfigValue.apply(SW360ConnectionConfiguration.CLIENT_PASSWORD_KEY);
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
        Boolean useProxy = getBooleanConfigValue.apply(SW360ConnectionConfiguration.PROXY_USE);
        return ProxySettings.fromConfig(useProxy, proxyHost, proxyPort);
    }

    @FunctionalInterface
    public interface Getter<T> {
        T apply(String s);
    }
}
