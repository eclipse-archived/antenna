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
package org.eclipse.sw360.antenna.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProxySettingsTest {
    private static final String PROXY_HOST = "my.proxy.net";
    private static final int PROXY_PORT = 7777;

    /**
     * Helper function to check the implementation of equals() and hashCode().
     *
     * @param obj1     object 1 to be compared
     * @param obj2     object 2 to be compared
     * @param expected the expected outcome
     */
    private static void checkEquals(Object obj1, Object obj2, boolean expected) {
        assertThat(obj1.equals(obj2)).isEqualTo(expected);
        if (obj2 != null) {
            assertThat(obj2.equals(obj1)).isEqualTo(expected);
            if (expected) {
                assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode());
            }
        }
    }

    @Test
    public void testEqualsTrue() {
        ProxySettings settings = new ProxySettings(true, PROXY_HOST, PROXY_PORT);
        checkEquals(settings, settings, true);

        ProxySettings settings2 = new ProxySettings(true, PROXY_HOST, PROXY_PORT);
        checkEquals(settings, settings2, true);
    }

    @Test
    public void testEqualsFalse() {
        ProxySettings settings = new ProxySettings(true, PROXY_HOST, PROXY_PORT);
        ProxySettings settings2 = new ProxySettings(false, PROXY_HOST, PROXY_PORT);
        checkEquals(settings, settings2, false);

        settings2 = new ProxySettings(true, PROXY_HOST + ".other", PROXY_PORT);
        checkEquals(settings, settings2, false);

        settings2 = new ProxySettings(true, PROXY_HOST, PROXY_PORT - 1);
        checkEquals(settings, settings2, false);
    }

    @Test
    public void testEqualsCornerCases() {
        ProxySettings settings = new ProxySettings(false, PROXY_HOST, PROXY_PORT);

        checkEquals(settings, this, false);
        checkEquals(settings, null, false);
    }

    @Test
    public void testToString() {
        ProxySettings settings = new ProxySettings(true, PROXY_HOST, PROXY_PORT);
        String s = settings.toString();

        assertThat(s).contains("proxyHost='" + PROXY_HOST);
        assertThat(s).contains("proxyPort=" + PROXY_PORT);
        assertThat(s).contains("proxyUse=true");
    }

    @Test
    public void testEmpty() {
        ProxySettings emptySettings = ProxySettings.empty();

        assertThat(emptySettings.isProxyUse()).isFalse();
        assertThat(emptySettings.getProxyHost()).isNull();
        assertThat(emptySettings.getProxyPort()).isEqualTo(-1);
    }

    @Test
    public void testEmptyReturnsACachedInstance() {
        ProxySettings empty = ProxySettings.empty();

        assertThat(ProxySettings.empty()).isSameAs(empty);
    }

    @Test
    public void testEqualsWithEmpty() {
        ProxySettings settings = new ProxySettings(false, "", -1);
        checkEquals(ProxySettings.empty(), settings, false);

        settings = new ProxySettings(true, null, -1);
        checkEquals(ProxySettings.empty(), settings, false);

        settings = new ProxySettings(true, "", -1);
        checkEquals(ProxySettings.empty(), settings, false);
    }

    @Test
    public void testIsProxyUseNullHost() {
        ProxySettings settings = new ProxySettings(true, null, -1);

        assertThat(settings.isProxyUse()).isFalse();
    }

    @Test
    public void testIsProxyUseEmptyHost() {
        ProxySettings settings = new ProxySettings(true, "", -1);

        assertThat(settings.isProxyUse()).isFalse();
    }
}
