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
package org.eclipse.sw360.antenna.predicates;

import org.eclipse.sw360.antenna.model.xml.generated.License;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class LicensePredicates {
    private LicensePredicates() {
        // only statics
    }

    public static boolean hasNoText(License license) {
        return isBlank(license.getText());
    }
}
