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
package org.eclipse.sw360.antenna.sw360.client.api;

import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;

/**
 * <p>
 * The central interface for obtaining client objects to interact with an
 * SW360 server.
 * </p>
 * <p>
 * This interface provides access to a number of adapter objects that can
 * deal with specific SW360 endpoints. Via these adapter objects information
 * can be queried from and updated in SW360.
 * </p>
 */
public interface SW360Connection {
    /**
     * Returns an adapter object for the interaction with the
     * <em>components</em> endpoint of SW360. Using this adapter, information
     * about all known software components can be queried.
     *
     * @return the adapter for components
     */
    SW360ComponentClientAdapter getComponentAdapter();

    /**
     * Returns an adapter object for the interaction with the
     * <em>releases</em> endpoint of SW360. Each software component can have
     * multiple releases. Using this adapter, the releases for the known
     * components can be queried and managed.
     *
     * @return the adapter for releases
     */
    SW360ReleaseClientAdapter getReleaseAdapter();

    /**
     * Returns an adapter object for the interaction with the
     * <em>licenses</em> endpoint of SW360. This adapter allows querying the
     * licenses known to SW360.
     *
     * @return the adapter for licenses
     */
    SW360LicenseClientAdapter getLicenseAdapter();

    /**
     * Returns an adapter object for the interaction with the
     * <em>projects</em> endpoint of SW360. With this adapter projects can be
     * created and assigned to the software components they are using.
     *
     * @return the adapter for projects
     */
    SW360ProjectClientAdapter getProjectAdapter();
}
