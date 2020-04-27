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
package org.eclipse.sw360.antenna.sw360.client.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.auth.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360Client;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SW360ConnectionFactoryTest {
    /**
     * The test configuration passed to the factory instance.
     */
    private static final SW360ClientConfig CONFIG = SW360ClientConfig.createConfig("restURL",
            "authURL", "user", "password", "clientId", "clientPassword",
            mock(HttpClient.class), mock(ObjectMapper.class));

    /**
     * The factory object to be tested.
     */
    private SW360ConnectionFactory connectionFactory;

    @Before
    public void setUp() {
        connectionFactory = new SW360ConnectionFactory();
    }

    /**
     * Invokes the test factory with the test configuration to create a new
     * (test) SW360 connection.
     *
     * @return the new connection created by the factory
     */
    private SW360Connection newConnection() {
        return connectionFactory.newConnection(CONFIG);
    }

    /**
     * Checks whether the properties of a client object have been correctly
     * initialized.
     *
     * @param client the client to be checked
     */
    private static void checkClient(SW360Client client) {
        assertThat(client.getClientConfig()).isEqualTo(CONFIG);
        SW360AuthenticationClient authClient = client.getTokenProvider().getAuthClient();
        assertThat(authClient.getClientConfig()).isEqualTo(CONFIG);
    }

    /**
     * Checks whether a correct synchronous adapter for an entity type is
     * returned.
     *
     * @param syncAdapter  the synchronous adapter
     * @param asyncAdapter the corresponding asynchronous adapter
     */
    private static void checkSyncAdapter(Object syncAdapter, Object asyncAdapter) {
        assertThat(Proxy.isProxyClass(syncAdapter.getClass())).isTrue();
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(syncAdapter);
        assertThat(invocationHandler).isInstanceOf(SyncClientAdapterHandler.class);

        // check if the correct delegate is used
        assertThat(syncAdapter.hashCode()).isEqualTo(asyncAdapter.hashCode());
    }

    @Test
    public void testComponentAdapter() {
        SW360ComponentClientAdapter componentAdapter = newConnection().getComponentAdapter();

        checkClient(componentAdapter.getComponentClient());
    }

    @Test
    public void testReleaseAdapter() {
        SW360Connection connection = newConnection();
        SW360ReleaseClientAdapter releaseAdapter = (SW360ReleaseClientAdapter) connection.getReleaseAdapter();

        checkClient(releaseAdapter.getReleaseClient());
        assertThat(releaseAdapter.getComponentAdapter()).isEqualTo(connection.getComponentAdapter());
    }

    @Test
    public void testLicenseAdapter() {
        SW360Connection sw360Connection = newConnection();
        SW360LicenseClientAdapterAsync licenseAdapterAsync = sw360Connection.getLicenseAdapterAsync();
        SW360LicenseClientAdapter licenseAdapterSync = sw360Connection.getLicenseAdapter();

        checkClient(licenseAdapterAsync.getLicenseClient());
        checkClient(licenseAdapterSync.getLicenseClient());
        checkSyncAdapter(licenseAdapterSync, licenseAdapterAsync);
    }

    @Test
    public void testProjectAdapter() {
        SW360ProjectClientAdapter projectAdapter = newConnection().getProjectAdapter();

        checkClient(projectAdapter.getProjectClient());
    }
}