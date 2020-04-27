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

import org.eclipse.sw360.antenna.sw360.client.rest.SW360LicenseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.block;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360LicenseClientAdapterAsyncImplTest {
    private static final String LICENSE_NAME = "licenseName";

    private SW360LicenseClientAdapterAsync licenseClientAdapter;

    private SW360LicenseClient licenseClient;

    @Before
    public void setUp() {
        licenseClient = mock(SW360LicenseClient.class);
        licenseClientAdapter = new SW360LicenseClientAdapterAsyncImpl(licenseClient);
    }

    @Test
    public void testIsLicenseOfArtifactAvailableIsAvailable() {
        SW360SparseLicense sparseLicense = new SW360SparseLicense()
                .setShortName(LICENSE_NAME);
        when(licenseClient.getLicenses())
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(sparseLicense)));

        boolean licenseOfArtifactAvailable = block(licenseClientAdapter.isLicenseOfArtifactAvailable(LICENSE_NAME));

        assertThat(licenseOfArtifactAvailable).isTrue();
        verify(licenseClient, atLeastOnce()).getLicenses();
    }

    @Test
    public void testIsLicenseOfArtifactAvailableIsNotAvailable() {
        SW360SparseLicense sparseLicense = new SW360SparseLicense()
                .setShortName(LICENSE_NAME);
        when(licenseClient.getLicenses())
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(sparseLicense)));

        boolean licenseOfArtifactAvailable =
                block(licenseClientAdapter.isLicenseOfArtifactAvailable(LICENSE_NAME + "-no"));

        assertThat(licenseOfArtifactAvailable).isFalse();
        verify(licenseClient, atLeastOnce()).getLicenses();
    }

    @Test
    public void testGetSW360LicenseByAntennaLicense() {
        SW360License license = prepareLicenseClientGetLicenseByName();

        Optional<SW360License> sw360LicenseByAntennaLicense =
                block(licenseClientAdapter.getSW360LicenseByAntennaLicense(LICENSE_NAME));

        assertLicenseByNameResult(license, sw360LicenseByAntennaLicense);
    }

    @Test
    public void testGetLicenseDetails() {
        SW360License license = prepareLicenseClientGetLicenseByName();
        SW360SparseLicense sparseLicense = new SW360SparseLicense()
                .setShortName(LICENSE_NAME);

        Optional<SW360License> licenseDetails = block(licenseClientAdapter.getLicenseDetails(sparseLicense));

        assertLicenseByNameResult(license, licenseDetails);
    }

    private SW360License prepareLicenseClientGetLicenseByName() {
        SW360License license = new SW360License()
                .setShortName(LICENSE_NAME);
        when(licenseClient.getLicenseByName(LICENSE_NAME))
                .thenReturn(CompletableFuture.completedFuture(license));

        return license;
    }

    private void assertLicenseByNameResult(SW360License license, Optional<SW360License> sw360LicenseByAntennaLicense) {
        assertThat(sw360LicenseByAntennaLicense).isPresent();
        assertThat(sw360LicenseByAntennaLicense).hasValue(license);
        verify(licenseClient, atLeastOnce()).getLicenseByName(LICENSE_NAME);
    }

}