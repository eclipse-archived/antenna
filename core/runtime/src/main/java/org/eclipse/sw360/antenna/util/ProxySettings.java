/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

public class ProxySettings {
    private final boolean proxyUse;
    private final String proxyHost;
    private final int proxyPort;

    public ProxySettings(boolean proxyUse, String proxyHost, int proxyPort) {
        this.proxyUse = proxyUse;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public static ProxySettings empty() {
        return new ProxySettings(false, null, -1);
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


}
