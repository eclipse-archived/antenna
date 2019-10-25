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

import com.here.ort.spdx.SpdxException;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.util.LicenseSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

                addSourceUrlIfAvailable(artifact, sw360Release);
                addCPEIdIfAvailable(artifact, sw360Release);
                addClearingStateIfAvailable(artifact, sw360Release);
                mapExternalIdsOnSW360Release(sw360Release, artifact);
                updateLicenses(artifact, sw360Release);
            } else {
                warnAndReport(artifact, "No SW360 release found for artifact.", MessageType.PROCESSING_FAILURE);
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
        artifact.addFact(mapCoordinates(sw360Release));


        addLicenseFact(Optional.ofNullable(sw360Release.getDeclaredLicense()), artifact, DeclaredLicenseInformation::new, artifact.askFor(DeclaredLicenseInformation.class).isPresent());
        addLicenseFact(Optional.ofNullable(sw360Release.getObservedLicense()), artifact, ObservedLicenseInformation::new, artifact.askFor(ObservedLicenseInformation.class).isPresent());
        addLicenseFact(Optional.ofNullable(sw360Release.getOverriddenLicense()), artifact, OverriddenLicenseInformation::new, artifact.askFor(OverriddenLicenseInformation.class).isPresent());

        Optional.ofNullable(sw360Release.getReleaseTagUrl())
                .map(ArtifactReleaseTagURL::new)
                .ifPresent(artifact::addFact);

        try {
            Optional.ofNullable(sw360Release.getSoftwareHeritageId())
                    .map(ArtifactSoftwareHeritageID.Builder::new)
                    .map(ArtifactSoftwareHeritageID.Builder::build)
                    .ifPresent(artifact::addFact);
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.getMessage());
        }
        sw360Release.getHashes().stream()
                .map(hash -> new ArtifactFilename(null, hash))
                .forEach(artifact::addFact);
        Optional.ofNullable(sw360Release.getChangeStatus())
                .map(ArtifactChangeStatus.ChangeStatus::valueOf)
                .map(ArtifactChangeStatus::new)
                .ifPresent(artifact::addFact);
        Optional.ofNullable(sw360Release.getCopyrights())
                .map(CopyrightStatement::new)
                .ifPresent(artifact::addFact);
    }

    private void addLicenseFact(Optional<String> licenseRawData, Artifact artifact, Function<LicenseInformation, ArtifactLicenseInformation> licenseCreator, boolean isAlreadyPresent) {
        licenseRawData.map(this::parseSpdxExpression)
                .map(licenseCreator)
                .ifPresent(expression -> addFactAndLogWarning(artifact, isAlreadyPresent, expression));
    }

    private void addFactAndLogWarning(Artifact artifact, boolean isAlreadyPresent, ArtifactLicenseInformation expression) {
        if (isAlreadyPresent) {
            warnAndReport(artifact,
                    "License information of type " + expression.getClass().getSimpleName() + " found in SW360. Overwriting existing information in artifact.",
                    MessageType.OVERRIDE_ARTIFACT_ATTRIBUTES);
        }
        artifact.addFact(expression);
    }

    private LicenseInformation parseSpdxExpression(String expression) {
        try {
            return LicenseSupport.fromSPDXExpression(expression);
        } catch (SpdxException e) {
            License unparsableExpression = new License();
            unparsableExpression.setName(expression);
            return unparsableExpression;
        }
    }

    private ArtifactCoordinates mapCoordinates(SW360Release sw360Release) {
        final Map<String, String> coordinates = sw360Release.getCoordinates();
        return new ArtifactCoordinates(new HashSet<>(coordinates.values()));
    }

    private void addSourceUrlIfAvailable(Artifact artifact, SW360Release release) {
        if (release.getDownloadurl() != null && !release.getDownloadurl().isEmpty()) {
            artifact.addFact(new ArtifactSourceUrl(release.getDownloadurl()));
        }
    }

    private void updateLicenses(Artifact artifact, SW360Release release) {
        List<License> artifactLicenses = artifact.askForAll(ArtifactLicenseInformation.class)
                .stream()
                .map(ArtifactLicenseInformation::get)
                .map(LicenseInformation::getLicenses)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        final SW360ReleaseEmbedded embedded = release.get_Embedded();
        if (embedded == null) {
            LOGGER.info("No license information available in SW360.");
            return;
        }
        List<SW360SparseLicense> releaseLicenses = new ArrayList<>(embedded.getLicenses());

        if (!artifactLicenses.isEmpty()) {
            if (releaseLicenses.isEmpty()) {
                LOGGER.info("License information available in antenna but not in SW360.");
            } else {
                updateLicenseInformation(artifactLicenses, releaseLicenses);
            }
        }
    }

    private License makeLicenseFromLicenseDetails(License license, SW360License licenseDetails) {
        license.setLongName(licenseDetails.getFullName());
        license.setText(licenseDetails.getText());
        return license;
    }

    private License enrichLicenseWithSW360Data(License license, SW360SparseLicense sparseLicense) {
        Optional<License> updatedLicense = connector.getLicenseDetails(sparseLicense)
                .map(licenseDetails -> makeLicenseFromLicenseDetails(license, licenseDetails));
        if (updatedLicense.isPresent()) {
            return updatedLicense.get();
        }

        LOGGER.info("Could not get details for license " + sparseLicense.getFullName());
        return license;
    }

    private void updateLicenseInformation(List<License> artifactLicenses, List<SW360SparseLicense> sw360licenses) {
        artifactLicenses.forEach(license -> updateLicense(license, sw360licenses));
    }

    private void updateLicense(License license, List<SW360SparseLicense> sw360licenses) {
        sw360licenses.stream()
                .filter(l -> l.getShortName().equals(license.getName()))
                .findFirst()
                .ifPresent(l -> enrichLicenseWithSW360Data(license, l));
    }

    private void warnAndReport(Artifact artifact, String message, MessageType messageType) {
        LOGGER.warn(message);
        reporter.add(
                artifact,
                messageType,
                message);
    }

    private void addCPEIdIfAvailable(Artifact artifact, SW360Release release) {
        String cpeId = release.getCpeId();
        if (cpeId != null && cpeId.startsWith("cpe:")) {
            artifact.addFact(new ArtifactCPE(cpeId));
        }
    }


}