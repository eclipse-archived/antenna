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

import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.auth.AccessTokenProvider;
import org.eclipse.sw360.antenna.sw360.client.auth.SW360AuthenticationClient;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360ProjectClient;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360ReleaseClient;

/**
 * <p>
 * A factory class for creating a new {@link SW360Connection}.
 * </p>
 * <p>
 * This class is the central entry point into the SW360 client library. It can
 * create an {@link SW360Connection} object from an {@link SW360ClientConfig}
 * instance. From this connection instance the adapter objects can be obtained
 * that allow the actual interaction with an SW360 server.
 * </p>
 */
public class SW360ConnectionFactory {
    /**
     * Creates a new {@code SW360Connection} instance based on the passed in
     * configuration object. The configuration defines the SW360 server to be
     * accessed, together with some central helper objects that are used for
     * these interactions.
     *
     * @param config the configuration of this SW360 client
     * @return a new {@code SW360Connection} object
     */
    public SW360Connection newConnection(SW360ClientConfig config) {
        SW360AuthenticationClient authClient = new SW360AuthenticationClient(config);
        AccessTokenProvider tokenProvider = new AccessTokenProvider(authClient);

        SW360ComponentClient componentClient = new SW360ComponentClient(config, tokenProvider);
        org.eclipse.sw360.antenna.sw360.client.adapter.SW360ComponentClientAdapter componentAdapter = new SW360ComponentClientAdapter(componentClient);
        SW360ReleaseClient releaseClient = new SW360ReleaseClient(config, tokenProvider);
        SW360ReleaseClientAdapter releaseAdapter = new SW360ReleaseClientAdapter(releaseClient, componentAdapter);

        SW360LicenseClient licenseClient = new SW360LicenseClient(config, tokenProvider);
        SW360LicenseClientAdapterAsync licenseAdapterAsync = new SW360LicenseClientAdapterAsyncImpl(licenseClient);
        SW360LicenseClientAdapter licenseAdapterSync =
                SyncClientAdapterHandler.newHandler(SW360LicenseClientAdapter.class,
                        SW360LicenseClientAdapterAsync.class, licenseAdapterAsync);

        SW360ProjectClient projectClient = new SW360ProjectClient(config, tokenProvider);
        org.eclipse.sw360.antenna.sw360.client.adapter.SW360ProjectClientAdapter projectAdapter = new SW360ProjectClientAdapter(projectClient);

        return new SW360Connection() {
            @Override
            public org.eclipse.sw360.antenna.sw360.client.adapter.SW360ComponentClientAdapter getComponentAdapter() {
                return componentAdapter;
            }

            @Override
            public SW360ReleaseClientAdapter getReleaseAdapter() {
                return releaseAdapter;
            }

            @Override
            public SW360LicenseClientAdapter getLicenseAdapter() {
                return licenseAdapterSync;
            }

            @Override
            public SW360LicenseClientAdapterAsync getLicenseAdapterAsync() {
                return licenseAdapterAsync;
            }

            @Override
            public org.eclipse.sw360.antenna.sw360.client.adapter.SW360ProjectClientAdapter getProjectAdapter() {
                return projectAdapter;
            }
        };
    }
}
