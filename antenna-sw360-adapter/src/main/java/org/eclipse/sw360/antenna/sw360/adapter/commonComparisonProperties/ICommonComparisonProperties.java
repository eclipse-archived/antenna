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
package org.eclipse.sw360.antenna.sw360.adapter.commonComparisonProperties;

import java.util.List;

public interface ICommonComparisonProperties {
    String getName();

    List<String> getVersions();

    default boolean matchesOnComparisonProperties(ICommonComparisonProperties other) {
        String obj1Name = getName();
        String obj2Name = other.getName();
        if (! obj1Name.equals(obj2Name)) {
            return false;
        }

        List<String> obj1Versions = getVersions();
        List<String> obj2Versions = other.getVersions();
        return obj1Versions.stream()
                .anyMatch(obj2Versions::contains);
    }
}
