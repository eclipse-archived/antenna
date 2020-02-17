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
package org.eclipse.sw360.antenna.sw360.rest;

import org.apache.http.HttpStatus;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class SW360LicenseClientIT extends AbstractMockServerTest {
    /**
     * An array with the names of the licenses in the test data.
     */
    private static final String[] TEST_LICENSES = {
            "BSD Zero Clause License", "Attribution Assurance License", "Amazon Digital Services License",
            "Academic Free License v1.1", "XPP License"
    };

    private SW360LicenseClient licenseClient;

    @Before
    public void setUp() {
        licenseClient = new SW360LicenseClient(wireMockRule.baseUrl(), createRestTemplate());
    }

    /**
     * Tests the passed in list of license data against the expected test data.
     *
     * @param licenses the collection with license data to be checked
     */
    private static void checkLicenses(List<? extends SW360SparseLicense> licenses) {
        List<String> actualLicenses = licenses.stream()
                .map(SW360SparseLicense::getFullName)
                .collect(Collectors.toList());
        assertThat(actualLicenses).containsExactlyInAnyOrder(TEST_LICENSES);
        assertHasLinks(licenses);
    }

    @Test
    public void testGetLicenses() {
        wireMockRule.stubFor(get(urlPathEqualTo("/licenses"))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("all_licenses.json")));

        List<SW360SparseLicense> licenses = licenseClient.getLicenses(new HttpHeaders());
        checkLicenses(licenses);
    }

    @Test
    public void testGetLicenseByName() {
        final String licenseName = "tst";
        wireMockRule.stubFor(get(urlPathEqualTo("/licenses/" + licenseName))
                .willReturn(aJsonResponse(HttpStatus.SC_OK)
                        .withBodyFile("license.json")));

        SW360License license = assertPresent(licenseClient.getLicenseByName(licenseName, new HttpHeaders()));
        assertThat(license.getFullName()).isEqualTo("Test License");
        assertThat(license.getText()).contains("Bosch.IO GmbH");
        assertThat(license.getShortName()).isEqualTo("0TST");
    }

    @Test(expected = HttpClientErrorException.class)
    public void testGetLicenseByNameUnknown() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));

        licenseClient.getLicenseByName("unknown", new HttpHeaders());
    }

    @Test
    public void testGetLicenseByNameNoContent() {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT)));

        Optional<SW360License> optLicense = licenseClient.getLicenseByName("foo", new HttpHeaders());
        assertThat(optLicense).isNotPresent();
    }
}
