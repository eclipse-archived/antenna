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
package org.eclipse.sw360.antenna.sw360.rest;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SW360RestTemplateFactoryTest {
    @Test
    public void testClientBuilderIsCreated() {
        SW360RestTemplateFactory factory = new SW360RestTemplateFactory();

        assertThat(factory.newClientBuilder()).isNotNull();
    }

    /**
     * Creates a test factory instance that is prepared to use the passed in
     * builder for HTTP clients. This is used to test whether the HTTP client
     * is correctly configured.
     *
     * @param builder the HTTP client builder
     * @return the test factory
     */
    private static SW360RestTemplateFactory factoryWithClientBuilder(HttpClientBuilder builder) {
        return new SW360RestTemplateFactory() {
            @Override
            HttpClientBuilder newClientBuilder() {
                return builder;
            }
        };
    }

    @Test
    public void testCreateHttpClientIfNoProxyIsUsed() {
        ProxySettings settings = ProxySettings.noProxy();
        HttpClientBuilder builder = mock(HttpClientBuilder.class);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(builder.build()).thenReturn(httpClient);
        SW360RestTemplateFactory factory = factoryWithClientBuilder(builder);

        assertThat(factory.createHttpClient(settings)).isEqualTo(httpClient);
        verify(builder).build();
        verifyNoMoreInteractions(builder);
    }

    @Test
    public void testCreateHttpClientWithProxyConfiguration() throws HttpException {
        ProxySettings settings = ProxySettings.useProxy("my.company.proxy", 3128);
        HttpClientBuilder builder = mock(HttpClientBuilder.class);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(builder.build()).thenReturn(httpClient);
        when(builder.setRoutePlanner(any())).thenReturn(builder);
        SW360RestTemplateFactory factory = factoryWithClientBuilder(builder);

        assertThat(factory.createHttpClient(settings)).isEqualTo(httpClient);
        ArgumentCaptor<HttpRoutePlanner> captor = ArgumentCaptor.forClass(HttpRoutePlanner.class);
        verify(builder).setRoutePlanner(captor.capture());

        HttpHost target = new HttpHost("www.eclipse.org", 443, "https");
        HttpGet request = new HttpGet("/sw360antenna");
        HttpRoute route = captor.getValue().determineRoute(target, request, new BasicHttpContext());
        assertThat(route.getProxyHost().getHostName()).isEqualTo(settings.getProxyHost());
        assertThat(route.getProxyHost().getPort()).isEqualTo(settings.getProxyPort());
    }

    @Test
    public void testCreateRequestFactory() {
        ProxySettings settings = ProxySettings.useProxy("proxy.net", 8080);
        CloseableHttpClient client = mock(CloseableHttpClient.class);
        SW360RestTemplateFactory factory = new SW360RestTemplateFactory() {
            @Override
            HttpClient createHttpClient(ProxySettings proxySettings) {
                assertThat(proxySettings).isEqualTo(settings);
                return client;
            }
        };

        ClientHttpRequestFactory requestFactoryIfc = factory.createRequestFactory(settings);
        assertThat(requestFactoryIfc).isInstanceOf(HttpComponentsClientHttpRequestFactory.class);
        HttpComponentsClientHttpRequestFactory requestFactory =
                (HttpComponentsClientHttpRequestFactory) requestFactoryIfc;
        assertThat(requestFactory.getHttpClient()).isEqualTo(client);
    }

    @Test
    public void testCreateRestTemplate() {
        ProxySettings settings = ProxySettings.useProxy("foo.bar", 1234);
        ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);
        SW360RestTemplateFactory factory = new SW360RestTemplateFactory() {
            @Override
            ClientHttpRequestFactory createRequestFactory(ProxySettings proxySettings) {
                assertThat(proxySettings).isEqualTo(settings);
                return requestFactory;
            }
        };

        RestTemplate restTemplate = factory.createRestTemplate(settings);
        assertThat(restTemplate.getRequestFactory()).isEqualTo(requestFactory);
    }
}
