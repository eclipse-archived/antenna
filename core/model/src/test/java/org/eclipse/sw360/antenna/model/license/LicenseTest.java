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
package org.eclipse.sw360.antenna.model.license;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseTest {
    @Test
    public void testEmptyLicense() {
        License license = new License();

        assertThat(license.isEmpty()).isTrue();
        assertThat(license.getThreatGroup()).isNotPresent();
        assertThat(license.getClassification()).isNotPresent();
    }

    @Test
    public void testLicenseWithOnlyId() {
        String licenseId = "Apache-2.0";
        License license = new License(licenseId);

        assertThat(license.isEmpty()).isFalse();
        assertThat(license.getProperties().size()).isEqualTo(0);
        assertThat(license.getId()).isEqualTo(licenseId);
    }

    @Test
    public void testEqualLicense() {
        License license1 = new License ("Apache-2.0");
        License license2 = new License ("Apache-2.0", "Apache License Version 2.0", "Some license text");
        License license3 = new License ("MIT");

        assertThat(license1.equalLicense(license2)).isTrue();
        assertThat(license1.equalLicense(license3)).isFalse();
    }

    @Test
    public void testLicenseWithoutProperties() {
        String licenseId = "Apache-2.0";
        String licenseName = "Apache License 2.0";
        String licenseText = "Too long";
        License license = new License(licenseId, licenseName, licenseText);

        assertThat(license.isEmpty()).isFalse();
        assertThat(license.getId()).isEqualTo(licenseId);
        assertThat(license.getCommonName()).isEqualTo(licenseName);
        assertThat(license.getText()).isEqualTo(licenseText);
        assertThat(license.getProperties().size()).isEqualTo(0);
    }


    @Test
    public void testFullySpecifiedLicense() {
        String licenseId = "Apache-2.0";
        String licenseName = "Apache License 2.0";
        String licenseText = "Too long Apache text.";
        Map<String, String> customProperties = new HashMap<String, String>() {{
            put("customPropertyKey", "customPropertyValue");
        }};
        License license = new License(licenseId, licenseName, licenseText, customProperties);
        license.setThreatGroup("Unknown");
        license.setClassification("Unknown");

        assertThat(license.isEmpty()).isFalse();

        assertThat(license.getId()).isEqualTo(licenseId);
        assertThat(license.getCommonName()).isEqualTo(licenseName);
        assertThat(license.getText()).isEqualTo(licenseText);

        assertThat(license.getProperties().size()).isEqualTo(3);
        assertThat(license.getProperty("customPropertyKey")).hasValue("customPropertyValue");
        assertThat(license.getClassification()).hasValue("Unknown");
        assertThat(license.getThreatGroup()).hasValue("Unknown");

        assertThat(license.evaluate()).isEqualTo(licenseId);
    }

    @Test
    public void testNullAsProperties() {
        License license = new License();

        license.setProperties(null);

        assertThat(license.getProperties()).isNotNull();
    }

    @Test
    public void pushCoverageForEqualsAndHashcode() {
        EqualsVerifier.forClass(License.class)
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
