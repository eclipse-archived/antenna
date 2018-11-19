/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.sw360.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;

public class SW360LicenseClientAdapter {
    private final SW360LicenseClient licenseClient;

    public SW360LicenseClientAdapter(String restUrl) { this.licenseClient = new SW360LicenseClient(restUrl); }

    public boolean isLicenseOfArtifactAvailable(License license, HttpHeaders header) throws IOException, AntennaException {
        List<SW360License> sw360Licenses = licenseClient.getLicenses(header);

        return sw360Licenses.stream()
                .map(l -> SW360HalResourceUtility.getLastIndexOfLinkObject(l.get_Links()).orElse(""))
                .anyMatch(n -> n.equals(license.getName()));
    }

    public SW360License addLicense(License license, HttpHeaders header) throws IOException, AntennaException {
        SW360License sw360License = new SW360License(license);
        return licenseClient.createLicense(sw360License, header);
    }

    public SW360License getSW360LicenseByAntennaLicense(License license, HttpHeaders header) throws IOException, AntennaException {
        return licenseClient.getLicense(license.getName(), header);
    }
}
