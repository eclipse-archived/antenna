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
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    public CompletableFuture<SW360Release> createRelease(SW360Release release) {
        if (!SW360ReleaseAdapterUtils.isValidRelease(release)) {
            return failedFuture(new SW360ClientException("Can not write invalid release for " +
                    release.getName() + "-" + release.getVersion()));
        }
        if (release.getId() != null) {
            throw new SW360ClientException("Can not write release which already has the id " + release.getId());
        }

        return assignReleaseToComponent(release)
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
                        if (cfs.getEmbedded().getReleases().stream()
                                .map(SW360SparseRelease::getVersion)
                                .anyMatch(release.getVersion()::equals)) {
                            throw new SW360ClientException("The release already exists in the found component");
                        }
                        release.setComponentId(cfs.getId());
                    });
                    return release;
                });
    }

    @Override
    public CompletableFuture<AttachmentUploadResult> uploadAttachments(AttachmentUploadRequest uploadRequest) {
        CompletableFuture<AttachmentUploadResult> futResult =
                CompletableFuture.completedFuture(new AttachmentUploadResult(uploadRequest.getTarget()));

        for (AttachmentUploadRequest.Item item : uploadRequest.items()) {
            futResult = futResult.thenCompose(result -> {
                if (attachmentIsPotentialDuplicate(item.getPath(), result.getTarget().getEmbedded().getAttachments())) {
                    return CompletableFuture.completedFuture(result.addFailedUpload(item,
                            new SW360ClientException("Duplicate attachment file name: " +
                                    item.getPath().getFileName())));
                }

                return getReleaseClient()
                        .uploadAndAttachAttachment(result.getTarget(), item.getPath(), item.getAttachmentType())
                        .handle((updatedRelease, ex) -> (updatedRelease != null) ?
                                result.addSuccessfulUpload(updatedRelease, item) :
                                result.addFailedUpload(item, ex));
            });
        }

        return futResult;
    }

    private static boolean attachmentIsPotentialDuplicate(Path attachment, Set<SW360SparseAttachment> attachments) {
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
    public CompletableFuture<Optional<SW360SparseRelease>> getSparseReleaseByExternalIds(Map<String, ?> externalIds) {
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
    public CompletableFuture<Optional<SW360SparseRelease>> getSparseReleaseByNameAndVersion(String componentName,
                                                                                            String version) {
        return getComponentAdapter().getComponentByName(componentName)
                .thenApply(optComponent ->
                        optComponent.map(SW360Component::getEmbedded)
                                .map(SW360ComponentEmbedded::getReleases)
                                .flatMap(releases -> releases.stream()
                                        .filter(rel -> version.equals(rel.getVersion()))
                                        .findFirst()));

    }

    @Override
    public CompletableFuture<Optional<SW360Release>> getReleaseByVersion(SW360Component component, String releaseVersion) {
        if (component != null &&
                component.getEmbedded() != null &&
                component.getEmbedded().getReleases() != null) {

            List<SW360SparseRelease> releases = component.getEmbedded().getReleases();
            Optional<String> releaseId = releases.stream()
                    .filter(release -> release.getVersion().equals(releaseVersion))
                    .findFirst()
                    .flatMap(release -> SW360HalResourceUtility.getLastIndexOfSelfLink(release.getLinks()));
            if (releaseId.isPresent()) {
                return getReleaseById(releaseId.get());
            }
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<Path>> downloadAttachment(SW360Release release, SW360SparseAttachment attachment, Path downloadPath) {
        return Optional.ofNullable(release.getLinks().getSelf())
                .map(self ->
                        optionalFuture(getReleaseClient().downloadAttachment(self.getHref(), attachment, downloadPath)))
                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<SW360Release> updateRelease(SW360Release release) {
        return getReleaseClient().patchRelease(release);
    }
}
