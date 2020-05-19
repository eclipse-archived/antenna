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
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;

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
    /**
     * Returns the {@code SW360ReleaseClient} used by this adapter. The client
     * can be used for low-level operations against the SW360 releases
     * endpoint.
     *
     * @return the {@code SW360ReleaseClient}
     */
    SW360ReleaseClient getReleaseClient();

    /**
     * Creates a new release in SW360 based on the passed in entity. The given
     * entity must have all mandatory properties defined. It is assigned to the
     * component it references; the component instance is created if necessary.
     *
     * @param release the entity describing the release to be created
     * @return a future with the newly created release entity
     */
    CompletableFuture<SW360Release> createRelease(SW360Release release);

    /**
     * Tries to find the release with the given ID. Result is an
     * {@code Optional}; if the release ID cannot be resolved, the
     * {@code Optional} is empty.
     *
     * @param releaseId the ID of the release in question
     * @return a future with an {@code Optional} with the release found
     */
    CompletableFuture<Optional<SW360Release>> getReleaseById(String releaseId);

    /**
     * Tries to transform a sparse release into a full one. This method looks
     * up the release the given object points to and returns a data object with
     * all its properties. If the release cannot be resolved, result is an
     * empty {@code Optional}.
     *
     * @param sparseRelease the sparse release object
     * @return a future with an {@code Optional} with the release found
     */
    CompletableFuture<Optional<SW360Release>> enrichSparseRelease(SW360SparseRelease sparseRelease);

    /**
     * Searches for a release based on the given external IDs. This method
     * performs a search on the releases using the external IDs as criterion.
     * If no match is found, result is an empty {@code Optional}. If the search
     * yields multiple results, the resulting future fails with an exception.
     * Result is a sparse release, which can be converted to a full
     * {@link SW360Release} object by using the
     * {@link #enrichSparseRelease(SW360SparseRelease)} method.
     *
     * @param externalIds a map with the external IDs to search for
     * @return a future with an {@code Optional} with the release that was
     * found
     */
    CompletableFuture<Optional<SW360SparseRelease>> getSparseReleaseByExternalIds(Map<String, ?> externalIds);

    /**
     * Searches for a release based on the component name and the release
     * version. This method obtains the component associated with this release
     * and filters its releases for the correct version. Result is an empty
     * {@code Optional} if no matching version is found. Otherwise, a sparse
     * release is returned, which can be converted to a full
     * {@link SW360Release} by using the
     * {@link #enrichSparseRelease(SW360SparseRelease)} method.
     *
     * @param componentName the name of the component affected
     * @param version the version of the desired release
     * @return a future with an {@code Optional} with the release that was
     * found
     */
    CompletableFuture<Optional<SW360SparseRelease>> getSparseReleaseByNameAndVersion(String componentName,
                                                                                     String version);

    /**
     * Tries to retrieve the release with the given version from the passed in
     * {@code SW360Component} entity. If the component has a release with the
     * version specified, all its properties are loaded and returned in an
     * entity object; otherwise, result is an empty {@code Optional}.
     *
     * @param component      the {@code SW360Component} entity
     * @param releaseVersion the version of the release in question
     * @return a future with an {@code Optional} with the release found
     */
    CompletableFuture<Optional<SW360Release>> getReleaseByVersion(SW360Component component, String releaseVersion);

    /**
     * Uploads an arbitrary number of attachments for a release.
     *
     * @param sw360item   the release to upload attachments to
     * @param attachments a map defining the attachments to be uploaded
     * @return a future with the updated release entity
     */
    CompletableFuture<SW360Release> uploadAttachments(SW360Release sw360item,
                                                      Map<Path, SW360AttachmentType> attachments);

    /**
     * Tries to download an attachment from a release. If it can be resolved,
     * the attachment file is written into the download path provided. This
     * directory is created if it does not exist (but not any parent
     * directories).
     *
     * @param release      the release entity
     * @param attachment   the attachment to be downloaded
     * @param downloadPath the path where to store the downloaded file
     * @return a future with an {@code Optional} with the path to the file that
     * has been written
     */
    CompletableFuture<Optional<Path>> downloadAttachment(SW360Release release, SW360SparseAttachment attachment,
                                                         Path downloadPath);

    /**
     * Updates a release. The release is updated in the database based on the
     * properties of the passed in entity.
     *
     * @param release the release to be updated
     * @return a future with the updated release
     */
    CompletableFuture<SW360Release> updateRelease(SW360Release release);
}
