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

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseTest {
    private License license;

    @Before
    public void init() {
        this.license = new License();
        license.setName("testLicense");
        license.setText("testText");
    }

    @Test
    public void nameTest() {
        assertThat(license.getName()).isEqualTo("testLicense");

    }

    @Test
    public void textTest() {
        assertThat(license.getText()).isEqualTo("testText");
    }

}
