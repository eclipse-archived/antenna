/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadRequest;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360AttachmentUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360Visibility;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360ProjectType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SW360MetaDataUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360MetaDataUpdater.class);

    // rest service adapters
    private final SW360ProjectClientAdapter projectClientAdapter;
    private final SW360LicenseClientAdapter licenseClientAdapter;
    private final SW360ReleaseClientAdapter releaseClientAdapter;

    /**
     * Stores a set with the IDs of licenses known to SW360. This set is
     * populated on demand and then cached to speed up further license checks.
     */
    private final AtomicReference<Set<String>> knownSW360LicenseIds;

    public SW360MetaDataUpdater(SW360Connection connection) {
        projectClientAdapter = connection.getProjectAdapter();
        licenseClientAdapter = connection.getLicenseAdapter();
        releaseClientAdapter = connection.getReleaseAdapter();
        knownSW360LicenseIds = new AtomicReference<>();
    }

    public Set<SW360License> getLicenses(Collection<License> licenses) {
        return licenses.stream()
                .filter(this::isLicenseInSW360)
                .map(license -> licenseClientAdapter.getLicenseByName(license.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private boolean isLicenseInSW360(License license) {
        if (getSW360Licenses().contains(license.getId())) {
            LOGGER.debug("License [{}] found in SW360.", license.getId());
            return true;
        }
        LOGGER.debug("License [{}] unknown in SW360.", license.getId());
        return false;
    }

    /**
     * Returns a set with the licenses known to SW360. The set is retrieved
     * once and then cached. Note that an atomic reference is used to make
     * sure that the initialization happens in a thread-safe way.
     *
     * @return the set with licenses known to SW360
     */
    private Set<String> getSW360Licenses() {
        Set<String> licenseIds = knownSW360LicenseIds.get();
        while (licenseIds == null) {
            licenseIds = loadLicensesFromSW360();
            if (!knownSW360LicenseIds.compareAndSet(null, licenseIds)) {
                licenseIds = knownSW360LicenseIds.get();
            }
        }
        return licenseIds;
    }

    /**
     * Queries the licenses known to SW360 and generates a set with their IDs.
     *
     * @return the set with IDs of all known licenses in SW360
     */
    private Set<String> loadLicensesFromSW360() {
        LOGGER.info("Querying existing licenses from SW360.");
        return licenseClientAdapter.getLicenses().stream()
                .map(SW360SparseLicense::getShortName)
                .collect(Collectors.toSet());
    }

    /**
     * Makes sure that a release corresponding to the passed in data object
     * exists in SW360 and returns it. If no matching release is found, a new
     * one is created. Otherwise, based on the {@code updateExisting} flag, the
     * release found in SW360 may or may not be updated.
     *
     * @param sw360ReleaseFromArtifact the release to update or create
     * @param updateExisting           a flag whether the release should be
     *                                 updated if it already exists
     * @param overwriteSW360Data    a flag whether the release derived from
     *                                 the artifact takes precedence when
     *                                 merging with release found in SW360
     * @return the updated or newly created release entity
     */
    public SW360Release getOrCreateRelease(SW360Release sw360ReleaseFromArtifact, boolean updateExisting, boolean overwriteSW360Data) {
        Optional<SW360SparseRelease> optSparseReleaseByIds =
                releaseClientAdapter.getSparseReleaseByExternalIds(sw360ReleaseFromArtifact.getExternalIds());
        Optional<SW360SparseRelease> optSparseRelease = optSparseReleaseByIds.isPresent() ? optSparseReleaseByIds :
                releaseClientAdapter.getSparseReleaseByNameAndVersion(sw360ReleaseFromArtifact.getName(),
                        sw360ReleaseFromArtifact.getVersion());
        Optional<SW360Release> optRelease = optSparseRelease.flatMap(releaseClientAdapter::enrichSparseRelease)
                .map(release -> {
                    if (overwriteSW360Data && updateAllowed(release.getClearingState())) {
                        return release.mergeWith(sw360ReleaseFromArtifact);
                    }
                    return sw360ReleaseFromArtifact.mergeWith(release);
                });

        if (optRelease.isPresent()) {
            SW360Release release = optRelease.get();
            return updateExisting ?
                    releaseClientAdapter.updateRelease(release) : release;
        }
        return releaseClientAdapter.createRelease(sw360ReleaseFromArtifact);
    }

    /**
     * Makes sure that a release corresponding to the passed in data object
     * exists in SW360 and returns it. If no matching release is found, a new
     * one is created. Otherwise, based on the {@code updateExisting} flag, the
     * release found in SW360 may or may not be updated.
     *
     * @param sw360ReleaseFromArtifact the release to update or create
     * @param updateExisting           a flag whether the release should be
     *                                 updated if it already exists
     * @return the updated or newly created release entity
     */
    public SW360Release getOrCreateRelease(SW360Release sw360ReleaseFromArtifact, boolean updateExisting) {
        return getOrCreateRelease(sw360ReleaseFromArtifact, updateExisting, false);
    }

    /**
     * Checks whether a given Optional clearing state string allows for the release to be updated.
     * @param clearingState Optional clearing state that gets checked against
     * @return true if the optional clearing state string are null or empty or of the clearing state allows for updates.
     */
    private boolean updateAllowed(String clearingState) {
        if (clearingState == null || clearingState.isEmpty()) {
            return true;
        } else {
            return ArtifactClearingState.ClearingState.valueOf(clearingState).isUpdateAllowed();
        }
    }

    public void createProject(String projectName, String projectVersion, Collection<SW360Release> releases) {
        Optional<String> projectId =
                projectClientAdapter.getProjectByNameAndVersion(projectName, projectVersion)
                        .map(SW360HalResource::getId);

        String id = projectId.orElseGet(() ->
                projectClientAdapter.createProject(prepareNewProject(projectName, projectVersion)).getId());
        projectClientAdapter.addSW360ReleasesToSW360Project(id, releases);
    }

    /**
     * Uploads multiple attachments to a release. This method goes beyond the
     * default upload functionality provided by the SW360 client library in the
     * following aspects:
     * <ul>
     * <li>Uploads are skipped if the same attachment with the same content is
     * already present.</li>
     * <li>Conflicts are found (i.e. there is already an attachment with this
     * name, but with different content). Based on the {@code force} flag
     * passed in, the existing attachment is either deleted before the upload
     * (if {@code force} is <strong>true</strong>) or a failure is recorded for
     * this attachment ({@code force} == <strong>false</strong>).</li>
     * </ul>
     *
     * @param sw360Release the target of the uploads
     * @param attachments  a map with paths and attachment types to upload
     * @param force        the flag that controls how to deal with conflicts
     * @return a result object with information about successful and failed
     * uploads
     */
    public AttachmentUploadResult<SW360Release> uploadAttachments(SW360Release sw360Release,
                                                                  Map<Path, SW360AttachmentType> attachments,
                                                                  boolean force) {
        Set<String> attachmentsToDelete = new HashSet<>();
        Map<AttachmentUploadRequest.Item, Throwable> conflictFailures = new HashMap<>();
        AttachmentUploadRequest.Builder<SW360Release> builder = AttachmentUploadRequest.builder(sw360Release);
        for (Map.Entry<Path, SW360AttachmentType> e : attachments.entrySet()) {
            String fileName = String.valueOf(e.getKey().getFileName());
            Optional<SW360SparseAttachment> optAttachment = findAttachmentByFileName(sw360Release, fileName);
            boolean shouldUpload = optAttachment.map(attachment ->
                    checkUploadCriteria(e.getKey(), attachment, attachmentsToDelete, conflictFailures, force))
                    .orElse(true);
            if (shouldUpload) {
                builder = builder.addAttachment(e.getKey(), e.getValue());
            }
        }

        deleteConflictingAttachments(sw360Release, attachmentsToDelete);

        AttachmentUploadRequest<SW360Release> uploadRequest = builder.build();
        if (!uploadRequest.getItems().isEmpty()) {
            AttachmentUploadResult<SW360Release> result = releaseClientAdapter.uploadAttachments(uploadRequest);
            LOGGER.debug("Result of attachment upload operation: {}", result);
            if (!result.isSuccess()) {
                LOGGER.error("Failed to upload attachments: {}", result.failedUploads());
            }
            return appendFailures(result, conflictFailures);
        }
        return appendFailures(new AttachmentUploadResult<>(sw360Release), conflictFailures);
    }

    /**
     * Deletes all the source attachments of the given release. This is useful
     * before uploading a new source attachment to avoid clashes with existing
     * ones and to make sure that there is only a single source attachment.
     *
     * @param release the release to be updated
     * @return the updated release
     */
    public SW360Release deleteSourceAttachments(SW360Release release) {
        return deleteAttachments(release, attachment -> attachment.getAttachmentType() == SW360AttachmentType.SOURCE);
    }

    /**
     * Deletes all the attachments of the given release matched by a filter.
     * This method gives full control over the attachments that are removed and
     * which are kept. This is useful for instance to delete all other source
     * attachments than the final approved one.
     *
     * @param release the release to be updated
     * @param filter  a filter to select the attachments to be deleted
     * @return the updated release
     */
    public SW360Release deleteAttachments(SW360Release release, Predicate<SW360SparseAttachment> filter) {
        Set<String> deleteAttachmentIds = release.getEmbedded().getAttachments().stream()
                .filter(filter)
                .map(SW360SparseAttachment::getId)
                .collect(Collectors.toSet());
        return deleteAttachmentIds.isEmpty() ? release :
                releaseClientAdapter.deleteAttachments(release, deleteAttachmentIds);
    }

    /**
     * Calculates the SHA-1 hash for the given local attachment file. This is
     * used to determine whether a modified attachment file needs to be
     * uploaded.
     *
     * @param path the path to the local file
     * @return the SHA-1 hash of this file
     * @throws SW360ClientException if an error occurs
     */
    String calculateAttachmentHash(Path path) {
        return SW360AttachmentUtils.calculateSha1Hash(path);
    }

    /**
     * Checks whether an attachment associated with a release is identical to
     * the local file. If this is the case, the upload of this attachment file
     * can be skipped.
     *
     * @param path       the path to the local attachment file
     * @param attachment the attachment from the current release
     * @return a flag whether the attachment is matched by these properties
     */
    private boolean checkAttachmentContentUpToDate(Path path, SW360SparseAttachment attachment) {
        try {
            return calculateAttachmentHash(path).equals(attachment.getSha1());
        } catch (SW360ClientException e) {
            LOGGER.warn("Could not calculate has for attachment {}.", path, e);
            return false;
        }
    }

    /**
     * Deletes conflicting attachments before an upload operation if there are
     * any. Exceptions are caught and logged; in case of failures, it may be
     * possible that the following attachment uploads might fail.
     *
     * @param sw360Release        the release
     * @param attachmentsToDelete a set with attachment IDs to delete
     */
    private void deleteConflictingAttachments(SW360Release sw360Release, Set<String> attachmentsToDelete) {
        if (!attachmentsToDelete.isEmpty()) {
            LOGGER.debug("Deleting conflicting attachments: {}.", attachmentsToDelete);
            try {
                releaseClientAdapter.deleteAttachments(sw360Release, attachmentsToDelete);
            } catch (Exception e) {
                LOGGER.error("Failed to delete conflicting attachments.", e);
            }
        }
    }

    /**
     * Creates a new {@code SW360Project} entity and sets typical properties
     * before it is created.
     *
     * @param projectName    the project name
     * @param projectVersion the project version
     * @return the new, initialized {@code SW360Project} entity
     */
    private static SW360Project prepareNewProject(String projectName, String projectVersion) {
        SW360Project sw360Project = new SW360Project();
        sw360Project.setName(projectName);
        sw360Project.setVersion(projectVersion);
        sw360Project.setDescription(projectName + " " + projectVersion);
        sw360Project.setProjectType(SW360ProjectType.PRODUCT);
        sw360Project.setVisibility(SW360Visibility.BUISNESSUNIT_AND_MODERATORS);
        return sw360Project;
    }

    /**
     * Checks whether a local attachment file should be uploaded to the server.
     * This function determines whether an upload for this file is necessary or
     * can be skipped. It detects conflicts with an already existing attachment
     * and handles them based on the {@code force} flag: if uploads are forced,
     * the attachment's ID is added to the set of attachments to be deleted;
     * otherwise, a conflict error is reported for this upload.
     *
     * @param path                the path to the local attachment file
     * @param attachment          the attachment assigned to the release
     * @param attachmentsToDelete set with IDs of attachments to delete
     * @param conflictFailures    map to report conflict failures
     * @param force               the flag that controls how to deal with conflicts
     * @return a flag whether this attachment file must be uploaded
     */
    private boolean checkUploadCriteria(Path path, SW360SparseAttachment attachment,
                                        Set<String> attachmentsToDelete,
                                        Map<AttachmentUploadRequest.Item, Throwable> conflictFailures, boolean force) {
        if (checkAttachmentContentUpToDate(path, attachment)) {
            LOGGER.debug("Skipping upload for {} as it already exists.", path);
            return false;
        } else {
            if (force) {
                attachmentsToDelete.add(attachment.getId());
            } else {
                conflictFailures.put(new AttachmentUploadRequest.Item(path, attachment.getAttachmentType()),
                        new IllegalStateException("Conflict detected for " + path +
                                "! An attachment with this name, but different content exists."));
                return false;
            }
        }

        return true;
    }

    /**
     * Searches for an attachment with the given name in the release specified.
     *
     * @param sw360Release the release
     * @param fileName     the name of the attachment in question
     * @return an {@code Optional} with the matched attachment
     */
    private static Optional<SW360SparseAttachment> findAttachmentByFileName(SW360Release sw360Release,
                                                                            String fileName) {
        return sw360Release.getEmbedded().getAttachments().stream()
                .filter(attachment -> fileName.equals(attachment.getFilename()))
                .findFirst();
    }

    /**
     * Appends the given map of additional failures to the given result
     * object.
     *
     * @param orgResult    the original result
     * @param moreFailures a map with failures to add
     * @return the result with the failures appended
     */
    private static AttachmentUploadResult<SW360Release>
    appendFailures(AttachmentUploadResult<SW360Release> orgResult,
                   Map<AttachmentUploadRequest.Item, Throwable> moreFailures) {
        if (moreFailures.isEmpty()) {
            return orgResult;
        }
        moreFailures.putAll(orgResult.failedUploads());
        return AttachmentUploadResult.newResult(orgResult.getTarget(), orgResult.successfulUploads(), moreFailures);
    }
}
