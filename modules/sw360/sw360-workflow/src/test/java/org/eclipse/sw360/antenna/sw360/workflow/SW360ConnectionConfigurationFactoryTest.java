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

import org.eclipse.sw360.antenna.http.api.HttpClient;
import org.eclipse.sw360.antenna.http.api.HttpClientFactory;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.eclipse.sw360.antenna.http.impl.HttpClientFactoryImpl;
import org.eclipse.sw360.antenna.sw360.client.SW360ConnectionFactory;
import org.eclipse.sw360.antenna.sw360.client.api.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360ConnectionConfigurationFactoryTest {
    private static final String REST_URL = "https://www.sw360.org/api";
    private static final String AUTH_URL = "https://auth.sw360.org/token";
    private static final String USER = "scott";
    private static final String PASSWORD = "tiger";
    private static final String CLIENT_ID = "oauth_client";
    private static final String CLIENT_SECRET = "oauth_client_secret";
    private static final String PROXY_HOST = "my.proxy.org";
    private static final int PROXY_PORT = 8888;

    /**
     * Returns an object allowing access to string-based test configuration
     * properties.
     *
     * @return the accessor for properties
     */
    private static SW360ConnectionConfigurationFactory.Getter<String> stringConfigGetter() {
        Map<String, String> props = new HashMap<>();
        props.put(SW360ConnectionConfigurationFactory.REST_SERVER_URL_KEY, REST_URL);
        props.put(SW360ConnectionConfigurationFactory.AUTH_SERVER_URL_KEY, AUTH_URL);
        props.put(SW360ConnectionConfigurationFactory.USERNAME_KEY, USER);
        props.put(SW360ConnectionConfigurationFactory.PASSWORD_KEY, PASSWORD);
        props.put(SW360ConnectionConfigurationFactory.CLIENT_USER_KEY, CLIENT_ID);
        props.put(SW360ConnectionConfigurationFactory.CLIENT_PASSWORD_KEY, CLIENT_SECRET);

        return props::get;
    }

    /**
     * Returns an object allowing access to boolean test configuration
     * properties. The only boolean property that is supported is the flag
     * whether a proxy should be used.
     *
     * @param useProxy the proxy flag
     * @return the accessor for boolean properties
     */
    private static SW360ConnectionConfigurationFactory.Getter<Boolean> booleanConfigGetter(boolean useProxy) {
        return key -> {
            if (!SW360ConnectionConfigurationFactory.PROXY_USE.equals(key)) {
                fail("Unsupported boolean property: " + key);
            }
            return useProxy;
        };
    }

    @Test
    public void testDefaultHttClientFactoryIsCreated() {
        SW360ConnectionConfigurationFactory factory = new SW360ConnectionConfigurationFactory();

        assertThat(factory.getHttpClientFactory()).isInstanceOf(HttpClientFactoryImpl.class);
    }

    @Test
    public void testDefaultConnectionFactoryIsCreated() {
        SW360ConnectionConfigurationFactory factory = new SW360ConnectionConfigurationFactory();

        assertThat(factory.getConnectionFactory()).isNotNull();
    }

    /**
     * Tests the creation of a client connection using the parameters
     * specified.
     *
     * @param useProxy         flag whether a proxy should be used
     * @param expProxySettings the expected proxy settings
     */
    private static void checkConnectionCreation(boolean useProxy, ProxySettings expProxySettings) {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        SW360ConnectionFactory connectionFactory = mock(SW360ConnectionFactory.class);
        SW360Connection connection = mock(SW360Connection.class);
        when(httpClientFactory.newHttpClient(any())).thenReturn(httpClient);
        when(connectionFactory.newConnection(any())).thenReturn(connection);
        SW360ConnectionConfigurationFactory factory =
                new SW360ConnectionConfigurationFactory(httpClientFactory, connectionFactory);

        SW360Connection actualConnection =
                factory.createConnection(stringConfigGetter(), booleanConfigGetter(useProxy),
                        PROXY_HOST, PROXY_PORT);
        assertThat(actualConnection).isEqualTo(connection);

        ArgumentCaptor<HttpClientConfig> captHttConfig = ArgumentCaptor.forClass(HttpClientConfig.class);
        verify(httpClientFactory).newHttpClient(captHttConfig.capture());
        HttpClientConfig httpClientConfig = captHttConfig.getValue();
        assertThat(httpClientConfig.proxySettings()).isEqualTo(expProxySettings);

        ArgumentCaptor<SW360ClientConfig> captSW360Config = ArgumentCaptor.forClass(SW360ClientConfig.class);
        verify(connectionFactory).newConnection(captSW360Config.capture());
        SW360ClientConfig sw360ClientConfig = captSW360Config.getValue();
        assertThat(sw360ClientConfig.getRestURL()).isEqualTo(REST_URL);
        assertThat(sw360ClientConfig.getAuthURL()).isEqualTo(AUTH_URL);
        assertThat(sw360ClientConfig.getUser()).isEqualTo(USER);
        assertThat(sw360ClientConfig.getPassword()).isEqualTo(PASSWORD);
        assertThat(sw360ClientConfig.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(sw360ClientConfig.getClientPassword()).isEqualTo(CLIENT_SECRET);
        assertThat(sw360ClientConfig.getHttpClient()).isEqualTo(httpClient);
        assertThat(sw360ClientConfig.getObjectMapper()).isEqualTo(httpClientConfig.getOrCreateObjectMapper());
    }

    @Test
    public void testCreateConnectionWithoutProxy() {
        checkConnectionCreation(false, ProxySettings.noProxy());
    }

    @Test
    public void testCreateConnectionWithProxy() {
        checkConnectionCreation(true, ProxySettings.useProxy(PROXY_HOST, PROXY_PORT));
    }
}
