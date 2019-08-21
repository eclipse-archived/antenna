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
package org.eclipse.sw360.antenna.policy.workflow.processors;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.policy.engine.ThirdPartyArtifact;

/**
 * The facade of the {@link ThirdPartyArtifact} used in the {@link org.eclipse.sw360.antenna.policy.engine.PolicyEngine}
 * for retrieving metadata towards the Antenna data model.
 */
class AntennaArtifact implements ThirdPartyArtifact {
    private final Artifact artifact;

    AntennaArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    Artifact getArtifact() {
        return artifact;
    }

    @Override
    public boolean isIdentified() {
        return artifact.getMatchState() == MatchState.EXACT;
    }

    @Override
    public boolean hasLicense(String licenseRegex) {
        return ArtifactLicenseUtils.getFinalLicenses(artifact)
                .getLicenses()
                .stream()
                .map(License::getName)
                .anyMatch(license -> license.matches(licenseRegex));
    }

    @Override
    public String getPurl() {
        // ToDo: I have to check how I can extract the information for the purl ideally.
        return artifact.prettyPrint();
    }
}
