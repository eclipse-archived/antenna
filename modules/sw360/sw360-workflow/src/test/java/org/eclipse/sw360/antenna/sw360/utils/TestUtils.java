/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.Collections;
import java.util.Optional;

public class TestUtils {
    public static final String RELEASE_VERSION1 = "1.0.0";
    public static final String RELEASE_DOWNLOAD_URL = "https://organisation-test.org/";
    public static final String RELEASE_CLEARING_STATE = "PROJECT_APPROVED";
    public static final String RELEASE_DECLEARED_LICENSE = "The-Test-License";
    public static final String RELEASE_OBSERVED_LICENSE = "A-Test-License";
    public static final String RELEASE_OVERRIDEN_LICENSE = "Overriden";
    public static final String RELEASE_RELEASE_TAG_URL = "https://gitTool.com/project/repository";
    public static final String RELEASE_SOFTWAREHERITGAE_ID = "swh:1:rel:1234512345123451234512345123451234512345";
    public static final String RELEASE_HASH1= "b2a4d4ae21c789b689dd162deb819665567f481c";
    public static final String RELEASE_CHANGESTATUS = "AS_IS";
    public static final String RELEASE_COPYRIGHT = "Copyright xxxx Some Copyright Enterprise";

    public static SW360Release mkSW360Release(String name) {
        SW360Release sw360Release = new SW360Release();

        sw360Release.setVersion(RELEASE_VERSION1);

        sw360Release.setDownloadurl(RELEASE_DOWNLOAD_URL);
        sw360Release.setClearingState(RELEASE_CLEARING_STATE);

        sw360Release.setDeclaredLicense(RELEASE_DECLEARED_LICENSE);
        sw360Release.setObservedLicense(RELEASE_OBSERVED_LICENSE);
        Coordinate coordinate = new Coordinate(Coordinate.Types.MAVEN, "org.group.id", "artifactId" + name + "", RELEASE_VERSION1);
        sw360Release.setCoordinates(Collections.singletonMap(Coordinate.Types.MAVEN,
                coordinate.toString()));
        sw360Release.setReleaseTagUrl(RELEASE_RELEASE_TAG_URL);
        sw360Release.setSoftwareHeritageId(RELEASE_SOFTWAREHERITGAE_ID);
        sw360Release.setHashes(Collections.singleton(RELEASE_HASH1));
        sw360Release.setChangeStatus(RELEASE_CHANGESTATUS);
        sw360Release.setCopyrights(RELEASE_COPYRIGHT);
        sw360Release.setName(String.join("/", coordinate.getNamespace(), coordinate.getName()));

        return sw360Release;
    }

    public static Artifact mkArtifact(String name, boolean withOverridden) {
        // License information

        Artifact artifact = new Artifact("SW360");
        artifact.setProprietary(false);
        artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, "org.group.id", "artifactId" + name + "", RELEASE_VERSION1));

        ArtifactToReleaseUtils.addLicenseFact(Optional.of(RELEASE_DECLEARED_LICENSE), artifact, DeclaredLicenseInformation::new, artifact.askFor(DeclaredLicenseInformation.class).isPresent());
        ArtifactToReleaseUtils.addLicenseFact(Optional.of(RELEASE_OBSERVED_LICENSE), artifact, ObservedLicenseInformation::new, artifact.askFor(ObservedLicenseInformation.class).isPresent());
        if (withOverridden) {
            ArtifactToReleaseUtils.addLicenseFact(Optional.of(RELEASE_OVERRIDEN_LICENSE), artifact, OverriddenLicenseInformation::new, artifact.askFor(OverriddenLicenseInformation.class).isPresent());
        }
        artifact.addFact(new ArtifactSourceUrl(RELEASE_DOWNLOAD_URL));
        artifact.addFact(new ArtifactReleaseTagURL(RELEASE_RELEASE_TAG_URL));
        artifact.addFact(new ArtifactSoftwareHeritageID.Builder(RELEASE_SOFTWAREHERITGAE_ID).build());
        artifact.addFact(new ArtifactFilename(null, ("12345678" + name)));
        artifact.addFact(new ArtifactFilename(null, ("12345678" + name)));
        artifact.addFact(new ArtifactClearingState(ArtifactClearingState.ClearingState.valueOf(RELEASE_CLEARING_STATE)));
        artifact.addFact(new ArtifactChangeStatus(ArtifactChangeStatus.ChangeStatus.valueOf(RELEASE_CHANGESTATUS)));
        artifact.addFact(new CopyrightStatement(RELEASE_COPYRIGHT));

        return artifact;
    }
}
