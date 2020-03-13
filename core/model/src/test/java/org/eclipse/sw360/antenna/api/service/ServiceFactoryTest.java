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
package org.eclipse.sw360.antenna.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.JsonBean;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.http.HttpClientFactory;
import org.eclipse.sw360.antenna.http.HttpClientFactoryImpl;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceFactoryTest {
    private static final String PROXY_HOST = "my.proxy.com";
    private static final int PROXY_PORT = 4712;

    /**
     * Tests the JSON object mapper. By reading a test JSON file, it is checked
     * whether the mapper has been correctly initialized; especially that
     * unknown properties are ignored.
     */
    @Test
    public void testObjectMapperIsConfigured() throws IOException {
        JsonBean expBean = new JsonBean();
        expBean.setName("test");
        expBean.setAge(42);

        ObjectMapper mapper = ServiceFactory.getObjectMapper();
        assertThat(mapper).isNotNull();
        JsonBean bean = mapper.readValue(getClass().getResource("/testPerson.json"), JsonBean.class);
        assertThat(bean).isEqualTo(expBean);
    }

    @Test
    public void testObjectMapperIsCached() {
        ObjectMapper mapper1 = ServiceFactory.getObjectMapper();

        ObjectMapper mapper2 = ServiceFactory.getObjectMapper();
        assertThat(mapper2).isSameAs(mapper1);
    }

    @Test
    public void testDefaultHttpClientFactory() {
        ServiceFactory factory = new ServiceFactory();

        HttpClient httpClient = factory.createHttpClient(false, null, 0);
        assertThat(httpClient.getClass().getSimpleName()).isEqualTo("HttpClientImpl");
    }

    private static HttpClientConfig createHttpClientConfig(ProxySettings proxySettings) {
        return HttpClientConfig.basicConfig()
                .withProxySettings(proxySettings)
                .withObjectMapper(ServiceFactory.getObjectMapper());
    }

    @Test
    public void testHttpClientIsCorrectlyConfigured() {
        HttpClientFactory clientFactory = mock(HttpClientFactory.class);
        HttpClient client = mock(HttpClient.class);
        ProxySettings proxySettings = ProxySettings.useProxy(PROXY_HOST, PROXY_PORT);
        HttpClientConfig clientConfig = createHttpClientConfig(proxySettings);
        when(clientFactory.newHttpClient(clientConfig)).thenReturn(client);
        ServiceFactory factory = new ServiceFactory(clientFactory);

        HttpClient httpClient = factory.createHttpClient(true, PROXY_HOST, PROXY_PORT);
        assertThat(httpClient).isEqualTo(client);
    }

    @Test
    public void testHttpClientsWithSettingsAreCached() {
        HttpClientFactory clientFactory = mock(HttpClientFactory.class);
        HttpClient client1 = mock(HttpClient.class);
        HttpClient client2 = mock(HttpClient.class);
        HttpClient neverUsedClient = mock(HttpClient.class);
        ProxySettings proxySettings = ProxySettings.useProxy(PROXY_HOST, PROXY_PORT);
        HttpClientConfig clientConfig1 = createHttpClientConfig(proxySettings);
        HttpClientConfig clientConfig2 = createHttpClientConfig(ProxySettings.noProxy());
        when(clientFactory.newHttpClient(clientConfig1)).thenReturn(client1, neverUsedClient);
        when(clientFactory.newHttpClient(clientConfig2)).thenReturn(client2);
        ServiceFactory factory = new ServiceFactory(clientFactory);
        assertThat(factory.createHttpClient(true, PROXY_HOST, PROXY_PORT)).isEqualTo(client1);

        assertThat(factory.createHttpClient(true, PROXY_HOST, PROXY_PORT)).isEqualTo(client1);
        assertThat(factory.createHttpClient(false, PROXY_HOST, PROXY_PORT)).isEqualTo(client2);
    }
}
