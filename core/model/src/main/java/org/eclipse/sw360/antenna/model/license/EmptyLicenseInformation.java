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
package org.eclipse.sw360.antenna.model.license;

import java.util.Collections;
import java.util.Set;

public class EmptyLicenseInformation extends LicenseInformation {
    @Override
    public LicenseInformation and(LicenseInformation other) {
        return new LicenseStatement(other);
    }

    @Override
    public LicenseInformation or(LicenseInformation other) {
        return new LicenseStatement(other);
    }

    @Override
    public String toSpdxExpression() {
        return "";
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Set<License> getLicenses() {
        return Collections.emptySet();
    }
}
