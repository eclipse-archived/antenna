/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToComponentUtils;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

public class SW360MetaDataReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360MetaDataReceiver.class);

    // rest service adapters
    private SW360ComponentClientAdapter componentClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;
    private SW360LicenseClientAdapter licenseClientAdapter;

    private SW360ConnectionConfiguration sw360ConnectionConfiguration;

    public SW360MetaDataReceiver(SW360ConnectionConfiguration sw360ConnectionConfiguration) {
        componentClientAdapter = sw360ConnectionConfiguration.getSW360ComponentClientAdapter();
        releaseClientAdapter = sw360ConnectionConfiguration.getSW360ReleaseClientAdapter();
        licenseClientAdapter = sw360ConnectionConfiguration.getSW360LicenseClientAdapter();
        this.sw360ConnectionConfiguration = sw360ConnectionConfiguration;
    }

    public Optional<SW360Release> findReleaseForArtifact(Artifact artifact) {
        HttpHeaders headers = sw360ConnectionConfiguration.getHttpHeaders();
        Optional<SW360Component> component = getComponentByArtifact(artifact, headers);
        if (component.isPresent()) {

            String releaseVersionOfArtifact = ArtifactToReleaseUtils.createSW360ReleaseVersion(artifact);
            return releaseClientAdapter.getReleaseByVersion(component.get(), releaseVersionOfArtifact, headers);
        }
        return Optional.empty();
    }

    public Optional<SW360License> getLicenseDetails(SW360SparseLicense sparseLicense) {
        HttpHeaders headers = sw360ConnectionConfiguration.getHttpHeaders();
        return licenseClientAdapter.getLicenseDetails(sparseLicense, headers);
    }

    public Optional<Path> downloadAttachment(SW360Release release, SW360SparseAttachment attachment, Path downloadPath) {
        HttpHeaders header = sw360ConnectionConfiguration.getHttpHeaders();
        header.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        return releaseClientAdapter.downloadAttachment(release, attachment, downloadPath, header);
    }

    public Optional<SW360Component> getComponentByArtifact(Artifact artifact, HttpHeaders header) {
        try {
            String componentName = ArtifactToComponentUtils.createComponentName(artifact);
            return componentClientAdapter.getComponentByName(componentName, header);
        } catch (ExecutionException e) {
            LOGGER.debug("No component found for {}. Reason: {}", artifact.prettyPrint(), e.getMessage());
            LOGGER.debug("Error: ", e);
            return Optional.empty();
        }
    }

}
