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

import java.util.Set;

public abstract class LicenseInformation {
    public abstract LicenseInformation and(LicenseInformation other);
    public abstract LicenseInformation or(LicenseInformation other);
    public abstract String toSpdxExpression();
    public abstract boolean isEmpty();
    public abstract Set<License> getLicenses();
}
