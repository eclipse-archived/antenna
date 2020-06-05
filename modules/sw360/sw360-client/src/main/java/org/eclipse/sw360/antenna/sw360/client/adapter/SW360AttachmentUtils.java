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

import org.eclipse.sw360.antenna.sw360.client.rest.SW360AttachmentAwareClient;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.optionalFuture;

/**
 * <p>
 * A helper class providing utility methods related to the handling of
 * attachments.
 * </p>
 * <p>
 * Attachments are supported by multiple entity types. Therefore, it makes
 * sense to extract the functionality into a separate utility class.
 * </p>
 */
class SW360AttachmentUtils {
    private SW360AttachmentUtils() {
    }

    /**
     * Processes a request to upload multiple attachments. All attachment files
     * referenced by the passed in request are uploaded to the target entity. A
     * result object is returned with information about the single upload
     * operations.
     *
     * @param client             the client that handles a single upload operation
     * @param uploadRequest      the request to upload attachments
     * @param getAttachmentsFunc a function to access the existing attachments;
     *                           this is used to check for duplicates
     * @param <T>                the type of the target entity for the upload
     * @return a result object for the multi-upload operation
     */
    public static <T extends SW360HalResource<?, ?>> CompletableFuture<AttachmentUploadResult<T>>
    uploadAttachments(SW360AttachmentAwareClient<T> client, AttachmentUploadRequest<T> uploadRequest,
                      Function<? super T, Set<SW360SparseAttachment>> getAttachmentsFunc) {
        CompletableFuture<AttachmentUploadResult<T>> futResult =
                CompletableFuture.completedFuture(new AttachmentUploadResult<>(uploadRequest.getTarget()));

        for (AttachmentUploadRequest.Item item : uploadRequest.getItems()) {
            futResult = futResult.thenCompose(result -> {
                if (attachmentIsPotentialDuplicate(item.getPath(), getAttachmentsFunc.apply(result.getTarget()))) {
                    return CompletableFuture.completedFuture(result.addFailedUpload(item,
                            new SW360ClientException("Duplicate attachment file name: " +
                                    item.getPath().getFileName())));
                }

                return client
                        .uploadAndAttachAttachment(result.getTarget(), item.getPath(), item.getAttachmentType())
                        .handle((updatedEntity, ex) -> (updatedEntity != null) ?
                                result.addSuccessfulUpload(updatedEntity, item) :
                                result.addFailedUpload(item, ex));
            });
        }

        return futResult;
    }

    /**
     * Downloads a specific attachment file assigned to an entity to a local
     * folder on the hard disk. The directory is created if it does not exist
     * yet (but not any non-existing parent components). Result is an
     * {@code Optional} with the path to the file that has been downloaded. If
     * the requested attachment cannot be resolved, the {@code Optional} is
     * empty.
     *
     * @param client       the client that handles the download operation
     * @param entity       the entity to which the attachment belongs
     * @param attachment   the attachment that is to be downloaded
     * @param downloadPath the path where to store the downloaded file
     * @param <T>          the type of the entity that owns the attachment
     * @return a future with the {@code Optional} containing the path to the
     * file that was downloaded
     */
    public static <T extends SW360HalResource<?, ?>> CompletableFuture<Optional<Path>>
    downloadAttachment(SW360AttachmentAwareClient<? extends T> client, T entity, SW360SparseAttachment attachment,
                       Path downloadPath) {
        return Optional.ofNullable(entity.getSelfLink())
                .map(self ->
                        optionalFuture(client.downloadAttachment(self.getHref(), attachment, downloadPath)))
                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    /**
     * Checks whether an attachment with a specific name already exists for the
     * target entity.
     *
     * @param attachment  the path to the local attachment file
     * @param attachments the attachments assigned to the target entity
     * @return a flag whether the attachment is duplicate
     */
    private static boolean attachmentIsPotentialDuplicate(Path attachment, Set<SW360SparseAttachment> attachments) {
        return attachments.stream()
                .anyMatch(attachment1 -> attachment1.getFilename().equals(attachment.getFileName().toString()));
    }
}
