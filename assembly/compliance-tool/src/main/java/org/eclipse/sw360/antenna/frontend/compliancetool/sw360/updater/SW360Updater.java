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
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadRequest;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToAttachmentUtils;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils.getArtifactsFromCsvFile;

public class SW360Updater {
    /**
     * Configuration property that controls whether the source attachments of a
     * release should be removed after it has been cleared. If set to
     * <strong>true</strong> (the default is <strong>false</strong>), the
     * source folder gets automatically cleaned up of source files that are no
     * longer relevant for the compliance workflow.
     */
    public static final String PROP_REMOVE_CLEARED_SOURCES = "removeClearedSources";

    /**
     * Configuration property that controls whether synthetic clearing documents
     * should be removed after they have been uploaded.
     */
    public static final String PROP_REMOVE_CLEARING_DOCS = "removeClearingDocuments";

    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Updater.class);

    private final SW360UpdaterImpl updater;
    private final SW360Configuration configuration;
    private final ClearingReportGenerator generator;

    private final boolean removeClearedSources;
    private final boolean removeClearingDocs;

    public SW360Updater(SW360UpdaterImpl updater, SW360Configuration configuration,
                        ClearingReportGenerator generator) {
        this.updater = Objects.requireNonNull(updater, "UpdaterImpl must not be null");
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
        this.generator = Objects.requireNonNull(generator, "Clearing report generator must not be null");

        removeClearedSources = Boolean.parseBoolean(configuration.getProperties().get(PROP_REMOVE_CLEARED_SOURCES));
        removeClearingDocs = Boolean.parseBoolean(configuration.getProperties().get(PROP_REMOVE_CLEARING_DOCS));
    }

    public void execute() {
        LOGGER.debug("{} has started.", SW360Updater.class.getName());
        Collection<Artifact> artifacts = getArtifactsFromCsvFile(configuration.getProperties(), configuration.getCsvFilePath());

        artifacts.forEach(this::uploadReleaseWithClearingDocumentFromArtifact);


        LOGGER.info("The SW360Exporter was executed from the base directory: {} " +
                        "with the csv file taken from the path: {}, " +
                        "the clearing reports taken or temporarily created in: {} " +
                        "and the source files taken from the folder: {}.",
                configuration.getBaseDir().toAbsolutePath(),
                configuration.getCsvFilePath().toAbsolutePath(),
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
                Path clearingDoc = getOrGenerateClearingDocument(release, artifact);
                AttachmentUploadRequest<SW360Release> uploadRequest = AttachmentUploadRequest.builder(release)
                        .addAttachment(clearingDoc,
                                SW360AttachmentType.CLEARING_REPORT)
                        .build();
                releaseClientAdapter.uploadAttachments(uploadRequest);

                if (removeClearedSources) {
                    removeSourceArtifacts(artifact, release);
                }
                if (removeClearingDocs) {
                    removeClearingDocument(clearingDoc);
                }
            }
        } catch (SW360ClientException e) {
            LOGGER.error("Failed to process artifact {}.", artifact, e);
        }
    }

    private Path getOrGenerateClearingDocument(SW360Release release, Artifact artifact) {
        return artifact.askFor(ArtifactClearingDocument.class).map(ArtifactClearingDocument::get)
                .orElse(generator.createClearingDocument(release, configuration.getTargetDir()));
    }

    private static void removeSourceArtifacts(Artifact artifact, SW360Release release) {
        ArtifactToAttachmentUtils.getAttachmentsFromArtifact(artifact).keySet()
                .forEach(path -> {
                    LOGGER.info("Removing source attachment {} of release {}:{}.", path,
                            release.getName(), release.getVersion());
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        LOGGER.error("Could not remove source attachment {} of release {}:{}", path,
                                release.getName(), release.getVersion());
                    }
                });
    }

    private static void removeClearingDocument(Path clearingDoc) {
        LOGGER.debug("Removing clearing document {}.", clearingDoc);
        try {
            Files.delete(clearingDoc);
        } catch (IOException e) {
            LOGGER.error("Could not delete clearing document {}", clearingDoc, e);
        }
    }
}
