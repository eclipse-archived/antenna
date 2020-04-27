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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ConnectionFactory;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void testDefaultConnectionFactoryIsCreated() {
        SW360ConnectionConfigurationFactory factory =
                new SW360ConnectionConfigurationFactory();

        assertThat(factory.getConnectionFactory()).isNotNull();
    }

    @Test
    public void testConnectionCreation() {
        HttpClient httpClient = mock(HttpClient.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        SW360ConnectionFactory connectionFactory = mock(SW360ConnectionFactory.class);
        SW360Connection connection = mock(SW360Connection.class);
        when(connectionFactory.newConnection(any())).thenReturn(connection);
        SW360ConnectionConfigurationFactory factory = new SW360ConnectionConfigurationFactory(connectionFactory);

        SW360Connection actualConnection =
                factory.createConnection(stringConfigGetter(), httpClient, mapper);
        assertThat(actualConnection).isEqualTo(connection);

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
        assertThat(sw360ClientConfig.getObjectMapper()).isEqualTo(mapper);
    }
}
