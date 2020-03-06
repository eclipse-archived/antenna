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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseOperatorTest {
    @Test
    public void testLicenseOperator() {
        assertThat(LicenseOperator.fromValue("and"))
                .isEqualTo(LicenseOperator.AND);
        assertThat(LicenseOperator.fromValue("And"))
                .isEqualTo(LicenseOperator.AND);
        assertThat(LicenseOperator.fromValue("AND"))
                .isEqualTo(LicenseOperator.AND);

        assertThat(LicenseOperator.fromValue("or"))
                .isEqualTo(LicenseOperator.OR);
        assertThat(LicenseOperator.fromValue("Or"))
                .isEqualTo(LicenseOperator.OR);
        assertThat(LicenseOperator.fromValue("OR"))
                .isEqualTo(LicenseOperator.OR);
    }
}
