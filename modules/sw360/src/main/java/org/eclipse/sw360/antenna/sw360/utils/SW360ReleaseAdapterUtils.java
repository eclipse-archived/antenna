/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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

import com.here.ort.spdx.SpdxException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.util.LicenseSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SW360ReleaseAdapterUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360ReleaseAdapterUtils.class);

    public static Artifact convertToArtifact(SW360Release release, Artifact artifact) {
        artifact.setProprietary(release.isProprietary());
        Optional.ofNullable(release.getCoordinates())
                .map(Map::values)
                .map(HashSet::new)
                .map(ArtifactCoordinates::new)
                .ifPresent(artifact::addFact);

        addLicenseFact(Optional.ofNullable(release.getDeclaredLicense()), artifact, DeclaredLicenseInformation::new, artifact.askFor(DeclaredLicenseInformation.class).isPresent());
        addLicenseFact(Optional.ofNullable(release.getObservedLicense()), artifact, ObservedLicenseInformation::new, artifact.askFor(ObservedLicenseInformation.class).isPresent());
        addLicenseFact(Optional.ofNullable(release.getOverriddenLicense()), artifact, OverriddenLicenseInformation::new, artifact.askFor(OverriddenLicenseInformation.class).isPresent());

        String cpeId = release.getCpeId();
        if (cpeId != null && cpeId.startsWith("cpe:")) {
            artifact.addFact(new ArtifactCPE(cpeId));
        }
        Optional.ofNullable(release.getDownloadurl())
                .map(ArtifactSourceUrl::new)
                .ifPresent(artifact::addFact);
        Optional.ofNullable(release.getReleaseTagUrl())
                .map(ArtifactReleaseTagURL::new)
                .ifPresent(artifact::addFact);
        try {
            Optional.ofNullable(release.getSoftwareHeritageId())
                    .map(ArtifactSoftwareHeritageID.Builder::new)
                    .map(ArtifactSoftwareHeritageID.Builder::build)
                    .ifPresent(artifact::addFact);
        } catch (IllegalArgumentException e) {
            LOGGER.debug(e.getMessage());
        }
        Optional.ofNullable(release.getChangeStatus())
                .map(ArtifactChangeStatus.ChangeStatus::valueOf)
                .map(ArtifactChangeStatus::new)
                .ifPresent(artifact::addFact);
        Optional.ofNullable(release.getClearingState())
                .map(ArtifactClearingState.ClearingState::valueOf)
                .map(ArtifactClearingState::new)
                .ifPresent(artifact::addFact);
        Optional.ofNullable(release.getCopyrights())
                .map(CopyrightStatement::new)
                .ifPresent(artifact::addFact);
        if (release.getHashes() != null) {
            Set<String> hashes = release.getHashes();
            hashes.forEach(hash ->
                    artifact.addFact(new ArtifactFilename("", hash)));
        }
        // TODO missing way to get attachments into facts without downloading which isn't appropriate in utils class
        return artifact;
    }

    private static void addLicenseFact(Optional<String> licenseRawData, Artifact artifact, Function<LicenseInformation, ArtifactLicenseInformation> licenseCreator, boolean isAlreadyPresent) {
        licenseRawData.map(SW360ReleaseAdapterUtils::parseSpdxExpression)
                .map(licenseCreator)
                .ifPresent(expression -> addFactAndLogWarning(artifact, isAlreadyPresent, expression));
    }

    private static void addFactAndLogWarning(Artifact artifact, boolean isAlreadyPresent, ArtifactLicenseInformation expression) {
        if (isAlreadyPresent) {
            LOGGER.debug("License information of type {} found in SW360. Overwriting existing information in artifact.", expression.getClass().getSimpleName());
        }
        artifact.addFact(expression);
    }

    private static LicenseInformation parseSpdxExpression(String expression) {
        try {
            return LicenseSupport.fromSPDXExpression(expression);
        } catch (SpdxException e) {
            License unparsableExpression = new License();
            unparsableExpression.setName(expression);
            return unparsableExpression;
        }
    }

    public static SW360Release convertToReleaseWithoutAttachments(Artifact artifact) {
        SW360Release release = new SW360Release();
        String componentName = SW360ComponentAdapterUtils.createComponentName(artifact);

        SW360ReleaseAdapterUtils.setVersion(release, artifact);
        SW360ReleaseAdapterUtils.setCPEId(release, artifact);
        release.setName(componentName);

        SW360ReleaseAdapterUtils.setCoordinates(release, artifact);
        SW360ReleaseAdapterUtils.setOverriddenLicense(release, artifact);
        SW360ReleaseAdapterUtils.setDeclaredLicense(release, artifact);
        SW360ReleaseAdapterUtils.setObservedLicense(release, artifact);
        SW360ReleaseAdapterUtils.setSourceUrl(release, artifact);
        SW360ReleaseAdapterUtils.setReleaseTagUrl(release, artifact);
        SW360ReleaseAdapterUtils.setSwhId(release, artifact);
        SW360ReleaseAdapterUtils.setHashes(release, artifact);
        SW360ReleaseAdapterUtils.setClearingStatus(release, artifact);
        SW360ReleaseAdapterUtils.setChangeStatus(release, artifact);
        SW360ReleaseAdapterUtils.setCopyrights(release, artifact);
        release.setProprietary(artifact.isProprietary());

        return release;
    }

    public static boolean isValidRelease(SW360Release release) {
        if (release.getName() == null || release.getName().isEmpty()) {
            return false;
        }
        return release.getVersion() != null && !release.getVersion().isEmpty();
    }

    public static String createSW360ReleaseVersion(Artifact artifact) {
        return SW360ComponentAdapterUtils.createComponentVersion(artifact);
    }

    public static void setVersion(SW360Release release, Artifact artifact) {
        final String version = SW360ReleaseAdapterUtils.createSW360ReleaseVersion(artifact);
        if (!version.isEmpty()) {
            release.setVersion(version);
        }
    }

    private static void setCPEId(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactCPE.class)
                .ifPresent(release::setCpeId);
    }

    private static void setCoordinates(SW360Release release, Artifact artifact) {
        release.setCoordinates(getMapOfCoordinates(artifact));
    }

    private static Map<String, String> getMapOfCoordinates(Artifact artifact) {
        Map<String, String> coordinates = new HashMap<>();
        artifact.askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getCoordinates)
                .ifPresent(packageURLS -> packageURLS.forEach(packageURL ->
                        coordinates.put(packageURL.getType(), packageURL.canonicalize())));
        return coordinates;
    }

    private static void setOverriddenLicense(SW360Release release, Artifact artifact) {
        artifact.askForGet(OverriddenLicenseInformation.class)
                .ifPresent(licenseInformation -> release.setOverriddenLicense(licenseInformation.evaluate()));
    }

    private static void setDeclaredLicense(SW360Release release, Artifact artifact) {
        artifact.askForGet(DeclaredLicenseInformation.class)
                .ifPresent(licenseInformation -> release.setDeclaredLicense(licenseInformation.evaluate()));
    }

    private static void setObservedLicense(SW360Release release, Artifact artifact) {
        artifact.askForGet(ObservedLicenseInformation.class)
                .ifPresent(licenseInformation -> release.setObservedLicense(licenseInformation.evaluate()));
    }

    private static void setSourceUrl(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactSourceUrl.class)
                .ifPresent(release::setDownloadurl);
    }

    private static void setReleaseTagUrl(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactReleaseTagURL.class)
                .ifPresent(release::setReleaseTagUrl);
    }

    private static void setSwhId(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactSoftwareHeritageID.class)
                .ifPresent(release::setSoftwareHeritageId);
    }

    private static void setHashes(SW360Release release, Artifact artifact) {
        Set<String> hashList = artifact.askForAll(ArtifactFilename.class)
                .stream()
                .map(ArtifactFilename::getArtifactFilenameEntries)
                .flatMap(Collection::stream)
                .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                .collect(Collectors.toSet());
        release.setHashes(hashList);
    }

    private static void setClearingStatus(SW360Release release, Artifact artifact) {
        Optional<ArtifactClearingState.ClearingState> cs = artifact.askForGet(ArtifactClearingState.class);
        cs.ifPresent(clearingState -> release.setClearingState(clearingState.toString()));
    }

    private static void setChangeStatus(SW360Release release, Artifact artifact) {
        Optional<ArtifactChangeStatus.ChangeStatus> cs = artifact.askForGet(ArtifactChangeStatus.class);
        cs.ifPresent(changeStatus -> release.setChangeStatus(changeStatus.toString()));
    }

    private static void setCopyrights(SW360Release release, Artifact artifact) {
        Optional<CopyrightStatement> cs = artifact.askFor(CopyrightStatement.class);
        cs.ifPresent(copyrightStatement -> release.setCopyrights(copyrightStatement.toString()));
    }
}
