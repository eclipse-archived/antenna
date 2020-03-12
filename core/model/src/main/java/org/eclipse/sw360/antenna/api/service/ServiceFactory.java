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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.http.HttpClientFactory;
import org.eclipse.sw360.antenna.http.HttpClientFactoryImpl;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.config.ProxySettings;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * A class for obtaining central shared service objects that can be used during
 * an Antenna execution.
 * </p>
 * <p>
 * There are a couple of service objects that are expensive to create, but once
 * created can be shared by all Antenna components. This class manages such
 * objects. It provides methods for querying the single service objects
 * supported. The objects are typically created lazily on first access.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 */
public class ServiceFactory {
    /**
     * The factory for creating HTTP client instances.
     */
    private final HttpClientFactory httpClientFactory;

    /**
     * A cache for the HTTP client objects that have been created. For each
     * different proxy settings a separate client is created.
     */
    private final ConcurrentMap<ProxySettings, HttpClient> httpClients;

    /**
     * Creates a new instance of {@code ServiceFactory} with default settings.
     */
    public ServiceFactory() {
        this(new HttpClientFactoryImpl());
    }

    /**
     * Creates a new instance of {@code ServiceFactory} that uses the provided
     * factory to create new HTTP clients.
     *
     * @param httpClientFactory the HTTP client factory
     */
    ServiceFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
        httpClients = new ConcurrentHashMap<>();
    }

    /**
     * Returns an initialized JSON object mapper. This mapper can (and should)
     * be used by all components having to deal with JSON serialization.
     *
     * @return the JSON object mapper
     */
    public static ObjectMapper getObjectMapper() {
        return LazyMapperHolder.MAPPER;
    }

    /**
     * Returns an initialized HTTP client that supports the given proxy
     * configuration. This method returns the same client instance when asked
     * for the same proxy configuration.
     *
     * @param useProxy  flag whether a proxy should be used
     * @param proxyHost the proxy host
     * @param proxyPort the proxy port
     * @return the {@code HttpClient} supporting this proxy configuration
     */
    public HttpClient createHttpClient(boolean useProxy, String proxyHost, int proxyPort) {
        ProxySettings proxySettings = ProxySettings.fromConfig(useProxy, proxyHost, proxyPort);
        return httpClients.computeIfAbsent(proxySettings, this::createHttpClient);
    }

    /**
     * Creates a new HTTP client that is configured with the proxy settings
     * passed in.
     *
     * @param settings the proxy settings
     * @return the new {@code HttpClient}
     */
    private HttpClient createHttpClient(ProxySettings settings) {
        HttpClientConfig clientConfig = createHttpClientConfig(settings);
        return httpClientFactory.newHttpClient(clientConfig);
    }

    /**
     * Creates a configuration for a new HTTP client based on the given proxy
     * settings. As the JSON mapper needed by the client the one managed by
     * this factory is used.
     *
     * @param settings the proxy settings
     * @return the {@code HttpClientConfig} for these proxy settings
     */
    private static HttpClientConfig createHttpClientConfig(ProxySettings settings) {
        return HttpClientConfig.basicConfig()
                .withObjectMapper(getObjectMapper())
                .withProxySettings(settings);
    }

    /**
     * Application of the Initialization-on-demand holder idiom for the JSON
     * object mapper.
     */
    private static class LazyMapperHolder {
        private static final ObjectMapper MAPPER = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
