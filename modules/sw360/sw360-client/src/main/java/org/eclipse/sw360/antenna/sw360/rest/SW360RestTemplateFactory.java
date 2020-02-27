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

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * A helper class that allows the creation of a {@code RestTemplate} that is
 * correctly configured with a request factory supporting the current proxy
 * settings.
 * </p>
 */
public class SW360RestTemplateFactory {
    /**
     * Creates a new {@code RestTemplate} that is initialized with the given
     * settings.
     *
     * @param proxySettings the proxy configuration
     * @return the initialized {@code RestTemplate}
     */
    public RestTemplate createRestTemplate(ProxySettings proxySettings) {
        return new RestTemplate(createRequestFactory(proxySettings));
    }

    /**
     * Creates the request factory for the {@code RestTemplate} managed by this
     * factory. This implementation creates a factory based on Apache HTTP
     * client that is properly configured.
     *
     * @param proxySettings the proxy configuration
     * @return the {@code ClientHttpRequestFactory} to create request objects
     */
    ClientHttpRequestFactory createRequestFactory(ProxySettings proxySettings) {
        return new HttpComponentsClientHttpRequestFactory(createHttpClient(proxySettings));
    }

    /**
     * Creates the HTTP client for the REST template managed by this factory.
     *
     * @param proxySettings the proxy configuration
     * @return the HTTP client
     */
    HttpClient createHttpClient(ProxySettings proxySettings) {
        HttpClientBuilder builder = newClientBuilder();
        if (proxySettings.isProxyUse()) {
            builder.setRoutePlanner(new DefaultProxyRoutePlanner(
                    new HttpHost(proxySettings.getProxyHost(), proxySettings.getProxyPort())));
        }
        return builder.build();
    }

    /**
     * Returns a new builder object for creating HTTP client instances.
     *
     * @return the new HTTP client builder
     */
    HttpClientBuilder newClientBuilder() {
        return HttpClients.custom();
    }
}
