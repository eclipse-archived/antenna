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

package org.eclipse.sw360.antenna.model.util;


import org.eclipse.sw360.antenna.model.license.LicenseInformation;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Can be used to compare the names of two licenses. Uses the compare method of
 * string.
 */
public class LicenseComparator implements Comparator<LicenseInformation>, Serializable {

    /**
     * Compares the names of the two license using String.compareTo().
     *
     * @param license1
     * @param license2
     * @return licenseName1.compareTo(licenseName2);
     */
    @Override
    public int compare(LicenseInformation license1, LicenseInformation license2) {
        String licenseName1 = license1.evaluate();
        String licenseName2 = license2.evaluate();
        return licenseName1.compareTo(licenseName2);
    }

}
