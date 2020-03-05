/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.client.rest;

import org.eclipse.sw360.antenna.http.api.RequestBuilder;
import org.eclipse.sw360.antenna.http.utils.HttpConstants;
import org.eclipse.sw360.antenna.sw360.client.auth.AccessTokenProvider;
import org.eclipse.sw360.antenna.sw360.client.config.SW360ClientConfig;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * A base class for client implementations that deal with resources supporting
 * attachments.
 * </p>
 * <p>
 * The class provides methods to upload and download attachments linked to
 * another resource.
 * </p>
 *
 * @param <T> the type of the resource handled by this class
 */
public abstract class SW360AttachmentAwareClient<T extends SW360HalResource<?, ?>> extends SW360Client {
    /**
     * Tag for the request to upload an attachment.
     */
    static final String TAG_UPLOAD_ATTACHMENT = "post_upload_attachment";

    /**
     * Tag for the request to download an attachment.
     */
    static final String TAG_DOWNLOAD_ATTACHMENT = "get_download_attachment";

    private static final Logger LOGGER = LoggerFactory.getLogger(SW360AttachmentAwareClient.class);
    private static final String ATTACHMENTS_ENDPOINT = "/attachments";

    /**
     * Creates a new instance of {@code SW360AttachmentAwareClient} with the
     * dependencies passed in.
     *
     * @param config   the client configuration
     * @param provider the access token provider
     */
    protected SW360AttachmentAwareClient(SW360ClientConfig config, AccessTokenProvider provider) {
        super(config, provider);
    }

    /**
     * Returns the type of the resource class for which attachments are
     * managed by this client. This is needed to do the correct JSON
     * deserialization.
     *
     * @return the resource class managed by this client
     */
    protected abstract Class<T> getHandledClassType();

    /**
     * Uploads an attachment file, creates an entity for it, and assigns it to
     * the entity represented by the passed in object. The path provided must
     * point to an existing file. A future with the modified entity (the one
     * the attachment has been added to) is returned.
     *
     * @param itemToModify a data object defining the resource to attach the
     *                     file
     * @param fileToAttach the path to the file to be attached
     * @param kindToAttach the attachment type
     * @return a future with the entity that has been modified
     */
    public CompletableFuture<T> uploadAndAttachAttachment(T itemToModify, Path fileToAttach,
                                                          SW360AttachmentType kindToAttach) {
        if (!Files.exists(fileToAttach)) {
            LOGGER.warn("The file=[{}], which should be attached to release, does not exist", fileToAttach);
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IOException("File to upload does not exist: " + fileToAttach));
            return failedFuture;
        }

        SW360Attachment sw360Attachment = new SW360Attachment(fileToAttach, kindToAttach);
        final String self = itemToModify.get_Links().getSelf().getHref();
        return executeJsonRequest(builder -> builder.method(RequestBuilder.Method.POST)
                        .uri(self + ATTACHMENTS_ENDPOINT)
                        .bodyPart("attachment", part ->
                                part.bodyJson(sw360Attachment))
                        .bodyPart("file", part ->
                                part.bodyFile(fileToAttach, HttpConstants.CONTENT_OCTET_STREAM)),
                getHandledClassType(), TAG_UPLOAD_ATTACHMENT);
    }

    /**
     * Downloads a specific attachment file and stores it in the directory
     * provided as parameter. The directory is created if it does not exist yet
     * (but not any non-existing parent components). If the attachment cannot
     * be resolved, the resulting future fails with a
     * {@link org.eclipse.sw360.antenna.http.utils.FailedRequestException} with
     * status code 404; it contains an {@code IOException} if there was a
     * problem with an operation on the file system or if the server could not
     * be contacted. Note that this operation is not atomic; it may already
     * create a directory and then fail if the attachment cannot be resolved.
     *
     * @param itemHref     the base resource URL to access the attachment entity
     * @param attachment   a data object defining the attachment to be loaded
     * @param downloadPath the path where to store the file downloaded
     * @return a future with the path where the file was stored
     */
    public CompletableFuture<Path> downloadAttachment(String itemHref, SW360SparseAttachment attachment,
                                                      Path downloadPath) {
        String attachmentId = attachment.getAttachmentId();
        String url = itemHref + "/attachments/" + attachmentId;
        try {
            if (!Files.isDirectory(downloadPath)) {
                LOGGER.debug("Creating download path {}.", downloadPath);
                Files.createDirectory(downloadPath);
            }
            Path targetFile = downloadPath.resolve(attachment.getFilename());

            return executeRequest(builder -> builder.uri(url)
                    .header(HttpConstants.HEADER_ACCEPT, HttpConstants.CONTENT_OCTET_STREAM),
                    response -> {
                        Files.copy(response.bodyStream(), targetFile);
                        return targetFile;
                    }, TAG_DOWNLOAD_ATTACHMENT);
        } catch (IOException e) {
            LOGGER.warn("Request to write downloaded attachment {} to {} failed with {}", attachment.getFilename(), downloadPath, e.getMessage());
            LOGGER.debug("Error: ", e);
            CompletableFuture<Path> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}
