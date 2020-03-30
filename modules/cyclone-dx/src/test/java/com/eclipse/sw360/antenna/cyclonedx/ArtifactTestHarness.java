/*
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2020.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package com.eclipse.sw360.antenna.cyclonedx;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.util.LicenseSupport;
import java.util.Optional;
import java.util.function.Function;

public class ArtifactTestHarness {
    public static final Artifact EMPTY = new ArtifactTestHarness.Builder().build();

    public static class Builder
    {
        Artifact artifact = new Artifact();

        public Artifact build()
        {
            return artifact;
        }

        public Builder setProprietary(boolean proprietary)
        {
            artifact.setProprietary(proprietary);
            return this;
        }

        public Builder setSimpleMaven(String artifactId)
        {
            return setMaven("org.example.group.id", artifactId, "1.2.3");
        }

        public Builder setMaven(String groupId, String artifactId, String version)
        {
            artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, groupId, artifactId, version));
            return this;
        }

        public Builder setNPM(String name, String version)
        {
            artifact.addCoordinate(new Coordinate(Coordinate.Types.NPM, name, version));
            return this;
        }

        public Builder setDeclared(String declaredLicenseName)
        {
            addLicenseFact(Optional.of(declaredLicenseName), artifact, DeclaredLicenseInformation::new, artifact.askFor(DeclaredLicenseInformation.class).isPresent());
            return this;
        }

        public Builder setObserved(String observedLicenseName)
        {
            addLicenseFact(Optional.of(observedLicenseName), artifact, ObservedLicenseInformation::new, artifact.askFor(ObservedLicenseInformation.class).isPresent());
            return this;
        }

        private static void addLicenseFact(Optional<String> licenseRawData, Artifact artifact, Function<LicenseInformation, ArtifactLicenseInformation> licenseCreator, boolean isAlreadyPresent) {
            licenseRawData.map(LicenseSupport::parseSpdxExpression)
                    .map(licenseCreator)
                    .ifPresent(artifact::addFact);
        }

        public Builder setSourceUrl(String url)
        {
            artifact.addFact(new ArtifactSourceUrl(url));
            return this;
        }

        public Builder addFilename(String filename, String hash)
        {
            artifact.addFact(new ArtifactFilename(filename, hash));
            return this;
        }

        public Builder addFilename(String filename, String hash, String hashAlgorithm)
        {
            artifact.addFact(new ArtifactFilename(filename, hash, hashAlgorithm));
            return this;
        }
    }
}
