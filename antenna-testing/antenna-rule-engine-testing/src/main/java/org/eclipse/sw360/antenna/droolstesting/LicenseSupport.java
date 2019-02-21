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
package org.eclipse.sw360.antenna.droolstesting;

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;

import java.util.Collection;
import java.util.Iterator;

// TODO This needs to be moved somewhere else
public class LicenseSupport {
    public static LicenseInformation mapLicenses(Collection<String> licenses, LicenseOperator op) {
        Iterator<String> iterator = licenses.iterator();
        if (!iterator.hasNext()) {
            return new LicenseStatement();
        }

        return computeRecursiveLicenseStatement(iterator, new LicenseStatement(), op);
    }

    private static LicenseInformation computeRecursiveLicenseStatement(
            Iterator<String> iterator, LicenseStatement leftLicense, LicenseOperator op) {
        License license = new License();
        license.setName(iterator.next());

        if (iterator.hasNext()) {
            leftLicense.setLeftStatement(license);
            leftLicense.setOp(op);
            leftLicense.setRightStatement(computeRecursiveLicenseStatement(iterator, new LicenseStatement(), op));
            return leftLicense;
        }
        return license;
    }
}
