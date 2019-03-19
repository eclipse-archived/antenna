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

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;

import java.util.Collection;
import java.util.Iterator;

public class LicenseSupport {
    public static LicenseInformation mapLicenses(Collection<String> licenses) {
        return mapLicenses(licenses, LicenseOperator.AND);
    }

    public static LicenseInformation mapLicenses(Collection<String> licenses, LicenseOperator operator) {
        Iterator<String> iterator = licenses.iterator();
        if (!iterator.hasNext()) {
            return new LicenseStatement();
        }

        return computeRecursiveLicenseStatement(iterator, new LicenseStatement(), operator);
    }

    private static LicenseInformation computeRecursiveLicenseStatement(
            Iterator<String> iterator, LicenseStatement leftLicense, LicenseOperator operator) {
        License license = new License();
        license.setName(iterator.next());

        if (iterator.hasNext()) {
            leftLicense.setLeftStatement(license);
            leftLicense.setOp(operator);
            leftLicense.setRightStatement(computeRecursiveLicenseStatement(iterator, new LicenseStatement(), operator));
            return leftLicense;
        }
        return license;
    }
}
