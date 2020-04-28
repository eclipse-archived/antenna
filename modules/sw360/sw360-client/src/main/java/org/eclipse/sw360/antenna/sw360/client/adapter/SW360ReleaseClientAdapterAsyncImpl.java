/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ReleaseClient;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.failedFuture;
import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.optionalFuture;

/**
 * Adapter implementation for the SW360 releases endpoint.
 */
class SW360ReleaseClientAdapterAsyncImpl implements SW360ReleaseClientAdapterAsync {
    private final SW360ReleaseClient releaseClient;
    private final SW360ComponentClientAdapterAsync sw360ComponentClientAdapter;

    public SW360ReleaseClientAdapterAsyncImpl(SW360ReleaseClient client,
                                              SW360ComponentClientAdapterAsync componentClientAdapter) {
        releaseClient = client;
        sw360ComponentClientAdapter = componentClientAdapter;
    }

    @Override
    public SW360ReleaseClient getReleaseClient() {
        return releaseClient;
    }

    public SW360ComponentClientAdapterAsync getComponentAdapter() {
        return sw360ComponentClientAdapter;
    }

    @Override
    public CompletableFuture<SW360Release> getOrCreateRelease(SW360Release sw360ReleaseFromArtifact, boolean updateReleases) {
        // NOTE: this code does now always merge with the SW360Release used for querying
        return getRelease(sw360ReleaseFromArtifact)
                .thenApply(optRelease -> optRelease.map(sw360ReleaseFromArtifact::mergeWith))
                .thenCompose(optRelease -> {
                    if (updateReleases && optRelease.isPresent()) {
                        return getReleaseClient().patchRelease(optRelease.get())
                                .thenApply(Optional::of);
                    } else {
                        return CompletableFuture.completedFuture(optRelease);
                    }
                })
                .thenCompose(optRelease ->
                        optRelease
                                .map(CompletableFuture::completedFuture)
                                .orElseGet(() -> createRelease(sw360ReleaseFromArtifact)));
    }

    /*
     * Create a release in SW360
     */
    @Override
    public CompletableFuture<SW360Release> createRelease(SW360Release releaseFromArtifact) {
        if (!SW360ReleaseAdapterUtils.isValidRelease(releaseFromArtifact)) {
            return failedFuture(new SW360ClientException("Can not write invalid release for " +
                    releaseFromArtifact.getName() + "-" + releaseFromArtifact.getVersion()));
        }
        if (releaseFromArtifact.getReleaseId() != null) {
            throw new SW360ClientException("Can not write release which already has the id " + releaseFromArtifact.getReleaseId());
        }

        return assignReleaseToComponent(releaseFromArtifact)
                .thenCompose(getReleaseClient()::createRelease);
    }

    /**
     * Obtains the component a new release belongs to and establishes the link.
     * It is also checked whether the release is not already present for this
     * component.
     *
     * @param release the release to be created
     * @return a future with the updated and validated release
     */
    private CompletableFuture<SW360Release> assignReleaseToComponent(SW360Release release) {
        final SW360Component componentFromRelease = SW360ComponentAdapterUtils.createFromRelease(release);
        return getComponentAdapter().getOrCreateComponent(componentFromRelease)
                .thenApply(componentFromSW360 -> {
                    componentFromSW360.ifPresent(cfs -> {
                        if (cfs.get_Embedded().getReleases().stream()
                                .map(SW360SparseRelease::getVersion)
                                .anyMatch(release.getVersion()::equals)) {
                            throw new SW360ClientException("The release already exists in the found component");
                        }
                        release.setComponentId(cfs.getComponentId());
                    });
                    return release;
                });
    }

    @Override
    public CompletableFuture<SW360Release> uploadAttachments(SW360Release sw360item, Map<Path, SW360AttachmentType> attachments) {
        CompletableFuture<SW360Release> futUpdatedRelease = CompletableFuture.completedFuture(sw360item);
        for(Map.Entry<Path, SW360AttachmentType> attachment : attachments.entrySet()) {
            if (!attachmentIsPotentialDuplicate(attachment.getKey(), sw360item.get_Embedded().getAttachments())) {
                futUpdatedRelease = futUpdatedRelease.thenCompose(release ->
                        getReleaseClient().uploadAndAttachAttachment(sw360item, attachment.getKey(),
                                attachment.getValue()));
            }
        }

        return futUpdatedRelease;
    }

    private static boolean attachmentIsPotentialDuplicate(Path attachment, List<SW360SparseAttachment> attachments) {
        return attachments.stream()
                .anyMatch(attachment1 -> attachment1.getFilename().equals(attachment.getFileName().toString()));
    }

    @Override
    public CompletableFuture<Optional<SW360Release>> getReleaseById(String releaseId) {
        return optionalFuture(getReleaseClient().getRelease(releaseId));
    }

    @Override
    public CompletableFuture<Optional<SW360Release>> enrichSparseRelease(SW360SparseRelease sparseRelease) {
        return getReleaseById(sparseRelease.getReleaseId());
    }

    @Override
    public CompletableFuture<Optional<SW360SparseRelease>> getSparseRelease(SW360Release release) {
        return getReleaseByExternalIds(release.getExternalIds())
                .thenCompose(optRelease ->
                        optRelease.map(rel -> CompletableFuture.completedFuture(Optional.of(rel)))
                        .orElseGet(() -> getReleaseByNameAndVersion(release)));
    }

    @Override
    public CompletableFuture<Optional<SW360Release>> getRelease(SW360Release sw360ReleaseFromArtifact) {
        return getSparseRelease(sw360ReleaseFromArtifact)
                .thenCompose(optRelease ->
                        optRelease.map(this::enrichSparseRelease)
                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
    }

    @Override
    public CompletableFuture<Optional<SW360SparseRelease>> getReleaseByExternalIds(Map<String, ?> externalIds) {
        return getReleaseClient().getReleasesByExternalIds(externalIds)
                .thenApply(releases -> {
                    if (releases.isEmpty()) {
                        return Optional.empty();
                    } else if (releases.size() == 1) {
                        return Optional.of(releases.get(0));
                    } else {
                        throw new SW360ClientException("Multiple releases in SW360 matched by externalIDs: " +
                                externalIds);
                    }
                });
    }

    @Override
    public CompletableFuture<Optional<SW360SparseRelease>> getReleaseByNameAndVersion(SW360Release release) {
        return getComponentAdapter().getComponentByName(release.getName())
                .thenApply(optComponent ->
                        optComponent.map(SW360Component::get_Embedded)
                                .map(SW360ComponentEmbedded::getReleases)
                                .flatMap(releases -> releases.stream()
                                        .filter(rel -> release.getVersion().equals(rel.getVersion()))
                                        .findFirst()));

    }

    @Override
    public CompletableFuture<Optional<SW360Release>> getReleaseByVersion(SW360Component component, String releaseVersion) {
        if (component != null &&
                component.get_Embedded() != null &&
                component.get_Embedded().getReleases() != null) {

            List<SW360SparseRelease> releases = component.get_Embedded().getReleases();
            Optional<String> releaseId = releases.stream()
                    .filter(release -> release.getVersion().equals(releaseVersion))
                    .findFirst()
                    .flatMap(release -> SW360HalResourceUtility.getLastIndexOfSelfLink(release.get_Links()));
            if (releaseId.isPresent()) {
                return getReleaseById(releaseId.get());
            }
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<Path>> downloadAttachment(SW360Release release, SW360SparseAttachment attachment, Path downloadPath) {
        return Optional.ofNullable(release.get_Links().getSelf())
                .map(self ->
                        optionalFuture(getReleaseClient().downloadAttachment(self.getHref(), attachment, downloadPath)))
                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }
}
