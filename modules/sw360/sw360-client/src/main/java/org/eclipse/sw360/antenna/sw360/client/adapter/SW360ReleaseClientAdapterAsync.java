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
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ReleaseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Service interface for an adapter supporting asynchronous operations on SW360
 * release entities.
 * </p>
 */
public interface SW360ReleaseClientAdapterAsync {
    SW360ReleaseClient getReleaseClient();

    CompletableFuture<SW360Release> getOrCreateRelease(SW360Release sw360ReleaseFromArtifact, boolean updateReleases);

    CompletableFuture<SW360Release> createRelease(SW360Release releaseFromArtifact);

    CompletableFuture<SW360Release> uploadAttachments(SW360Release sw360item, Map<Path,
            SW360AttachmentType> attachments);

    CompletableFuture<Optional<SW360Release>> getReleaseById(String releaseId);

    CompletableFuture<Optional<SW360Release>> enrichSparseRelease(SW360SparseRelease sparseRelease);

    CompletableFuture<Optional<SW360SparseRelease>> getSparseRelease(SW360Release sw360ReleaseFromArtifact);

    CompletableFuture<Optional<SW360Release>> getRelease(SW360Release sw360ReleaseFromArtifact);

    CompletableFuture<Optional<SW360SparseRelease>> getReleaseByExternalIds(Map<String, ?> externalIds);

    CompletableFuture<Optional<SW360SparseRelease>> getReleaseByNameAndVersion(SW360Release sw360ReleaseFromArtifact);

    CompletableFuture<Optional<SW360Release>> getReleaseByVersion(SW360Component component, String releaseVersion);

    CompletableFuture<Optional<Path>> downloadAttachment(SW360Release release, SW360SparseAttachment attachment,
                                                         Path downloadPath);
}
