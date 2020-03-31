/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.license;

import java.util.Collection;

/**
 * A general interface for a license expression. It is implemented by {@link LicenseStatement} which reflects a SPDX
 * license expression or a single {@link License}, resp. a {@link WithLicense}, i.e., a license with an exception
 * according to the SPDX expression standard.
 */
public interface LicenseInformation {
    /**
     * Give a canonical form of the license expression, i.e., this returns a string with the SPDX expression of the
     * license statement. In cases where the licenses referenced are not referenced by a SPDX identifier, the expression
     * reflects something close to a SPDX expression but with unstandardized identifiers.
     *
     * @return An SPDX license expression, never null.
     *
     */
    String evaluate();

    /**
     * @return True, if the license expression is an empty string, false, if it contains data.
     */
    boolean isEmpty();

    /**
     * @return A flat list of all licenses referenced in the license expression, loosing the information on how licenses
     * are combined in the expression, never null.
     */
    Collection<License> getLicenses();
}
