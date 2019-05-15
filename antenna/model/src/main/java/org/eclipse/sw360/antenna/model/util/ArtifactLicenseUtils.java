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

package org.eclipse.sw360.antenna.model.util;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ConfiguredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.OverriddenLicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;

public class ArtifactLicenseUtils {

    private ArtifactLicenseUtils(){
        // only static methods
    }

    public static LicenseInformation getFinalLicenses(Artifact artifact) {
        return artifact.askForGet(ConfiguredLicenseInformation.class)
                .orElse(artifact.askForGet(OverriddenLicenseInformation.class)
                        .orElse(artifact.askForGet(DeclaredLicenseInformation.class)
                                .orElse(new LicenseStatement())));
    }
}
