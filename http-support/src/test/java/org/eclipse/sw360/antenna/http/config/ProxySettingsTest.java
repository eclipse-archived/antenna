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
package org.eclipse.sw360.antenna.http.config;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProxySettingsTest {
    private static final String PROXY_HOST = "my.proxy.net";
    private static final int PROXY_PORT = 7777;

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(ProxySettings.class)
                .verify();
    }

    @Test
    public void testToString() {
        ProxySettings settings = ProxySettings.useProxy(PROXY_HOST, PROXY_PORT);
        String s = settings.toString();

        assertThat(s).contains("proxyHost='" + PROXY_HOST);
        assertThat(s).contains("proxyPort=" + PROXY_PORT);
    }

    @Test
    public void testNoProxy() {
        ProxySettings emptySettings = ProxySettings.noProxy();

        assertThat(emptySettings.isProxyUse()).isFalse();
        assertThat(emptySettings.getProxyHost()).isEqualTo(ProxySettings.UNDEFINED_HOST);
        assertThat(emptySettings.getProxyPort()).isEqualTo(ProxySettings.UNDEFINED_PORT);
    }

    @Test
    public void testNoProxyReturnsACachedInstance() {
        ProxySettings empty = ProxySettings.noProxy();

        assertThat(ProxySettings.noProxy()).isSameAs(empty);
    }

    @Test
    public void testIsProxyUseNullHost() {
        ProxySettings settings = ProxySettings.useProxy(null, -1);

        assertThat(settings.isProxyUse()).isFalse();
    }

    @Test
    public void testIsUseProxyUndefinedPort() {
        ProxySettings settings = ProxySettings.useProxy(PROXY_HOST, ProxySettings.UNDEFINED_PORT);

        assertThat(settings.isProxyUse()).isFalse();
    }

    @Test
    public void testCreateForProxy() {
        ProxySettings settings = ProxySettings.useProxy(PROXY_HOST, PROXY_PORT);

        assertThat(settings.getProxyHost()).isEqualTo(PROXY_HOST);
        assertThat(settings.getProxyPort()).isEqualTo(PROXY_PORT);
        assertThat(settings.isProxyUse()).isTrue();
    }

    @Test
    public void testFromConfigUseProxy() {
        ProxySettings settings = ProxySettings.fromConfig(true, PROXY_HOST, PROXY_PORT);

        assertThat(settings.getProxyHost()).isEqualTo(PROXY_HOST);
        assertThat(settings.getProxyPort()).isEqualTo(PROXY_PORT);
        assertThat(settings.isProxyUse()).isTrue();
    }

    @Test
    public void testFromConfigNoProxyUse() {
        ProxySettings settings = ProxySettings.fromConfig(false, PROXY_HOST, PROXY_PORT);

        assertThat(settings).isEqualTo(ProxySettings.noProxy());
    }
}
