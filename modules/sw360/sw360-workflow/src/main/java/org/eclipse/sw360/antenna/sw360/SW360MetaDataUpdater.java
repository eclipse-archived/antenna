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
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.api.SW360Connection;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SW360MetaDataUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360MetaDataUpdater.class);

    // rest service adapters
    private SW360ProjectClientAdapter projectClientAdapter;
    private SW360LicenseClientAdapter licenseClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;

    private final boolean updateReleases;
    private final boolean uploadSources;

    public SW360MetaDataUpdater(SW360Connection connection, boolean updateReleases, boolean uploadSources) {
        projectClientAdapter = connection.getProjectAdapter();
        licenseClientAdapter = connection.getLicenseAdapter();
        releaseClientAdapter = connection.getReleaseAdapter();
        this.updateReleases = updateReleases;
        this.uploadSources = uploadSources;
    }

    public Set<SW360License> getLicenses(List<License> licenses) {
        return licenses.stream()
                .filter(this::isLicenseInSW360)
                .map(license -> licenseClientAdapter.getSW360LicenseByAntennaLicense(license.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private boolean isLicenseInSW360(License license) {
        if (licenseClientAdapter.isLicenseOfArtifactAvailable(license.getId(), header)) {
            LOGGER.debug("License [{}] found in SW360.", license.getId());
            return true;
        }
        LOGGER.debug("License [{}] unknown in SW360.", license.getId());
        return false;
    }

    public SW360Release getOrCreateRelease(SW360Release sw360ReleaseFromArtifact) {
        return releaseClientAdapter.getOrCreateRelease(sw360ReleaseFromArtifact, updateReleases);
    }

    public void createProject(String projectName, String projectVersion, Collection<SW360Release> releases) {
        Optional<String> projectId = projectClientAdapter.getProjectIdByNameAndVersion(projectName, projectVersion);

        String id;
        if (projectId.isPresent()) {
            // TODO: Needs endpoint on sw360 to update project on sw360
            LOGGER.debug("Could not update project {}, because the endpoint is not available.", projectId.get());
            id = projectId.get();
        } else {
            id = projectClientAdapter.addProject(projectName, projectVersion);
        }
        projectClientAdapter.addSW360ReleasesToSW360Project(id, releases);
    }

    public boolean isUploadSources() {
        return uploadSources;
    }

    public SW360Release uploadAttachments(SW360Release sw360Release, Map<Path, SW360AttachmentType> attachments) {
        return releaseClientAdapter.uploadAttachments(sw360Release, attachments);
    }
}
