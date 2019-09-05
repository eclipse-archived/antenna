/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.license.LicenseStatement;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseTest {
    @Test
    public void test() {
        final LicenseStatement licenseStatement = new LicenseStatement("(BSD-3-Clause)");
        assertThat(new LicenseStatement(licenseStatement.toSpdxExpression()))
                .isEqualTo(licenseStatement);
    }
}
