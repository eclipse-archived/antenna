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
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.sw360.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SW360LicenseClientAdapterTest {
    private static final String LICENSE_NAME = "licenseName";

    private SW360LicenseClientAdapter licenseClientAdapter;

    private SW360LicenseClient licenseClient = mock(SW360LicenseClient.class);
    private HttpHeaders header = mock(HttpHeaders.class);

    @Before
    public void setUp() {
        licenseClientAdapter = new SW360LicenseClientAdapter();
    }

    @Test
    public void testIsLicenseOfArtifactAvailableIsAvailable() {
        SW360SparseLicense sparseLicense = new SW360SparseLicense()
                .setShortName(LICENSE_NAME);
        when(licenseClient.getLicenses(header))
                .thenReturn(Collections.singletonList(sparseLicense));

        licenseClientAdapter.setLicenseClient(licenseClient);

        boolean licenseOfArtifactAvailable = licenseClientAdapter.isLicenseOfArtifactAvailable(LICENSE_NAME, header);

        assertThat(licenseOfArtifactAvailable).isTrue();
        verify(licenseClient, atLeastOnce()).getLicenses(header);
    }

    @Test
    public void testIsLicenseOfArtifactAvailableIsNotAvailable() {
        SW360SparseLicense sparseLicense = new SW360SparseLicense()
                .setShortName(LICENSE_NAME);
        when(licenseClient.getLicenses(header))
                .thenReturn(Collections.singletonList(sparseLicense));

        licenseClientAdapter.setLicenseClient(licenseClient);

        boolean licenseOfArtifactAvailable = licenseClientAdapter.isLicenseOfArtifactAvailable(LICENSE_NAME + "-no", header);

        assertThat(licenseOfArtifactAvailable).isFalse();
        verify(licenseClient, atLeastOnce()).getLicenses(header);
    }

    @Test
    public void testGetSW360LicenseByAntennaLicense() {
        SW360License license = prepareLicenseClientGetLicenseByName();

        Optional<SW360License> sw360LicenseByAntennaLicense = licenseClientAdapter.getSW360LicenseByAntennaLicense(LICENSE_NAME, header);

        assertLicenseByNameResult(license, sw360LicenseByAntennaLicense);
    }

    @Test
    public void testGetLicenseDetails() {
        SW360License license = prepareLicenseClientGetLicenseByName();
        SW360SparseLicense sparseLicense = new SW360SparseLicense()
                .setShortName(LICENSE_NAME);

        Optional<SW360License> licenseDetails = licenseClientAdapter.getLicenseDetails(sparseLicense, header);

        assertLicenseByNameResult(license, licenseDetails);
    }

    private SW360License prepareLicenseClientGetLicenseByName() {
        SW360License license = new SW360License()
                .setShortName(LICENSE_NAME);
        when(licenseClient.getLicenseByName(LICENSE_NAME, header))
                .thenReturn(Optional.of(license));

        licenseClientAdapter.setLicenseClient(licenseClient);
        return license;
    }

    private void assertLicenseByNameResult(SW360License license, Optional<SW360License> sw360LicenseByAntennaLicense) {
        assertThat(sw360LicenseByAntennaLicense).isPresent();
        assertThat(sw360LicenseByAntennaLicense).hasValue(license);
        verify(licenseClient, atLeastOnce()).getLicenseByName(LICENSE_NAME, header);
    }

}