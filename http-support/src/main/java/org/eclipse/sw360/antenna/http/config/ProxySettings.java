/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.http.config;

import java.util.Objects;

/**
 * <p>
 * A class defining proxy settings for HTTP clients.
 * </p>
 * <p>
 * This class allows configuring a proxy to be used for HTTP connections. The
 * proxy's host address and port number can be configured. It is also possible
 * to indicate that no proxy should be used.
 * </p>
 */
public final class ProxySettings {
    /**
     * Constant representing an undefined proxy host. If this value is set for
     * the host, this is interpreted as if no proxy is to be used.
     */
    public static final String UNDEFINED_HOST = "";

    /** Constant representing an undefined port. */
    public static final int UNDEFINED_PORT = -1;

    private static final ProxySettings EMPTY_SETTINGS = new ProxySettings(UNDEFINED_HOST, UNDEFINED_PORT);

    private final String proxyHost;
    private final int proxyPort;

    /**
     * Creates a new instance of {@code ProxySettings} with the proxy
     * parameters specified.
     *
     * @param proxyHost the proxy host
     * @param proxyPort the proxy port
     */
    private ProxySettings(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    /**
     * Returns an instance of {@code ProxySettings} that indicates that no
     * proxy is to be used.
     *
     * @return an instance with an empty proxy configuration
     */
    public static ProxySettings noProxy() {
        return EMPTY_SETTINGS;
    }

    /**
     * Creates a new instance of {@code ProxySettings} that uses the specified
     * settings for the proxy.
     *
     * @param host the host address of the proxy
     * @param port the port of the proxy
     * @return the new {@code ProxySettings} instance
     */
    public static ProxySettings useProxy(String host, int port) {
        return new ProxySettings(host, port);
    }

    /**
     * Creates a new instance of {@code ProxySettings} that is initialized from
     * configuration settings. In the configuration, it can be stated
     * explicitly whether a proxy is to be used or not. So it is possible that
     * valid settings for the proxy host and port are provided, but the
     * resulting settings should nevertheless refer to an undefined proxy.
     *
     * @param useProxy flag whether a proxy should be used
     * @param host     the proxy host (may be undefined)
     * @param port     the proxy port (may be undefined)
     * @return the new {@code ProxySettings} instance
     */
    public static ProxySettings fromConfig(boolean useProxy, String host, int port) {
        return useProxy ? useProxy(host, port) : noProxy();
    }

    /**
     * Returns a flag whether a proxy should be used.
     *
     * @return <strong>true</strong> if the proxy server defined by this object
     * should be used; <strong>false</strong> for a direct internet connection
     */
    public boolean isProxyUse() {
        return proxyPort != UNDEFINED_PORT && proxyHost != null && !proxyHost.equals(UNDEFINED_HOST);
    }

    /**
     * Returns the proxy host. This method only returns a defined value if
     * {@link #isProxyUse()} returns <strong>true</strong>.
     *
     * @return the proxy host address
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Returns the proxy port. This method only returns a defined value if
     * {@link #isProxyUse()} returns <strong>true</strong>.
     *
     * @return the proxy port
     */
    public int getProxyPort() {
        return proxyPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProxySettings settings = (ProxySettings) o;
        return  getProxyPort() == settings.getProxyPort() &&
                Objects.equals(getProxyHost(), settings.getProxyHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProxyHost(), getProxyPort());
    }

    @Override
    public String toString() {
        return "ProxySettings{" +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort=" + proxyPort +
                '}';
    }
}
