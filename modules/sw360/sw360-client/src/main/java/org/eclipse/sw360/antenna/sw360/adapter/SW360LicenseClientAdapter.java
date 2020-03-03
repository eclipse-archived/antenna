/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;

import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.block;
import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.optionalFuture;

public class SW360LicenseClientAdapter {
    private final SW360LicenseClient licenseClient;

    public SW360LicenseClientAdapter(SW360LicenseClient client) {
        licenseClient = client;
    }

    public SW360LicenseClient getLicenseClient() {
        return licenseClient;
    }

    public boolean isLicenseOfArtifactAvailable(String license) {
        List<SW360SparseLicense> sw360Licenses = block(getLicenseClient().getLicenses());

        return sw360Licenses.stream()
                .map(SW360SparseLicense::getShortName)
                .anyMatch(n -> n.equals(license));
    }

    public Optional<SW360License> getSW360LicenseByAntennaLicense(String license) {
        return block(optionalFuture(getLicenseClient().getLicenseByName(license)));
    }

    public Optional<SW360License> getLicenseDetails(SW360SparseLicense sparseLicense) {
        return block(optionalFuture(getLicenseClient().getLicenseByName(sparseLicense.getShortName())));
    }
}
