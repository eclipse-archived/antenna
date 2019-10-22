/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.rest.SW360ReleaseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;
import org.eclipse.sw360.antenna.sw360.utils.SW360ReleaseAdapterUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SW360ReleaseClientAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360ReleaseClientAdapter.class);

    private final SW360ReleaseClient releaseClient;
    private final SW360ComponentClientAdapter sw360ComponentClientAdapter;

    public SW360ReleaseClientAdapter(String restUrl, ProxySettings proxySettings) {
        this.releaseClient = new SW360ReleaseClient(restUrl, proxySettings);
        sw360ComponentClientAdapter = new SW360ComponentClientAdapter(restUrl, proxySettings);
    }

    public SW360Release getOrCreateRelease(SW360Release sw360ReleaseFromArtifact, HttpHeaders header, boolean uploadSources, boolean updateReleases) {
        // NOTE: this code does now always merge with the SW360Release used for querying
        return getRelease(sw360ReleaseFromArtifact, header)
                .map(sw360ReleaseFromArtifact::mergeWith)
                .map(sw360Release -> {
                    if(updateReleases) {
                        return releaseClient.patchRelease(sw360Release, header);
                    } else {
                        return sw360Release;
                    }
                })
                .orElseGet(() -> createRelease(sw360ReleaseFromArtifact, uploadSources, header));
    }

    /*
     * Create a release in SW360
     */
    public SW360Release createRelease(SW360Release releaseFromArtifact, boolean uploadSource, HttpHeaders header) {
        if (! SW360ReleaseAdapterUtils.isValidRelease(releaseFromArtifact)) {
            throw new ExecutionException("Can not write invalid release for " + releaseFromArtifact.getName() + "-" + releaseFromArtifact.getVersion());
        }
        if (releaseFromArtifact.getReleaseId() != null) {
            throw new ExecutionException("Can not write release which already has the id " + releaseFromArtifact.getReleaseId());
        }

        if (releaseFromArtifact.getComponentId() == null) {
            final SW360Component componentFromRelease = SW360ComponentAdapterUtils.createFromRelease(releaseFromArtifact);
            final SW360Component componentFromSW360 = sw360ComponentClientAdapter.getOrCreateComponent(componentFromRelease, header);
            if (componentFromSW360.get_Embedded().getReleases().stream()
                    .map(SW360SparseRelease::getVersion)
                    .anyMatch(releaseFromArtifact.getVersion()::equals)) {
                throw new ExecutionException("The release already exists in the found component");
            }

            releaseFromArtifact.setComponentId(componentFromSW360.getComponentId());
        }

        final SW360Release releaseFromSW360 = releaseClient.createRelease(releaseFromArtifact, header);

        final Optional<Path> sourceFile = releaseFromArtifact.getSourceFile();
        if (uploadSource && sourceFile.isPresent()) {
            return releaseClient.uploadAndAttachAttachment(releaseFromSW360, sourceFile.get(), "SOURCE", header);
        } else {
            return releaseFromSW360;
        }
    }

    public SW360Release getReleaseById(String releaseId, HttpHeaders header) {
        return releaseClient.getRelease(releaseId, header);
    }

    public SW360Release enrichSparseRelease(SW360SparseRelease sparseRelease, HttpHeaders header) {
        return getReleaseById(sparseRelease.getReleaseId(), header);
    }

    public Optional<SW360SparseRelease> getSparseRelease(SW360Release sw360ReleaseFromArtifact, HttpHeaders header) {
        final Optional<SW360SparseRelease> releaseByExternalId = getReleaseByExternalIds(sw360ReleaseFromArtifact.getExternalIds(), header);
        if (releaseByExternalId.isPresent()) {
            return releaseByExternalId;
        }
        return getReleaseByNameAndVersion(sw360ReleaseFromArtifact, header);
    }

    public Optional<SW360Release> getRelease(SW360Release sw360ReleaseFromArtifact, HttpHeaders header) {
        return getSparseRelease(sw360ReleaseFromArtifact, header)
                .map(sr -> enrichSparseRelease(sr, header));
    }

    public Optional<SW360SparseRelease> getReleaseByExternalIds(Map<String,String> externalIds, HttpHeaders headers) {
        final List<SW360SparseRelease> releasesByExternalIds = releaseClient.getReleasesByExternalIds(externalIds, headers);
        if (releasesByExternalIds.size() == 0) {
            return Optional.empty();
        } else if (releasesByExternalIds.size() == 1) {
            return Optional.of(releasesByExternalIds.get(0));
        } else {
            LOGGER.error("Multiple releases in SW360 matched by externalIDs");
            return Optional.empty();
        }
    }

    public Optional<SW360SparseRelease> getReleaseByNameAndVersion(SW360Release sw360ReleaseFromArtifact, HttpHeaders headers) {
        return sw360ComponentClientAdapter.getComponentByName(sw360ReleaseFromArtifact.getName(), headers)
                .map(SW360Component::get_Embedded)
                .map(SW360ComponentEmbedded::getReleases)
                .flatMap(releases -> releases.stream()
                        .filter(release -> sw360ReleaseFromArtifact.getVersion().equals(release.getVersion()))
                        .findFirst());
    }

    public Optional<SW360Release> getReleaseByArtifact(SW360Component component, Artifact artifact, HttpHeaders header) {
        String releaseVersionOfArtifact = SW360ReleaseAdapterUtils.createSW360ReleaseVersion(artifact);
        if (component != null &&
                component.get_Embedded() != null &&
                component.get_Embedded().getReleases() != null) {

            List<SW360SparseRelease> releases = component.get_Embedded().getReleases();
            Optional<String> releaseId = releases.stream()
                    .filter(release -> release.getVersion().equals(releaseVersionOfArtifact))
                    .findFirst()
                    .flatMap(release -> SW360HalResourceUtility.getLastIndexOfSelfLink(release.get_Links()));
            if (releaseId.isPresent()) {
                return Optional.of(getReleaseById(releaseId.get(), header));
            }
        }
        return Optional.empty();
    }
}
