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

import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.rest.SW360Client;
import org.eclipse.sw360.antenna.sw360.rest.SW360RestTemplateFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
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
     * Mock for the Rest template.
     */
    private RestTemplate template;

    @Before
    public void setUp() {
        template = mock(RestTemplate.class);
    }

    /**
     * Returns an object allowing access to string-based test configuration
     * properties.
     *
     * @return the accessor for properties
     */
    private static SW360ConnectionConfigurationFactory.Getter<String> stringConfigGetter() {
        Map<String, String> props = new HashMap<>();
        props.put(SW360ConnectionConfiguration.REST_SERVER_URL_KEY, REST_URL);
        props.put(SW360ConnectionConfiguration.AUTH_SERVER_URL_KEY, AUTH_URL);
        props.put(SW360ConnectionConfiguration.USERNAME_KEY, USER);
        props.put(SW360ConnectionConfiguration.PASSWORD_KEY, PASSWORD);
        props.put(SW360ConnectionConfiguration.CLIENT_USER_KEY, CLIENT_ID);
        props.put(SW360ConnectionConfiguration.CLIENT_PASSWORD_KEY, CLIENT_SECRET);

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
            if (!SW360ConnectionConfiguration.PROXY_USE.equals(key)) {
                fail("Unsupported boolean property: " + key);
            }
            return useProxy;
        };
    }

    /**
     * Checks whether the given client object has been correctly initialized.
     *
     * @param client  the client to be checked
     * @param baseUri the expected base URI for this client
     */
    private void checkClient(SW360Client client, String baseUri) {
        Assertions.assertThat(client).isNotNull();
        assertThat(client.getEndpoint()).startsWith(baseUri);
        Object restTemplate = ReflectionTestUtils.getField(client, "restTemplate");
        assertThat(restTemplate).isEqualTo(template);
    }

    /**
     * Checks whether the given SW360 adapter object has been correctly
     * initialized. This is done by checking the wrapped client object.
     *
     * @param adapter     the adapter to be checked
     * @param clientField the name of the field containing the client
     */
    private void checkAdapter(Object adapter, String clientField) {
        SW360Client client = (SW360Client) ReflectionTestUtils.getField(adapter, clientField);
        checkClient(client, SW360ConnectionConfigurationFactoryTest.REST_URL);
    }

    /**
     * Creates a configuration instance with test settings and the given flag
     * for proxy usage.
     *
     * @param useProxy the use proxy flag
     * @return the test configuration
     */
    private SW360ConnectionConfiguration createConfiguration(boolean useProxy) {
        ProxySettings settings = useProxy ? ProxySettings.useProxy(PROXY_HOST, PROXY_PORT) : ProxySettings.noProxy();
        SW360RestTemplateFactory restFactory = mock(SW360RestTemplateFactory.class);
        when(restFactory.createRestTemplate(settings)).thenReturn(template);

        SW360ConnectionConfigurationFactory factory = new SW360ConnectionConfigurationFactory(restFactory);
        return factory.createConfiguration(stringConfigGetter(), booleanConfigGetter(useProxy),
                PROXY_HOST, PROXY_PORT);
    }

    @Test
    public void testDefaultRestTemplateFactoryIsCreated() {
        SW360ConnectionConfigurationFactory factory = new SW360ConnectionConfigurationFactory();

        assertThat(factory.getRestTemplateFactory()).isNotNull();
    }

    @Test
    public void testAuthenticationClientFromConfiguration() {
        SW360ConnectionConfiguration configuration = createConfiguration(false);

        checkClient(configuration.getSW360AuthenticationClient(), AUTH_URL);
    }

    @Test
    public void testProxySettingsAreEvaluated() {
        SW360ConnectionConfiguration configuration = createConfiguration(true);

        checkClient(configuration.getSW360AuthenticationClient(), AUTH_URL);
    }

    @Test
    public void testComponentClientAdapter() {
        SW360ConnectionConfiguration configuration = createConfiguration(false);

        checkAdapter(configuration.getSW360ComponentClientAdapter(), "componentClient");
    }

    @Test
    public void testReleaseClientAdapter() {
        SW360ConnectionConfiguration configuration = createConfiguration(false);

        checkAdapter(configuration.getSW360ReleaseClientAdapter(), "releaseClient");
    }

    @Test
    public void testLicenseClientAdapter() {
        SW360ConnectionConfiguration configuration = createConfiguration(false);

        checkAdapter(configuration.getSW360LicenseClientAdapter(), "licenseClient");
    }

    @Test
    public void testProjectClientAdapter() {
        SW360ConnectionConfiguration configuration = createConfiguration(false);

        checkAdapter(configuration.getSW360ProjectClientAdapter(), "projectClient");
    }

    @Test
    public void testAuthenticationHeaders() {
        SW360AuthenticationClient authenticationClient = mock(SW360AuthenticationClient.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        String token = "<theAccessTokenForSW360>";
        when(authenticationClient.getOAuth2AccessToken(USER, PASSWORD, CLIENT_ID, CLIENT_SECRET))
                .thenReturn(token);
        when(authenticationClient.getHeadersWithBearerToken(token)).thenReturn(headers);
        SW360ConnectionConfiguration configuration = new SW360ConnectionConfiguration(authenticationClient,
                mock(SW360ComponentClientAdapter.class), mock(SW360ReleaseClientAdapter.class),
                mock(SW360LicenseClientAdapter.class), mock(SW360ProjectClientAdapter.class),
                USER, PASSWORD, CLIENT_ID, CLIENT_SECRET);

        assertThat(configuration.getHttpHeaders()).isEqualTo(headers);
    }
}
