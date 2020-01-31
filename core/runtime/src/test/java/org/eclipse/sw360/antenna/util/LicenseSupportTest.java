/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.util;

import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseSupportTest {
    @Test
    public void singleLicenseIsTreatedSimply() {
        Collection<String> license = Collections.singletonList("Single License");
        LicenseInformation actualLicenseSupport = LicenseSupport.mapLicenses(license);

        assertThat(actualLicenseSupport.toString()).startsWith("Single License");
    }

    @Test
    public void mulitpleLicensesTreatedCorrectly() {
        Collection<String> license = Arrays.asList("First License", "Second License", "Third License");
        LicenseInformation actualLicenseSupport = LicenseSupport.mapLicenses(license);

        assertThat(actualLicenseSupport.toString())
                .startsWith("( First License AND ( Second License AND Third License ) )");
    }

    @Test
    public void testSPDXParsing() {
        final LicenseInformation licenseInformation = LicenseSupport.fromSPDXExpression("BSD AND APACHE-2.0");
        assertThat(licenseInformation.getLicenses().size())
                .isEqualTo(2);
        LicenseStatement licenseStatement = (LicenseStatement) licenseInformation;
        assertThat(licenseStatement.getOp())
                .isEqualTo(LicenseOperator.AND);
        final LicenseInformation licenseInformation1 = LicenseSupport.fromSPDXExpression(licenseInformation.evaluate());
        assertThat(licenseInformation)
                .isEqualTo(licenseInformation1);
    }

    @Test
    public void testSPDXParsing2() {
        final LicenseInformation licenseInformation = LicenseSupport.fromSPDXExpression("BSD AND APACHE-2.0 OR MIT AND GPL-2.0+");
        assertThat(licenseInformation.getLicenses().size())
                .isEqualTo(4);
        LicenseStatement licenseStatement = (LicenseStatement) licenseInformation;
        assertThat(licenseStatement.getOp())
                .isEqualTo(LicenseOperator.OR);
        final LicenseInformation licenseInformation1 = LicenseSupport.fromSPDXExpression(licenseInformation.evaluate());
        assertThat(licenseInformation)
                .isEqualTo(licenseInformation1);
    }

    @Test
    public void testSPDXParsing3() {
        final LicenseInformation licenseInformation = LicenseSupport.fromSPDXExpression("MIT OR BSD AND APACHE-2.0 OR GPL-2.0-or-later");
        assertThat(licenseInformation.getLicenses().size())
                .isEqualTo(4);
        LicenseStatement licenseStatement = (LicenseStatement) licenseInformation;
        assertThat(licenseStatement.getOp())
                .isEqualTo(LicenseOperator.OR);
        final LicenseInformation licenseInformation1 = LicenseSupport.fromSPDXExpression(licenseInformation.evaluate());
        assertThat(licenseInformation)
                .isEqualTo(licenseInformation1);
    }

    @Test
    public void testSPDXParsing4() {
        final LicenseInformation licenseInformation = LicenseSupport.fromSPDXExpression("(MIT OR BSD) AND (APACHE-2.0 OR GPL-2.0-or-later)");
        assertThat(licenseInformation.getLicenses().size())
                .isEqualTo(4);
        LicenseStatement licenseStatement = (LicenseStatement) licenseInformation;
        assertThat(licenseStatement.getOp())
                .isEqualTo(LicenseOperator.AND);
        final LicenseInformation licenseInformation1 = LicenseSupport.fromSPDXExpression(licenseInformation.evaluate());
        assertThat(licenseInformation)
                .isEqualTo(licenseInformation1);
    }

    @Test
    public void testParseSpdxExpression() {
        final LicenseInformation licenseInformation = LicenseSupport.parseSpdxExpression("BSD AND APACHE-2.0");
        assertThat(licenseInformation.getLicenses().size())
                .isEqualTo(2);
        LicenseStatement licenseStatement = (LicenseStatement) licenseInformation;
        assertThat(licenseStatement.getOp())
                .isEqualTo(LicenseOperator.AND);
    }

    @Test
    public void testParseSpdxExpressionWithNonValidExpression() {
        final String license = "APACHE-2.0 PLUS BSD";
        final LicenseInformation licenseInformation = LicenseSupport.parseSpdxExpression(license);
        assertThat(licenseInformation.getLicenses().size())
                .isEqualTo(1);
        assertThat(licenseInformation.getLicenses().get(0).getName())
                .isEqualTo(license);
    }
}
