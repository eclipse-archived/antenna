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

import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class LicenseSupportTest {
    @Test
    public void singleLicenseIsTreatedSimply() {
        Collection<String> license = Collections.singletonList("Single License");
        LicenseInformation actualLicenseSupport = LicenseSupport.mapLicenses(license);

        Assertions.assertThat(actualLicenseSupport.toString()).startsWith("Single License");
    }

    @Test
    public void mulitpleLicensesTreatedCorrectly() {
        Collection<String> license = Arrays.asList("First License", "Second License", "Third License");
        LicenseInformation actualLicenseSupport = LicenseSupport.mapLicenses(license);

        Assertions.assertThat(actualLicenseSupport.toString())
                .startsWith("( First License AND ( Second License AND Third License ) )");
    }
}
