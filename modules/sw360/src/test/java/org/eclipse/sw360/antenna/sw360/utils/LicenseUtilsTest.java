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

package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseUtilsTest {

    @Test
    public void makeLicenseStatementFromStringTest() throws Exception {
        License expected = new License();
        expected.setName("BSD");

        Stream.of("BSD",
                " BSD   ",
                "( BSD )",
                " (BSD  )")
                .map(LicenseUtils::makeLicenseStatementFromString)
                .forEach(license -> assertThat(license).isEqualTo(expected));
    }

    @Test
    public void makeLicenseStatementFromComplicatedAndStringTest() throws Exception {
        LicenseStatement expected = new LicenseStatement();
        License bsd = new License();
        bsd.setName("BSD");
        expected.setLeftStatement(bsd);
        License apache = new License();
        apache.setName("Apache-2.0");
        expected.setRightStatement(apache);
        expected.setOp(LicenseOperator.AND);

        Stream.of("BSD AND Apache-2.0",
                "(BSD AND Apache-2.0 )",
                "( BSD AND Apache-2.0 )",
                "(BSD and Apache-2.0 AND () )",
                "(BSD AND ( Apache-2.0 AND ) )")
                .map(LicenseUtils::makeLicenseStatementFromString)
                .forEach(statement -> assertThat(statement).isEqualTo(expected));
    }

    @Test
    public void makeLicenseStatementFromComplicatedOrStringTest() throws Exception {
        LicenseStatement expected = new LicenseStatement();
        License bsd = new License();
        bsd.setName("BSD");
        expected.setLeftStatement(bsd);
        License apache = new License();
        apache.setName("Apache-2.0");
        expected.setRightStatement(apache);
        expected.setOp(LicenseOperator.OR);

        Stream.of("BSD OR Apache-2.0",
                "(BSD OR Apache-2.0 )",
                "( BSD OR Apache-2.0 )",
                "(BSD or Apache-2.0 OR () )",
                "(BSD OR ( Apache-2.0 OR ) )")
                .map(LicenseUtils::makeLicenseStatementFromString)
                .forEach(statement -> assertThat(statement).isEqualTo(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void makeLicenseStatementFromUnsupportedStringTest() throws Exception {
        LicenseUtils.makeLicenseStatementFromString("( BSD OR ( Apache-2.0 AND MIT) )");
    }
}
