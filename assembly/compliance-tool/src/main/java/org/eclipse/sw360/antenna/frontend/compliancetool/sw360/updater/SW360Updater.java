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
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState.ClearingState;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadRequest;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToAttachmentUtils;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Configuration property that defines the location of synthetic clearing
     * documents created by the updater. The path can be relative and is then
     * resolved against the base directory.
     */
    public static final String PROP_CLEARING_DOC_FOLDER = "clearingDocDir";

    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Updater.class);

    private final SW360UpdaterImpl updater;
    private final SW360Configuration configuration;
    private final ClearingReportGenerator generator;

    private final Path clearingDocDir;
    private final boolean removeClearedSources;
    private final boolean removeClearingDocs;

    public SW360Updater(SW360UpdaterImpl updater, SW360Configuration configuration,
                        ClearingReportGenerator generator) {
        this.updater = Objects.requireNonNull(updater, "UpdaterImpl must not be null");
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
        this.generator = Objects.requireNonNull(generator, "Clearing report generator must not be null");

        clearingDocDir = configuration.getBaseDir()
                .resolve(configuration.getProperty(PROP_CLEARING_DOC_FOLDER));
        removeClearedSources = configuration.getBooleanConfigValue(PROP_REMOVE_CLEARED_SOURCES);
        removeClearingDocs = configuration.getBooleanConfigValue(PROP_REMOVE_CLEARING_DOCS);
    }

    public void execute() {
        LOGGER.debug("{} has started.", SW360Updater.class.getName());
        Collection<Artifact> artifacts = getArtifactsFromCsvFile(configuration);

        Map<Artifact, SW360ClientException> exceptions = new HashMap<>();
        for (Artifact artifact : artifacts) {
            try {
                uploadReleaseWithClearingDocumentFromArtifact(artifact);
            } catch (SW360ClientException e) {
                exceptions.put(artifact, e);
            }
        }

        LOGGER.info("The SW360Updater was executed with the following configuration:");
        configuration.logConfiguration(LOGGER);
        LOGGER.info("Path for clearing documents: {}", clearingDocDir);

        if (!exceptions.isEmpty()) {
            StringBuilder builder = new StringBuilder("There have been some errors during update:");
            exceptions.forEach((key, value) -> {
                String packageURL = key.getMainCoordinate()
                        .map(coordinate ->
                                coordinate.getPackageURL().toString())
                        .orElse("UNKNOWN");

                builder.append(System.lineSeparator());
                builder.append(String.format("%s: %s", packageURL, value.getMessage()));
            });
            LOGGER.error(builder.toString());
            throw new SW360ClientException(builder.toString());
        }
    }

    /**
     * Upload clearing document in a release as attachment
     *
     * @param artifact the artifact the release comes from
     * @throws SW360ClientException in case of error
     */
    private void uploadReleaseWithClearingDocumentFromArtifact(Artifact artifact) {
        LOGGER.info("Processing {}.", artifact);

        final SW360Release sw360ReleaseFromArtifact = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);
        final String releaseClearingState = sw360ReleaseFromArtifact.getClearingState();

        if (isNotEmptyOrInitialClearingState(releaseClearingState)) {

            if (ClearingState.valueOf(releaseClearingState) == ClearingState.WORK_IN_PROGRESS) {
                updater.artifactToReleaseInSW360(artifact, sw360ReleaseFromArtifact, true);
            } else {
                Path clearingDoc = getOrGenerateClearingDocument(sw360ReleaseFromArtifact, artifact);
                Map<Path, SW360AttachmentType> clearingDocUpload =
                        Collections.singletonMap(clearingDoc, SW360AttachmentType.CLEARING_REPORT);
                AttachmentUploadResult<SW360Release> uploadResult =
                        updater.artifactToReleaseWithUploads(artifact, sw360ReleaseFromArtifact, clearingDocUpload, false);
                SW360Release release = uploadResult.getTarget();

                Set<Path> failedUploads = uploadResult.failedUploads().keySet().stream()
                        .map(AttachmentUploadRequest.Item::getPath)
                        .collect(Collectors.toSet());
                if (removeClearedSources) {
                    removeSourceArtifact(artifact, release, failedUploads);
                }
                if (removeClearingDocs) {
                    removeClearingDocument(clearingDoc, failedUploads);
                }
            }
        }
    }

    private Path getOrGenerateClearingDocument(SW360Release release, Artifact artifact) {
        return artifact.askFor(ArtifactClearingDocument.class).map(ArtifactClearingDocument::get)
                .orElse(generator.createClearingDocument(release, clearingDocDir));
    }

    private static void removeSourceArtifact(Artifact artifact, SW360Release release, Set<Path> failedUploads) {
        ArtifactToAttachmentUtils.getSourceAttachmentFromArtifact(artifact)
                .ifPresent(path -> removeUploadedFile(path, failedUploads, "source attachment of release " +
                        release.getName() + ":" + release.getVersion()));
    }

    private static void removeClearingDocument(Path clearingDoc, Set<Path> failedUploads) {
        removeUploadedFile(clearingDoc, failedUploads, "clearing document");
    }

    /**
     * Removes a file after it has been uploaded. Before deleting the file, it
     * is checked whether the upload actually was successful; otherwise, the
     * file is kept.
     *
     * @param path          the path to the file to be deleted
     * @param failedUploads a set with files that could not be uploaded
     * @param tag           a tag describing the file for log output
     */
    private static void removeUploadedFile(Path path, Set<Path> failedUploads, String tag) {
        if (failedUploads.contains(path)) {
            LOGGER.debug("Skipping removal of {} {} as it could not be uploaded.", tag, path);
        } else {
            LOGGER.debug("Removing {} {}.", tag, path);
            try {
                Files.delete(path);
            } catch (IOException e) {
                LOGGER.error("Could not delete {} {}", tag, path, e);
            }
        }
    }

    private boolean isNotEmptyOrInitialClearingState(final String clearingState) {
        return clearingState != null && !clearingState.isEmpty() &&
                ClearingState.valueOf(clearingState) != ClearingState.INITIAL;
    }
}
