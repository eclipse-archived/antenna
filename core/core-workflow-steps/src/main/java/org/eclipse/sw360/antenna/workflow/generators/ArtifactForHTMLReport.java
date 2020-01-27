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
package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;

import java.util.UUID;

public class ArtifactForHTMLReport {
    private final String identifier;
    private final LicenseInformation license;
    private final String ankerTag;

    public ArtifactForHTMLReport(Artifact artifact) {
        this.identifier = artifact.askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getMainCoordinate)
                .map(Object::toString)
                .orElse("");
        this.ankerTag = UUID.randomUUID().toString();
        this.license = ArtifactLicenseUtils.getFinalLicenses(artifact);
    }


    public ArtifactForHTMLReport(String identifier, LicenseInformation licenseInformation) {
        this.identifier = identifier;
        this.license = licenseInformation;
        this.ankerTag = UUID.randomUUID().toString();
    }

    public String getIdentifier() {
        return identifier;
    }

    public LicenseInformation getLicense() {
        return license;
    }

    public String getAnkerTag() {
        return ankerTag;
    }
}
