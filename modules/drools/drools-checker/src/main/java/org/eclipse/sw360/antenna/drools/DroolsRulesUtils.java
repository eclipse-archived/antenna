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
package org.eclipse.sw360.antenna.drools;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;

import java.util.Collections;

public final class DroolsRulesUtils {
    private DroolsRulesUtils() {

    }

    public static boolean isLicenseInFinalLicenses(Artifact artifact, String licenseId) {
        return ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses().stream()
                .map(License::getName)
                .anyMatch(licenseId::equals);
    }

    public static boolean isThreatGroupInArtifact(Artifact artifact, LicenseThreatGroup threatGroup) {
        return ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses().stream()
                .map(License::getThreatGroup)
                .anyMatch(l -> threatGroup == null && l == null || threatGroup != null && threatGroup.equals(l));
    }

    public static <T extends ArtifactLicenseInformation> boolean isLicenseTypeInArtifact(Artifact artifact, Class<T> rawLicenseClass) {
        return artifact.askFor(rawLicenseClass).isPresent();
    }

    public static boolean isMissingLicenseInformationInArtifact(Artifact artifact, MissingLicenseReasons reason) {
        return artifact.askFor(MissingLicenseInformation.class)
                .map(MissingLicenseInformation::getMissingLicenseReasons)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(r -> r.equals(reason));
    }

    public static boolean isArtifactsSecurityIssuesRisky(Artifact artifact, double securitySeverityLimit, SecurityIssueStatus securityStatus) {
        return artifact.askForGet(ArtifactIssues.class)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(i -> i.getSeverity() > securitySeverityLimit && securityStatus.equals(i.getStatus()));
    }

    public static boolean matchingCoordinatesInArtifact(Artifact artifact, ArtifactIdentifier pattern) {
        return artifact.askFor(ArtifactCoordinates.class)
                .map(pattern::matches)
                .orElse(false);
    }

    public static boolean isMatchStateInArtifact(Artifact artifact, MatchState matchState) {
        return artifact.askFor(ArtifactMatchingMetadata.class)
                .map(ArtifactMatchingMetadata::getMatchState)
                .orElse(MatchState.UNKNOWN)
                .equals(matchState);
    }
}
