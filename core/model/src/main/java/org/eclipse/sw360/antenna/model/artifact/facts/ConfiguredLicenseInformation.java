/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model.artifact.facts;


import org.eclipse.sw360.antenna.model.license.LicenseInformation;

public class ConfiguredLicenseInformation extends ArtifactLicenseInformation {
    public ConfiguredLicenseInformation(LicenseInformation payload) {
        super(payload);
    }

    @Override
    public String getFactContentName() {
        return "Configured License";
    }
}
