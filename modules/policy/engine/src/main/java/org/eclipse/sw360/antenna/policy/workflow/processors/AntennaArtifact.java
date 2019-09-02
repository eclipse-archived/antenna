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

import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.util.ArtifactUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.policy.engine.ThirdPartyArtifact;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Collection<String> hasLicenses(Collection<String> searchedLicenses) {
        return ArtifactLicenseUtils.getFinalLicenses(artifact)
                .getLicenses()
                .stream()
                .map(License::getName)
                .filter(license -> searchedLicenses.contains(license))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PackageURL> getPurl() {
        return ArtifactUtils.getMostDominantArtifactCoordinates(ArtifactUtils.DEFAULT_PREFERED_COORDINATE_TYPES, artifact)
                .map(ArtifactCoordinates::getPurl);
    }
}
