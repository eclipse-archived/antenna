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
        String licenseId = "Apache-2.0";
        String licenseName = "Apache License 2.0";
        String licenseText = "Too long";
        License license = new License(licenseId, licenseName, licenseText);
        String exceptionId = "Exp.";
        String exceptionName = "Exception";
        String exceptionText = "Text of Exception.";
        License exception = new License(exceptionId, exceptionName, exceptionText);
        WithLicense withLicense = new WithLicense(license, exception);

        assertThat(withLicense.isEmpty()).isFalse();
        assertThat(withLicense.getLicenses().size())
                .isEqualTo(2);

        assertThat(withLicense.getLicense()).isEqualTo(license);
        assertThat(withLicense.getException()).isEqualTo(exception);

        assertThat(withLicense.evaluate()).isEqualTo("Apache-2.0 WITH Exp.");
        assertThat(withLicense.evaluateLong()).isEqualTo("Apache License 2.0 WITH Exception");
    }

    @Test
    public void testWithLicenseFromScratch() {
        WithLicense withLicense = new WithLicense();

        assertThat(withLicense.isEmpty()).isTrue();

        License license = new License("Apache-2.0");
        License exception = new License("Exception");
        withLicense.setLicense(license);
        withLicense.setException(exception);

        assertThat(withLicense.isEmpty()).isFalse();
        assertThat(withLicense.toString()).isEqualTo("Apache-2.0 WITH Exception");
    }

    @Test
    public void pushCoverageForEqualsAndHashcode() {
        EqualsVerifier.forClass(WithLicense.class)
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
