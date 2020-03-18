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
package org.eclipse.sw360.antenna.http;

import okhttp3.OkHttpClient;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.config.ProxySettings;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * <p>
 * An implementation of the {@link HttpClientFactory} interface that creates
 * client instances using underlying OkHttpClient objects.
 * </p>
 */
public class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public HttpClient newHttpClient(HttpClientConfig config) {
        return new HttpClientImpl(createClient(config), config.getOrCreateObjectMapper());
    }

    /**
     * Creates a new {@code OkHttpClient} object that is correctly configured.
     *
     * @param config the client configuration
     * @return the new client object
     */
    private static OkHttpClient createClient(HttpClientConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (config.proxySettings().isProxyUse()) {
            Proxy proxy = createProxy(config.proxySettings());
            builder.proxy(proxy);
        }
        if (unverifiedSSLCertificate()) {
            builder.hostnameVerifier((s, sslSession) -> true);
        }
        return builder.build();
    }

    /**
     * Using the Property "sw360.client.access.unverified", the connection to
     * SW360 can be done without verification of the ssl certificate
     *
     * @return True, if the client access to SW360 should be done with a self-certified call
     */
    private static boolean unverifiedSSLCertificate() {
        return "true".equalsIgnoreCase(System.getProperty("sw360.client.access.unverified", "false"));
    }

    /**
     * Creates a {@code Proxy} object that corresponds to the passed in proxy
     * settings.
     *
     * @param settings the {@code ProxySettings}
     * @return the corresponding {@code Proxy} representation
     */
    private static Proxy createProxy(ProxySettings settings) {
        return new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(settings.getProxyHost(), settings.getProxyPort()));
    }
}
