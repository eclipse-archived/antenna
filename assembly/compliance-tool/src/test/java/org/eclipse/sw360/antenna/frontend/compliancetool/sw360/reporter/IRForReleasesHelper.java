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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.ComponentSearchParams;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IRForReleasesHelper {
    /**
     * create a connection with all mock objects that get
     * called when executing getting the releases.
     *
     * @return mocked sw360 connection
     */
    static SW360Connection getSW360Connection(SW360Release release) {
        SW360Connection connection = mock(SW360Connection.class);

        final String name = "test";
        SW360Component component = SW360TestUtils.mkSW360Component(name);
        Self self = new Self("1234");
        LinkObjects linkObjectsWithSelf = new LinkObjects()
                .setSelf(self);
        component.setLinks(linkObjectsWithSelf);
        SW360ComponentEmbedded componentEmbedded = new SW360ComponentEmbedded();
        SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease(name);
        componentEmbedded.setReleases(Collections.singletonList(sparseRelease));
        component.setEmbedded(componentEmbedded);

        SW360ComponentClientAdapter componentClientAdapter = mock(SW360ComponentClientAdapter.class);
        when(componentClientAdapter.getComponentById(any()))
                .thenReturn(Optional.of((component)));
        SW360SparseComponent sparseComponent = SW360TestUtils.mkSW360SparseComponent(name);
        when(componentClientAdapter.search(ComponentSearchParams.ALL_COMPONENTS))
                .thenReturn(Collections.singletonList(sparseComponent));
        when(connection.getComponentAdapter())
                .thenReturn(componentClientAdapter);

        SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);
        when(releaseClientAdapter.getReleaseById(any()))
                .thenReturn(Optional.of(release));
        when(connection.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);

        return connection;
    }
}
