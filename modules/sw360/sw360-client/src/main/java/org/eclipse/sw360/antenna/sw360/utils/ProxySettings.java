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
package org.eclipse.sw360.antenna.sw360.utils;

import java.util.Objects;

public class ProxySettings {
    private static final ProxySettings EMPTY_SETTINGS = new ProxySettings(false, null, -1);

    private final boolean proxyUse;
    private final String proxyHost;
    private final int proxyPort;

    public ProxySettings(boolean proxyUse, String proxyHost, int proxyPort) {
        this.proxyUse = proxyUse;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    /**
     * Returns an instance of {@code ProxySettings} that indicates that no
     * proxy is to be used.
     *
     * @return an instance with an empty proxy configuration
     */
    public static ProxySettings empty() {
        return EMPTY_SETTINGS;
    }

    public boolean isProxyUse() {
        return proxyUse && proxyHost != null && ! proxyHost.isEmpty();
    }

    public String getProxyHost() {
        return proxyHost;
    }

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
        return proxyUse == settings.proxyUse &&
                getProxyPort() == settings.getProxyPort() &&
                Objects.equals(getProxyHost(), settings.getProxyHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxyUse, getProxyHost(), getProxyPort());
    }

    @Override
    public String toString() {
        return "ProxySettings{" +
                "proxyUse=" + proxyUse +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort=" + proxyPort +
                '}';
    }
}
