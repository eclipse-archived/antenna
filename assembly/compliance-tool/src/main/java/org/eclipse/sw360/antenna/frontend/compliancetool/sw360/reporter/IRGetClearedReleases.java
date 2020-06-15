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

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class IRGetClearedReleases extends IRForReleases {
    private static final String GET_RELEASES_CLEARED = "releases-cleared";

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_CLEARED;
    }

    @Override
    public Collection<SW360Release> execute(SW360Connection connection) {
        final List<SW360SparseComponent> components = connection.getComponentAdapter().getComponents();

        final Predicate<SW360Release> isApproved = ComplianceFeatureUtils::isApproved;
        return getReleasesFromComponentsByPredicate(connection, components, isApproved);
    }
}
