/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
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
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SW360EnricherImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SW360EnricherImpl.class);

    private final IProcessingReporter reporter;
    private final SW360MetaDataReceiver connector;
    private final boolean downloadAttachments;
    private final Path downloadPath;

    public SW360EnricherImpl(IProcessingReporter reporter, SW360MetaDataReceiver connector, boolean downloadAttachments, Path downloadPath) {
        this.reporter = reporter;
        this.connector = connector;
        this.downloadAttachments = downloadAttachments;
        this.downloadPath = downloadPath;
    }

    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        for (Artifact artifact : intermediates) {
            Optional<SW360Release> release = connector.findReleaseForArtifact(artifact);
            if (release.isPresent()) {
                mapReleaseToArtifact(release.get(), artifact);
            } else {
                warnAndReport(artifact, "No SW360 release found for artifact.", MessageType.PROCESSING_FAILURE);
            }
        }
        return intermediates;
    }

    private void mapReleaseToArtifact(SW360Release sw360Release, Artifact artifact) {
        ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(sw360Release, artifact);
        updateLicenses(artifact, sw360Release);

        if (downloadAttachments) {
            downloadAttachments(sw360Release, artifact);
        }
    }

    private void downloadAttachments(SW360Release sw360Release, Artifact artifact) {
        Set<SW360SparseAttachment> attachments = sw360Release.getEmbedded().getAttachments();

        attachments.stream()
                .filter(attachment -> attachment.getAttachmentType() == SW360AttachmentType.SOURCE)
                .forEach(attachment -> mapSourceAttachmentOnArtifact(sw360Release, attachment, artifact));
    }

    private void mapSourceAttachmentOnArtifact(SW360Release sw360Release, SW360SparseAttachment attachment, Artifact artifact) {
        Optional<Path> attachmentPath = connector.downloadAttachment(sw360Release, attachment, downloadPath);
        attachmentPath.ifPresent(path -> artifact.addFact(new ArtifactSourceFile(path)));
    }

    private void updateLicenses(Artifact artifact, SW360Release release) {
        List<License> artifactLicenses = artifact.askForAll(ArtifactLicenseInformation.class)
                .stream()
                .map(ArtifactLicenseInformation::get)
                .map(LicenseInformation::getLicenses)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        final SW360ReleaseEmbedded embedded = release.getEmbedded();
        if (embedded == null) {
            LOGGER.debug("No license information available in SW360.");
            return;
        }
        List<SW360SparseLicense> releaseLicenses = new ArrayList<>(embedded.getLicenses());

        if (!artifactLicenses.isEmpty()) {
            if (releaseLicenses.isEmpty()) {
                LOGGER.debug("License information available in antenna but not in SW360.");
            } else {
                updateLicenseInformation(artifactLicenses, releaseLicenses);
            }
        }
    }

    private void updateLicenseInformation(List<License> artifactLicenses, List<SW360SparseLicense> sw360licenses) {
        artifactLicenses.forEach(license -> updateLicense(license, sw360licenses));
    }

    private void updateLicense(License license, List<SW360SparseLicense> sw360licenses) {
        sw360licenses.stream()
                .filter(l -> l.getShortName().equals(license.getId()))
                .findFirst()
                .ifPresent(l -> enrichLicenseWithSW360Data(license, l));
    }

    private License enrichLicenseWithSW360Data(License license, SW360SparseLicense sparseLicense) {
        Optional<License> updatedLicense = connector.getLicenseDetails(sparseLicense)
                .map(licenseDetails -> makeLicenseFromLicenseDetails(license, licenseDetails));
        if (updatedLicense.isPresent()) {
            return updatedLicense.get();
        }

        LOGGER.warn("Could not get details for license {}", sparseLicense.getFullName());
        return license;
    }

    private License makeLicenseFromLicenseDetails(License license, SW360License licenseDetails) {
        license.setCommonName(licenseDetails.getFullName());
        license.setText(licenseDetails.getText());
        return license;
    }

    private void warnAndReport(Artifact artifact, String message, MessageType messageType) {
        LOGGER.debug(message);
        reporter.add(
                artifact,
                messageType,
                message);
    }
}