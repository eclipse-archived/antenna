/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import org.eclipse.sw360.antenna.model.license.EmptyLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;

import java.util.Collection;
import java.util.function.BinaryOperator;

public class LicenseSupport {
    public static LicenseInformation mapLicenses(Collection<String> licenses) {
        return mapLicenses(licenses, LicenseInformation::and);
    }

    public static LicenseInformation mapLicenses(Collection<String> licenses, BinaryOperator<LicenseInformation> operator) {
        return licenses.stream()
                .map(licenseId -> new License.Builder().setLicenseId(licenseId).build())
                .map(l -> (LicenseInformation) l)
                .reduce(new EmptyLicenseInformation(), operator);
    }
}
