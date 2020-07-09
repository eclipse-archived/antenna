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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SW360MetaDataUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360MetaDataUpdater.class);

    // rest service adapters
    private final SW360ProjectClientAdapter projectClientAdapter;
    private final SW360LicenseClientAdapter licenseClientAdapter;
    private final SW360ReleaseClientAdapter releaseClientAdapter;

    private final boolean updateReleases;
    private final boolean uploadSources;

    /**
     * Stores a set with the IDs of licenses known to SW360. This set is
     * populated on demand and then cached to speed up further license checks.
     */
    private final AtomicReference<Set<String>> knownSW360LicenseIds;

    public SW360MetaDataUpdater(SW360Connection connection, boolean updateReleases, boolean uploadSources) {
        projectClientAdapter = connection.getProjectAdapter();
        licenseClientAdapter = connection.getLicenseAdapter();
        releaseClientAdapter = connection.getReleaseAdapter();
        knownSW360LicenseIds = new AtomicReference<>();
        this.updateReleases = updateReleases;
        this.uploadSources = uploadSources;
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

    public SW360Release getOrCreateRelease(SW360Release sw360ReleaseFromArtifact) {
        Optional<SW360SparseRelease> optSparseReleaseByIds =
                releaseClientAdapter.getSparseReleaseByExternalIds(sw360ReleaseFromArtifact.getExternalIds());
        Optional<SW360SparseRelease> optSparseRelease = optSparseReleaseByIds.isPresent() ? optSparseReleaseByIds :
                releaseClientAdapter.getSparseReleaseByNameAndVersion(sw360ReleaseFromArtifact.getName(),
                        sw360ReleaseFromArtifact.getVersion());
        Optional<SW360Release> optRelease = optSparseRelease.flatMap(releaseClientAdapter::enrichSparseRelease)
                .map(sw360ReleaseFromArtifact::mergeWith);

        if (optRelease.isPresent()) {
            SW360Release release = optRelease.get();
            return updateReleases ?
                    releaseClientAdapter.updateRelease(release) : release;
        }
        return releaseClientAdapter.createRelease(sw360ReleaseFromArtifact);
    }

    public void createProject(String projectName, String projectVersion, Collection<SW360Release> releases) {
        Optional<String> projectId =
                projectClientAdapter.getProjectByNameAndVersion(projectName, projectVersion)
                        .map(SW360HalResource::getId);

        String id = projectId.orElseGet(() ->
                projectClientAdapter.createProject(prepareNewProject(projectName, projectVersion)).getId());
        projectClientAdapter.addSW360ReleasesToSW360Project(id, releases);
    }

    public boolean isUploadSources() {
        return uploadSources;
    }

    public SW360Release uploadAttachments(SW360Release sw360Release, Map<Path, SW360AttachmentType> attachments) {
        AttachmentUploadRequest.Builder<SW360Release> builder = AttachmentUploadRequest.builder(sw360Release);
        for (Map.Entry<Path, SW360AttachmentType> e : attachments.entrySet()) {
            if (needUpload(sw360Release, e.getKey())) {
                builder = builder.addAttachment(e.getKey(), e.getValue());
            }
        }

        AttachmentUploadRequest<SW360Release> uploadRequest = builder.build();
        if (!uploadRequest.getItems().isEmpty()) {
            AttachmentUploadResult<SW360Release> result = releaseClientAdapter.uploadAttachments(uploadRequest);
            LOGGER.debug("Result of attachment upload operation: {}", result);
            if (!result.isSuccess()) {
                LOGGER.error("Failed to upload attachments: {}", result.failedUploads());
            }
            return result.getTarget();
        }
        return sw360Release;
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
     * Checks whether an attachment upload is necessary for the given
     * attachment file. This is the case if no attachment with this file name
     * and type and identical content exists.
     * TODO Currently SW360 has strict checks regarding duplicate attachments;
     * for instance, it is not possible to upload an attachment with an
     * existing file name, even if the content has changed. So if this method
     * returns true, an upload may still be rejected by SW360. It is still open
     * how to handle this in the best way.
     *
     * @param release the release affected
     * @param path    the path to the local attachment file
     * @return a flag whether this attachment file must be uploaded
     */
    private boolean needUpload(SW360Release release, Path path) {
        Optional<SW360SparseAttachment> optAttachment = release.getEmbedded().getAttachments().stream()
                .filter(attachment -> attachmentMatches(path, attachment))
                .findFirst();
        return !optAttachment.isPresent();
    }

    /**
     * Checks whether an attachment associated with a release matches the given
     * properties. If there is an exact match, the upload of this attachment
     * file can be skipped.
     *
     * @param path       the path to the local attachment file
     * @param attachment the attachment from the current release
     * @return a flag whether the attachment is matched by these properties
     */
    private boolean attachmentMatches(Path path, SW360SparseAttachment attachment) {
        try {
            return calculateAttachmentHash(path).equals(attachment.getSha1());
        } catch (SW360ClientException e) {
            LOGGER.warn("Could not calculate has for attachment {}.", path, e);
            return false;
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
}
