/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
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

import org.eclipse.sw360.antenna.model.util.LicenseComparator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseComparatorTest {

    @Test
    public void testCompare() {
        License license = new License();
        license.setId("EPL-1.0");
        license.setText("");
        License compareLicense = new License();
        compareLicense.setId("a");
        LicenseComparator comparator = new LicenseComparator();
        assertThat(comparator.compare(license, license)).isEqualTo(0);
        assertThat(comparator.compare(compareLicense, license)).isPositive();
        assertThat(comparator.compare(license, compareLicense)).isNegative();
    }

}
