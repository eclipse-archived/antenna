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
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseData;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseState;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
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
    public boolean isProprietary() {
        return artifact.isProprietary();
    }

    @Override
    public LicenseState getLicenseState() {
        if (artifact.askForGet(ConfiguredLicenseInformation.class).isPresent()) {
            return LicenseState.EXPLICITLY_SET;
        }

        if (artifact.askForGet(OverriddenLicenseInformation.class).isPresent()) {
            return LicenseState.EXPLICITLY_SET;
        }

        final Optional<LicenseInformation> declared = artifact.askForGet(DeclaredLicenseInformation.class);
        final Optional<LicenseInformation> observed = artifact.askForGet(ObservedLicenseInformation.class);

        if(declared.isPresent() && observed.isPresent()) {
            return LicenseState.DECLARED_AND_OBSERVED;
        }

        if (declared.isPresent()) {
            return LicenseState.DECLARED_ONLY;
        }

        if (observed.isPresent()) {
            return LicenseState.OBSERVED_ONLY;
        }

        return LicenseState.NO_LICENSE;
    }

    @Override
    public Collection<LicenseData> getLicenses() {
        return ArtifactLicenseUtils.getFinalLicenses(artifact)
                .getLicenses()
                .stream()
                .map(AntennaLicenseData::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> getLicenseExpression() {
        LicenseInformation licenseInformation = ArtifactLicenseUtils.getFinalLicenses(artifact);
        if (licenseInformation.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(licenseInformation.evaluate());
    }

    @Override
    public Optional<URL> getSourceFileOrLink() {
        Optional<Path> sourcePath = artifact.askForGet(ArtifactSourceFile.class);
        if (sourcePath.isPresent()) {
            try {
                return Optional.of(sourcePath.get().toUri().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        Optional<String> sourceUrl = artifact.askForGet(ArtifactSourceUrl.class);
        if (sourceUrl.isPresent()) {
            try {
                return Optional.of(new URL(sourceUrl.get()));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> getSWHSourceId() {
        return artifact.askForGet(ArtifactSoftwareHeritageID.class);
    }

    @Override
    public Collection<Coordinate> getCoordinates() {
        return artifact.getCoordinates();
    }
}
