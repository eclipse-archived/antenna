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

package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.license.LicenseStatement;

public abstract class ArtifactLicenseInformation extends ArtifactFactWithPayload<LicenseInformation> {
    public ArtifactLicenseInformation(LicenseInformation payload) {
        super(payload);
    }

    public static LicenseInformation getDefault() {
        return new LicenseStatement();
    }

    @Override
    public boolean isEmpty() {
        return isEmpty(get());
    }

    public boolean isEmpty(LicenseInformation licenseInformation) {
        if(licenseInformation == null) {
            return true;
        }
        return licenseInformation.isEmpty();
    }
}
