/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.comparator;

import java.util.List;

public class ArtifactSW360ComponentComparator {
    public static boolean compare(ICommonComparisonProperties obj1, ICommonComparisonProperties obj2) {
        String obj1Name = obj1.getName();
        String obj2Name = obj2.getName();
        boolean isNameEqual = obj1Name.equals(obj2Name);
        if (!isNameEqual) {
            return false;
        }
        List<String> obj1Versions = obj1.getVersions();
        List<String> obj2Versions = obj2.getVersions();
        if ((obj1Versions.size() == 0) || (obj2Versions.size() == 0)) {
            return false;
        }
        obj1Versions.retainAll(obj2Versions);
        if (obj1Versions.size() >= 1) {
            return true;
        }
        return false;
    }
}
