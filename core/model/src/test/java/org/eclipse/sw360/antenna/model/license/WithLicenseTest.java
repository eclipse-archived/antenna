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

import static org.assertj.core.api.Assertions.assertThat;

public class WithLicenseTest {
    @Test
    public void testEmptyWithLicense() {
        WithLicense withLicense = new WithLicense();

        assertThat(withLicense.isEmpty()).isTrue();
    }

    @Test
    public void testSpecifiedWithLicense() {
        String licenseId = "GPL-2.0-or-later";
        String licenseName = "Gnu Public License 2.0 or later";
        String licenseText = "Text of GPL";
        String exceptionId = "Classpath-Exception";
        String exceptionName = "Classpath exception";
        String exceptionText = "Text of Exception";
        WithLicense withLicense = new WithLicense(licenseId, licenseName, licenseText, exceptionId,exceptionName, exceptionText);
        String threatGroup = "TestGroup";
        String classification = "TestClassification";
        withLicense.setThreatGroup(threatGroup);
        withLicense.setClassification(classification);

        assertThat(withLicense.isEmpty()).isFalse();
        assertThat(withLicense.getLicenses().size())
                .isEqualTo(1);

        assertThat(withLicense.getId()).isEqualTo(licenseId + " WITH " + exceptionId);
        assertThat(withLicense.getLicenseId()).isEqualTo(licenseId);
        assertThat(withLicense.getExceptionId()).isEqualTo(exceptionId);

        assertThat(withLicense.toString()).isEqualTo(licenseId + " WITH " + exceptionId);
        assertThat(withLicense.evaluate()).isEqualTo(licenseId + " WITH " + exceptionId);

        assertThat(withLicense.getCommonName()).isEqualTo(licenseName + " WITH " + exceptionName);
        assertThat(withLicense.getText()).contains(licenseText);
        assertThat(withLicense.getText()).contains(exceptionText);

        assertThat(withLicense.getThreatGroup().orElseThrow(NullPointerException::new)).isEqualTo(threatGroup);
        assertThat(withLicense.getClassification().orElseThrow(NullPointerException::new)).isEqualTo(classification);
    }

    @Test
    public void testEqualLicense() {
        WithLicense license1 = new WithLicense("GPL-2.0-or-later", "Classpath-Exception");
        WithLicense license2 = new WithLicense("GPL-2.0-or-later", "Gnu Public License 2.0 or later", "GPL Text", "Classpath-Exception", "Classpath exception", "Text of Exception");
        WithLicense license3 = new WithLicense("GPL-2.0-or-later", "Autoconf-Exception");


        assertThat(license1.equalLicense(license2)).isTrue();
        assertThat(license1.equalLicense(license3)).isFalse();
    }

    @Test
    public void testWithLicenseBuildFromAssembledInput() {
        String licenseId = "GPL-2.0-or-later WITH Classpath-Exception";
        String licenseName = "Gnu Public License or later WITH Classpath Exception";
        String licenseText = "Gnu license text\n\nwith exception:\n\nException text";

        WithLicense withLicense1 = new WithLicense();
        assertThat(withLicense1.isEmpty()).isTrue();

        withLicense1.setId(licenseId);
        withLicense1.setCommonName(licenseName);
        withLicense1.setText(licenseText);
        assertThat(withLicense1.isEmpty()).isFalse();

        WithLicense withLicense2 = new WithLicense(licenseId, licenseName, licenseText);

        assertThat(withLicense1).isEqualTo(withLicense2);
        assertThat(withLicense1.equalLicense(withLicense2)).isTrue();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorWithWrongIdParameter() {
        new WithLicense("something", "strange WITH happening", "with with exception: text");
    }

    @Test
    public void testPartialWithLicense() {
        WithLicense license = new WithLicense();

        assertThat(license.getId()).isEqualTo("");
        assertThat(license.getCommonName()).isEqualTo("");
        assertThat(license.getText()).isEqualTo("");

        license.setLicenseId("id1");
        license.setLicenseName("name1");
        license.setLicenseText("text1");

        assertThat(license.getId()).isEqualTo("");
        assertThat(license.getCommonName()).isEqualTo("");
        assertThat(license.getText()).isEqualTo("");

        license.setExceptionId("id2");
        assertThat(license.getId()).isEqualTo("id1 WITH id2");
        assertThat(license.getCommonName()).isEqualTo("");
        assertThat(license.getText()).isEqualTo("");
    }

    @Test
    public void testWithLicenseFromScratch() {
        WithLicense withLicense = new WithLicense();

        assertThat(withLicense.isEmpty()).isTrue();

        String licenseId = "Apache-2.0";
        String licenseName = "Apache License Version 2.0";
        String licenseText = "Some Text";
        String exceptionId = "Exception";
        String exceptionName = "Test Exception";
        String exceptionText = "Exception text";
        withLicense.setLicenseId(licenseId);
        withLicense.setLicenseName(licenseName);
        withLicense.setLicenseText(licenseText);
        withLicense.setExceptionId(exceptionId);
        withLicense.setExceptionName(exceptionName);
        withLicense.setExceptionText(exceptionText);

        assertThat(withLicense.isEmpty()).isFalse();
        assertThat(withLicense.toString()).isEqualTo(licenseId + " WITH " + exceptionId);
        assertThat(withLicense.getLicenseId()).isEqualTo(licenseId);
        assertThat(withLicense.getExceptionId()).isEqualTo(exceptionId);
        assertThat(withLicense.getId()).isEqualTo(licenseId + " WITH " + exceptionId);
        assertThat(withLicense.getCommonName()).isEqualTo(licenseName + " WITH " + exceptionName);
        assertThat(withLicense.getText()).contains(licenseText);
        assertThat(withLicense.getText()).contains(exceptionText);
    }

    @Test
    public void pushCoverageForEqualsAndHashcode() {
        EqualsVerifier.forClass(WithLicense.class)
                .withNonnullFields("exceptionId", "exceptionName", "exceptionText")
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
