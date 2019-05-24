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
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.OverriddenLicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;

import java.util.Optional;

public class ArtifactLicenseUtils {

    private ArtifactLicenseUtils(){
        // only static methods
    }

    /*
     * The final license is calculated as follows:
     *   - if the configuration overwrites the license (represented by a ConfiguredLicenseInformation fact), that one is used
     *   - if the tooling has provided an overridden (represented by an OverriddenLicenseInformation fact), that one is used
     *   - if the tooling has provided
     *     - a license declared by the package and/or
     *     - a observed licenses (e.g. by scanners)
     *     the resulting effective license is calculated as {declared licenses} AND {observed licenses}
     *   - otherwise an empty license is returned
     */
    public static LicenseInformation getFinalLicenses(Artifact artifact) {
        final Optional<LicenseInformation> configured = artifact.askForGet(ConfiguredLicenseInformation.class);

        if(configured.isPresent()) {
            return configured.get();
        }

        final Optional<LicenseInformation> overridden = artifact.askForGet(OverriddenLicenseInformation.class);

        if(overridden.isPresent()) {
            return overridden.get();
        }

        final Optional<LicenseInformation> declared = artifact.askForGet(DeclaredLicenseInformation.class);
        final Optional<LicenseInformation> observed = artifact.askForGet(ObservedLicenseInformation.class);
        if(declared.isPresent() && observed.isPresent()) {
            final LicenseStatement effective = new LicenseStatement();
            effective.setLeftStatement(declared.get());
            effective.setRightStatement(observed.get());
            effective.setOp(LicenseOperator.AND);
            return effective;
        }

        return declared.orElse(observed.orElse(new LicenseStatement()));
    }
}
