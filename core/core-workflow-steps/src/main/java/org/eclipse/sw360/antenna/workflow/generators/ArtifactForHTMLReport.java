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
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.util.ArtifactUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactForHTMLReport {
    private final String identifier;
    private final LicenseInformation license;
    private final String ankerTag;

    private static final List<Class<? extends ArtifactCoordinates>> preferedCoordinatesTypes = Stream.of(
            MavenCoordinates.class,
            BundleCoordinates.class).collect(Collectors.toList());

    public ArtifactForHTMLReport(Artifact artifact) {
        this.identifier = ArtifactUtils.getMostDominantArtifactCoordinates(preferedCoordinatesTypes, artifact)
                .map(ArtifactCoordinates::toString)
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
