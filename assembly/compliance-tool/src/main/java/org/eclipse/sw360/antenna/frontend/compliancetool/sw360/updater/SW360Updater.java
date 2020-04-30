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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingDocument;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils.getArtifactsFromCsvFile;

public class SW360Updater {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Updater.class);

    private final SW360UpdaterImpl updater;
    private final SW360Configuration configuration;
    private final ClearingReportGenerator generator;

    public SW360Updater(SW360UpdaterImpl updater, SW360Configuration configuration,
                        ClearingReportGenerator generator) {
        this.updater = Objects.requireNonNull(updater, "UpdaterImpl must not be null");
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
        this.generator = Objects.requireNonNull(generator, "Clearing report generator must not be null");
    }

    public void execute() {
        Collection<Artifact> artifacts = getArtifactsFromCsvFile(configuration.getProperties());

        artifacts.forEach(this::uploadReleaseWithClearingDocumentFromArtifact);


        LOGGER.info("The SW360Exporter was executed from the base directory: {} " +
                        "with the csv file taken from the path: {}, " +
                        "the clearing reports taken or temporarily created in: {}/{} " +
                        "and the source files taken from the folder: {}.",
                configuration.getBaseDir().toAbsolutePath(),
                configuration.getBaseDir().toAbsolutePath(),
                configuration.getCsvFileName(),
                configuration.getTargetDir().toAbsolutePath(),
                configuration.getSourcesPath().toAbsolutePath());
    }

    private void uploadReleaseWithClearingDocumentFromArtifact(Artifact artifact) {
        LOGGER.info("Processing {}.", artifact);
        try {
            SW360Release release = updater.artifactToReleaseInSW360(artifact);
            SW360ReleaseClientAdapter releaseClientAdapter = configuration.getConnection().getReleaseAdapter();

            if (release.getClearingState() != null &&
                    !release.getClearingState().isEmpty() &&
                    ArtifactClearingState.ClearingState.valueOf(release.getClearingState()) != ArtifactClearingState.ClearingState.INITIAL) {
                Map<Path, SW360AttachmentType> attachmentPathMap =
                        Collections.singletonMap(getOrGenerateClearingDocument(release, artifact),
                                SW360AttachmentType.CLEARING_REPORT);
                releaseClientAdapter.uploadAttachments(release, attachmentPathMap);
            }
        } catch (SW360ClientException e) {
            LOGGER.error("Failed to process artifact {}.", artifact, e);
        }
    }

    private Path getOrGenerateClearingDocument(SW360Release release, Artifact artifact) {
        return artifact.askFor(ArtifactClearingDocument.class).map(ArtifactClearingDocument::get)
                .orElse(generator.createClearingDocument(release, configuration.getTargetDir()));
    }
}
