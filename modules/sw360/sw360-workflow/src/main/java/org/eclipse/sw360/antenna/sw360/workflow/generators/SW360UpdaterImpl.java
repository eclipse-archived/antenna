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
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToAttachmentUtils;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SW360UpdaterImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360UpdaterImpl.class);

    private final String projectName;
    private final String projectVersion;
    private final SW360MetaDataUpdater sw360MetaDataUpdater;

    public SW360UpdaterImpl(SW360MetaDataUpdater sw360MetaDataUpdater,
                            String projectName, String projectVersion) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.sw360MetaDataUpdater = sw360MetaDataUpdater;
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
     * @param artifact artifact to be transformed to release
     * @return SW360Release on which artifact and additional information
     * form SW360 instance have been mapped on.
     */
    public SW360Release artifactToReleaseInSW360(Artifact artifact) {
        final SW360Release sw360ReleaseFromArtifact = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);

        return artifactToReleaseInSW360(artifact, sw360ReleaseFromArtifact);
    }

    /**
     * Maps an artifacts licenses and sources onto an SW360Release object and
     * either gets information about it from a SW360 instance or,
     * if it does not exist in the instance yet, creates it.
     * @param artifact artifact to be transformed to release
     * @param release release on which artifact license, sources and
     *                information form sw360 instance will be mapped
     * @return mapped SW360Release
     */
    public SW360Release artifactToReleaseInSW360(Artifact artifact, SW360Release release) {
        Set<String> licenseIds = getSetOfLicenseIds(artifact);
        release.setMainLicenseIds(licenseIds);
        SW360Release sw360ReleaseFinal = sw360MetaDataUpdater.getOrCreateRelease(release);

        sw360ReleaseFinal = handleSources(sw360ReleaseFinal, artifact);

        return sw360ReleaseFinal;
    }

    public SW360Release handleSources(SW360Release release, Artifact artifact) {
        if (sw360MetaDataUpdater.isUploadSources()
                && release.getLinks().getSelf() != null
                && !release.getLinks().getSelf().getHref().isEmpty()) {
            Map<Path, SW360AttachmentType> attachments = ArtifactToAttachmentUtils.getAttachmentsFromArtifact(artifact);
            if (!attachments.isEmpty()) {
                release = sw360MetaDataUpdater.uploadAttachments(release, attachments);
            }
        }
        return release;
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
