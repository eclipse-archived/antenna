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

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseStatementTest {
    @Test
    public void testEmptyLicenseStatement() {
        LicenseStatement licenseStatement = new LicenseStatement();

        assertThat(licenseStatement.isEmpty()).isTrue();
        assertThat(licenseStatement.getLicenses().size()).isEqualTo(0);
    }

    @Test
    public void testSimpleLicenseStatement() {
        License license1 = new License("Apache-2.0", "Apache License 2.0", "Too long");
        License license2 = new License("EPL-2.0", "Eclipse Public License 2.0", "Also too long");
        LicenseStatement licenseStatement = new LicenseStatement(Arrays.asList(license1, license2), LicenseOperator.AND);

        assertThat(licenseStatement.isEmpty()).isFalse();
        assertThat(licenseStatement.getLicenses().size()).isEqualTo(2);
        assertThat(licenseStatement.getOp()).isEqualTo(LicenseOperator.AND);

        assertThat(licenseStatement.getLicenses()
                .stream()
                .map(License::getId)
                .anyMatch(l -> "Apache-2.0".equals(l)))
            .isTrue();
        assertThat(licenseStatement.getLicenses()
                .stream()
                .map(License::getId)
                .anyMatch(l -> "EPL-2.0".equals(l)))
            .isTrue();

        assertThat(licenseStatement.evaluate()).isEqualTo("( Apache-2.0 AND EPL-2.0 )");
        assertThat(licenseStatement.evaluateLong())
                .isEqualTo("( Apache License 2.0 AND Eclipse Public License 2.0 )");
    }

    @Test
    public void testNullLicenseStatement() {
        LicenseStatement licenseStatement = new LicenseStatement();
        licenseStatement.setLicenses(null);

        assertThat(licenseStatement.getLicenses()).isNotNull();
        assertThat(licenseStatement.evaluate()).isEqualTo("");
        assertThat(licenseStatement.evaluateLong()).isEqualTo("");
    }

    @Test
    public void testAddingLicensesToStatement() {
        License license1 = new License("Apache-2.0");
        License license2 = new License("EPL-2.0");
        License license3 = new License("GPL");
        LicenseStatement licenseStatement = new LicenseStatement(Stream.of(license1, license2).collect(Collectors.toList()),
                LicenseOperator.AND);

        assertThat(licenseStatement.isEmpty()).isFalse();
        assertThat(licenseStatement.getLicenses().size()).isEqualTo(2);
        assertThat(licenseStatement.getLicenses()
                .stream()
                .map(License::getId)
                .anyMatch(n -> "GPL".equals(n)))
                .isFalse();

        licenseStatement.addLicenseInformation(license3);
        assertThat(licenseStatement.getLicenses().size()).isEqualTo(3);
        assertThat(licenseStatement.getLicenses()
                .stream()
                .map(License::getId)
                .anyMatch(n -> "GPL".equals(n)))
            .isTrue();
    }

    @Test
    public void pushCoverageForEqualsAndHashcode() {
        EqualsVerifier.forClass(LicenseStatement.class)
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
