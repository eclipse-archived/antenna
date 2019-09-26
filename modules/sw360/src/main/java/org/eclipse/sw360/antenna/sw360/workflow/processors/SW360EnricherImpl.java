/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.workflow.processors;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360CoordinateKeysToArtifactCoordinates;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseEmbedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SW360EnricherImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SW360EnricherImpl.class);

    private final IProcessingReporter reporter;
    private final SW360MetaDataReceiver connector;

    public SW360EnricherImpl(IProcessingReporter reporter, SW360MetaDataReceiver connector) {
        this.reporter = reporter;
        this.connector = connector;
    }

    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        for (Artifact artifact : intermediates) {
            Optional<SW360Release> release = connector.findReleaseForArtifact(artifact);
            if (release.isPresent()) {
                final SW360Release sw360Release = release.get();

                updateLicenses(artifact, sw360Release);
                addSourceUrlIfAvailable(artifact, sw360Release);
                addCPEIdIfAvailable(artifact, sw360Release);
                addClearingStateIfAvailable(artifact, sw360Release);
                mapExternalIdsOnSW360Release(sw360Release, artifact);
            } else {
                warnAndReport(artifact, "No SW360 release found for artifact.");
            }
        }
        return intermediates;
    }

    private void addClearingStateIfAvailable(Artifact artifact, SW360Release release) {
        if (release.getClearingState() != null && !release.getClearingState().isEmpty()) {
            artifact.addFact(new ArtifactClearingState(
                    ArtifactClearingState.ClearingState.valueOf(release.getClearingState())));
        }
    }

    private void mapExternalIdsOnSW360Release(SW360Release sw360Release, Artifact artifact) {
        mapCoordinates(sw360Release)
                .forEach(artifact::addFact);

        artifact.addFact(new DeclaredLicenseInformation(makeLicenseStatementFromString(sw360Release.getDeclaredLicense())));
        artifact.addFact(new ObservedLicenseInformation(makeLicenseStatementFromString(sw360Release.getObservedLicense())));
        artifact.addFact(new ArtifactReleaseTagURL(sw360Release.getReleaseTagUrl()));
        try {
            artifact.addFact(new ArtifactSoftwareHeritageID.Builder(sw360Release.getSoftwareHeritageId()).build());
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        sw360Release.getHashes().stream()
                .map(hash -> new ArtifactFilename(null, hash))
                .forEach(artifact::addFact);
        Optional.ofNullable(sw360Release.getChangeStatus())
                .map(ArtifactChangeStatus.ChangeStatus::valueOf)
                .map(ArtifactChangeStatus::new)
                .ifPresent(artifact::addFact);
        artifact.addFact(new CopyrightStatement(sw360Release.getCopyrights()));
    }

    private License makeLicenseStatementFromString(String license) {
        License license1 = new License();
        license1.setName(license);

        return license1;
    }

    private Stream<ArtifactCoordinates> mapCoordinates(SW360Release sw360Release) {
        final Map<String, String> coordinates = sw360Release.getCoordinates();
        return SW360CoordinateKeysToArtifactCoordinates.getKeys()
                .stream()
                .map(key -> {
                    String coordinateType = SW360CoordinateKeysToArtifactCoordinates.get(key);
                    final String coordinatesString = coordinates.get(coordinateType);
                    if(coordinatesString == null) {
                        return null;
                    }

                    String[] splitCoordiantes = coordinatesString.split(":");

                    if (splitCoordiantes.length == 3) {
                        return SW360CoordinateKeysToArtifactCoordinates.createArtifactCoordinates(splitCoordiantes[0], sw360Release.getName(), sw360Release.getVersion(), key);
                    } else {
                        return SW360CoordinateKeysToArtifactCoordinates.createArtifactCoordinates("", sw360Release.getName(), sw360Release.getVersion(), key);
                    }
                })
                .filter(Objects::nonNull);
    }

    private void addSourceUrlIfAvailable(Artifact artifact, SW360Release release) {
        if (release.getDownloadurl() != null && !release.getDownloadurl().isEmpty()) {
            artifact.addFact(new ArtifactSourceUrl(release.getDownloadurl()));
        }
    }

    private void updateLicenses(Artifact artifact, SW360Release release) {
        List<License> artifactLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        final SW360ReleaseEmbedded embedded = release.get_Embedded();
        if (embedded == null) {
            return;
        }
        List<SW360SparseLicense> releaseLicenses = new ArrayList<>(embedded.getLicenses());

        if (!artifactLicenses.isEmpty()) {
            if (releaseLicenses == null || releaseLicenses.isEmpty()) {
                LOGGER.info("License information available in antenna but not in SW360.");
            } else {
                if (hasDifferentLicenses(artifactLicenses, releaseLicenses)) {
                    warnAndReport(artifact, "Licenses are different between artifact and SW360. Overwriting with licenses from SW360.");
                    logLicenseDifference(artifactLicenses, releaseLicenses);

                    setLicensesForArtifact(artifact, releaseLicenses);
                }
            }
        } else {
            if (!releaseLicenses.isEmpty()) {
                LOGGER.info("License information is missing in artifact. Adding licenses from SW360.");
                logLicenseDifference(artifactLicenses, releaseLicenses);

                setLicensesForArtifact(artifact, releaseLicenses);
            }
        }
    }

    private boolean hasDifferentLicenses(List<License> artifactLicenses, List<SW360SparseLicense> releaseLicenses) {
        List<String> artifactLicenseNames = artifactLicenses.stream()
                .map(License::getName)
                .collect(Collectors.toList());
        List<String> releaseLicenseNames = releaseLicenses.stream()
                .map(SW360SparseLicense::getShortName)
                .collect(Collectors.toList());
        return !(releaseLicenseNames.containsAll(artifactLicenseNames) && artifactLicenseNames.containsAll(releaseLicenseNames));
    }

    private License makeLicenseFromLicenseDetails(SW360License licenseDetails) {
        License license = new License();
        license.setName(licenseDetails.getShortName());
        license.setLongName(licenseDetails.getFullName());
        license.setText(licenseDetails.getText());
        return license;
    }

    private Optional<License> enrichSparseLicenseFromSW360(SW360SparseLicense sparseLicense) {
        Optional<License> licenseDetails = connector.getLicenseDetails(sparseLicense)
                .map(this::makeLicenseFromLicenseDetails);
        if (!licenseDetails.isPresent()) {
            LOGGER.warn("Could not get details for license " + sparseLicense.getFullName());
        }
        return licenseDetails;
    }

    private LicenseStatement appendToLicenseStatement(LicenseStatement licenseStatement, License license) {
        LicenseStatement newLicenseStatement = new LicenseStatement();
        newLicenseStatement.setLeftStatement(licenseStatement);
        newLicenseStatement.setOp(LicenseOperator.AND);
        newLicenseStatement.setRightStatement(license);
        return newLicenseStatement;
    }

    private void setLicensesForArtifact(Artifact artifact, List<SW360SparseLicense> licenses) {
        LicenseStatement licenseStatement = new LicenseStatement();
        for (SW360SparseLicense sparseLicense : licenses) {
            try {
                Optional<License> license = enrichSparseLicenseFromSW360(sparseLicense);
                if (license.isPresent()) {
                    licenseStatement = appendToLicenseStatement(licenseStatement, license.get());
                }
            } catch (ExecutionException e) {
                LOGGER.error("Exception while getting license details from SW360 for license: " + sparseLicense.getFullName() + "(" + sparseLicense.getShortName() + ")", e);
            }
        }
        artifact.addFact(new ConfiguredLicenseInformation(licenseStatement));
    }

    private void warnAndReport(Artifact artifact, String message) {
        LOGGER.warn(message);
        reporter.add(
                artifact,
                MessageType.PROCESSING_FAILURE,
                message);
    }

    private void logLicenseDifference(List<License> artifactLicenses, List<SW360SparseLicense> releaseLicenses) {
        List<String> artifactLicenseNames = artifactLicenses.stream()
                .map(License::getName)
                .collect(Collectors.toList());
        List<String> releaseLicenseNames = releaseLicenses.stream()
                .map(SW360SparseLicense::getShortName)
                .collect(Collectors.toList());

        LOGGER.info("Artifact: '" + String.join("', '", artifactLicenseNames)
                + "' <-> SW360: '" + String.join("', '", releaseLicenseNames) + "'");
    }

    private void addCPEIdIfAvailable(Artifact artifact, SW360Release release) {
        String cpeId = release.getCpeId();
        if (cpeId != null && cpeId.startsWith("cpe:")) {
            artifact.addFact(new ArtifactCPE(cpeId));
        }
    }


}