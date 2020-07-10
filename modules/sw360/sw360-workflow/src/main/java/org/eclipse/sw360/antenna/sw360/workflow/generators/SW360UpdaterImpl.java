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
package org.eclipse.sw360.antenna.sw360.workflow.generators;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToAttachmentUtils;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * A class implementing the logic to update releases in SW360 with new meta
 * data.
 * </p>
 * <p>
 * The interaction with the SW360 server is done via a
 * {@link SW360MetaDataUpdater} object that is passed to the constructor. The
 * update process can be configured using a number of flags, e.g. to control
 * whether updates on release properties should be done or source attachments
 * should be uploaded.
 * </p>
 */
public class SW360UpdaterImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360UpdaterImpl.class);

    private final String projectName;
    private final String projectVersion;
    private final SW360MetaDataUpdater sw360MetaDataUpdater;
    private final boolean updateReleases;
    private final boolean uploadSources;
    private final boolean deleteObsoleteSourceAttachments;

    public SW360UpdaterImpl(SW360MetaDataUpdater sw360MetaDataUpdater,
                            String projectName, String projectVersion, boolean updateReleases,
                            boolean uploadSources, boolean deleteObsoleteSourceAttachments) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.sw360MetaDataUpdater = sw360MetaDataUpdater;
        this.updateReleases = updateReleases;
        this.uploadSources = uploadSources;
        this.deleteObsoleteSourceAttachments = deleteObsoleteSourceAttachments;
    }

    /**
     * Returns a flag whether properties of releases should be updated. New
     * releases are always created in SW360 if necessary. This property
     * controls how to deal with existing releases: if set to
     * <strong>true</strong>, they are patched with updated meta data.
     *
     * @return a flag whether releases should be updated
     */
    public boolean isUpdateReleases() {
        return updateReleases;
    }

    /**
     * Returns a flag whether source attachments should be uploaded to SW360.
     * If set to <strong>true</strong>, a defined and existing source
     * attachment file is upload for the current release if such an attachment
     * does not yet exist for this release.
     *
     * @return a flag whether sources should be uploaded
     */
    public boolean isUploadSources() {
        return uploadSources;
    }

    /**
     * Returns a flag whether source attachments should be deleted that are no
     * longer in use. This flag is evaluated only if {@link #isUploadSources()}
     * returns <strong>true</strong>. In this case, it controls whether all
     * source attachments are deleted before another one is uploaded. Antenna
     * assumes that there is a single source file only while SW360 supports
     * multiple source attachments. It is, however, not possible to override an
     * existing attachment in SW360 with updated content (e.g. to replace an
     * accidentally uploaded source attachment). Setting this property to
     * <strong>true</strong> addresses these problems: It ensures that there is
     * always only a single source attachment, and it supports updates of
     * attachment files by deleting them first.
     *
     * @return a flag whether obsolete source attachments should be deleted
     */
    public boolean isDeleteObsoleteSourceAttachments() {
        return deleteObsoleteSourceAttachments;
    }

    public Map<String, IAttachable> produce(Collection<Artifact> intermediates) {
        List<SW360Release> releases = new ArrayList<>();
        for (Artifact artifact : intermediates) {
            try {
                SW360Release sw360ReleaseFinal = artifactToReleaseInSW360(artifact);
                releases.add(sw360ReleaseFinal);
            } catch (ExecutionException e) {
                LOGGER.error("Release will not be created in SW360. Reason: {}", e.getMessage());
                LOGGER.debug("Error: ", e);
            }
        }

        sw360MetaDataUpdater.createProject(projectName, projectVersion, releases);
        return Collections.emptyMap();
    }

    /**
     * Maps an artifact onto an SW360Release object and either gets
     * information about it from a SW360 instance or, if it does not
     * exist in the instance yet, creates it
     *
     * @param artifact artifact to be transformed to release
     * @return SW360Release on which artifact and additional information
     * form SW360 instance have been mapped on.
     */
    public SW360Release artifactToReleaseInSW360(Artifact artifact) {
        final SW360Release sw360ReleaseFromArtifact = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);

        return artifactToReleaseInSW360(artifact, sw360ReleaseFromArtifact);
    }

    /**
     * Maps an artifact's licenses and sources onto an SW360Release object and
     * either gets information about it from a SW360 instance or,
     * if it does not exist in the instance yet, creates it.
     *
     * @param artifact artifact to be transformed to release
     * @param release  release on which artifact license, sources and
     *                 information form sw360 instance will be mapped
     * @return mapped SW360Release
     */
    public SW360Release artifactToReleaseInSW360(Artifact artifact, SW360Release release) {
        Set<String> licenseIds = getSetOfLicenseIds(artifact);
        release.setMainLicenseIds(licenseIds);
        SW360Release sw360ReleaseFinal = sw360MetaDataUpdater.getOrCreateRelease(release, isUpdateReleases());

        sw360ReleaseFinal = uploadSourceAndAttachments(sw360ReleaseFinal, artifact, Collections.emptyMap()).getTarget();

        return sw360ReleaseFinal;
    }

    /**
     * Updates a release with properties from the given artifact. Makes sure
     * that the release exists in SW360 (it is created now if necessary). It is
     * possible to upload additional files, e.g. a clearing report.
     *
     * @param artifact artifact to be transformed to release
     * @param release  release on which artifact license, sources and
     *                 information form sw360 instance will be mapped
     * @param uploads  a map with additional files to be uploaded
     * @return an object with the result of the upload operation
     */
    public AttachmentUploadResult<SW360Release> artifactToReleaseWithUploads(Artifact artifact, SW360Release release,
                                                                             Map<Path, SW360AttachmentType> uploads) {
        Set<String> licenseIds = getSetOfLicenseIds(artifact);
        release.setMainLicenseIds(licenseIds);
        SW360Release sw360ReleaseFinal = sw360MetaDataUpdater.getOrCreateRelease(release, isUpdateReleases());

        return uploadSourceAndAttachments(sw360ReleaseFinal, artifact, uploads);
    }

    /**
     * Uploads the source artifact for the given release if it is available and
     * if uploads are enabled plus any additional attachments. If configured,
     * all other source attachments are deleted for this release.
     *
     * @param release  the release affected by the update
     * @param artifact the corresponding {@code Artifact}
     * @param uploads  a map with additional files to be uploadd
     * @return an object with the result of the upload operation
     */
    private AttachmentUploadResult<SW360Release> uploadSourceAndAttachments(SW360Release release, Artifact artifact,
                                                                            Map<Path, SW360AttachmentType> uploads) {
        Optional<Path> optSrcPath = ArtifactToAttachmentUtils.getSourceAttachmentFromArtifact(artifact);
        String srcFileName = optSrcPath.map(path -> path.getFileName().toString()).orElse(null);
        SW360Release srcDeletedRelease = deleteSourceAttachments(release, srcFileName);

        Map<Path, SW360AttachmentType> allUploads = new HashMap<>(uploads);
        if (isUploadSources() && release.getLinks().getSelf() != null
                && !release.getLinks().getSelf().getHref().isEmpty()) {
            optSrcPath.ifPresent(srcPath -> allUploads.put(srcPath, SW360AttachmentType.SOURCE));
        }

        if (!allUploads.isEmpty()) {
            return sw360MetaDataUpdater.uploadAttachments(srcDeletedRelease, allUploads,
                    isDeleteObsoleteSourceAttachments());
        }

        return new AttachmentUploadResult<>(srcDeletedRelease);
    }

    /**
     * Deletes all source attachments of the given release and returns the
     * updated release entity. As the deletion is not a crucial operation,
     * exceptions are just logged. (It may be the case, that the following
     * attachment upload fails because of that failure.)
     *
     * @param release     the release affected by the update
     * @param srcFileName the name of the source attachment (or null)
     * @return the updated release
     */
    private SW360Release deleteSourceAttachments(SW360Release release, String srcFileName) {
        try {
            return isDeleteObsoleteSourceAttachments() ?
                    sw360MetaDataUpdater.deleteAttachments(release, deleteAttachmentsPredicate(srcFileName)) :
                    release;
        } catch (SW360ClientException e) {
            LOGGER.warn("Could not delete source attachments for release {}:{}",
                    release.getName(), release.getVersion(), e);
            return release;
        }
    }

    /**
     * Obtains the predicate for deleting attachments. If the current release
     * has a source attachment, this attachment is excluded from the delete
     * operation. (It may be deleted later if a conflict is detected, but
     * otherwise not.)
     *
     * @param srcFileName the name of the source attachment (or null)
     * @return the predicate to select the attachments to be deleted
     */
    private Predicate<SW360SparseAttachment> deleteAttachmentsPredicate(String srcFileName) {
        Predicate<SW360SparseAttachment> sourceTypePredicate = attachment ->
                attachment.getAttachmentType() == SW360AttachmentType.SOURCE;
        return srcFileName == null ? sourceTypePredicate :
                sourceTypePredicate.and(attachment -> !srcFileName.equals(attachment.getFilename()));
    }

    private Set<String> getSetOfLicenseIds(Artifact artifact) {
        Collection<License> availableLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();

        Set<SW360License> detectedLicenses = sw360MetaDataUpdater.getLicenses(availableLicenses);
        Set<String> licenseIds = Collections.emptySet();
        if (detectedLicenses.size() == availableLicenses.size()) {
            licenseIds = detectedLicenses.stream()
                    .map(SW360License::getShortName)
                    .collect(Collectors.toSet());
        }
        return licenseIds;
    }
}
