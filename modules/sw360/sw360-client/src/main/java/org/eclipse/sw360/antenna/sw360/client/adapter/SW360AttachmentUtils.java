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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.sw360.antenna.sw360.client.rest.SW360AttachmentAwareClient;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
public class SW360AttachmentUtils {
    /**
     * String for the digits for a Hex-string conversion.
     */
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Name of the SHA-1 algorithm to calculate hashes.
     */
    private static final String ALG_SHA1 = "SHA-1";

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
     * Calculates a hash value on the content of the file specified. This
     * functionality is useful in relation with attachments, as by comparing
     * the hashes it can be determined whether a local file is identical to an
     * attachment that was uploaded to SW360.
     *
     * @param file   the path to the file for which the hash should be computed
     * @param digest the digest for doing the calculation
     * @return the (Hex-encoded) hash value for this file
     * @throws SW360ClientException if an error occurs
     */
    public static String calculateHash(Path file, MessageDigest digest) {
        try (DigestInputStream din = new DigestInputStream(Files.newInputStream(file), digest)) {
            IOUtils.copy(din, NullOutputStream.NULL_OUTPUT_STREAM);
        } catch (IOException e) {
            throw new SW360ClientException("Could not calculate hash for file " + file, e);
        }
        return toHexString(digest.digest());
    }

    /**
     * Calculates a SHA1 hash value on the content of the file specified. This
     * is a specialization to the {@link #calculateHash(Path, MessageDigest)}
     * method, which uses a {@code MessageDigest} for SHA1 calculation. This
     * algorithm is used by SW360 per default to calculate hashes on uploaded
     * attachments.
     *
     * @param file the path to the file for which the hash should be computed
     * @return the (Hex-encoded) hash value for this file
     * @throws SW360ClientException if an error occurs
     */
    public static String calculateSha1Hash(Path file) {
        try {
            return calculateHash(file, MessageDigest.getInstance(ALG_SHA1));
        } catch (NoSuchAlgorithmException e) {
            // This cannot happen as every implementation of the Java platform must support this algorithm
            throw new AssertionError("SHA-1 algorithm not supported");
        }
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

    /**
     * Converts the given byte array to a string with hexadecimal digits.
     *
     * @param bytes the array of bytes
     * @return the resulting hex string
     */
    private static String toHexString(byte[] bytes) {
        char[] encoded = new char[bytes.length * 2];
        int idx = 0;
        for (byte b : bytes) {
            encoded[idx++] = HEX_DIGITS[b >> 4 & 0xF];
            encoded[idx++] = HEX_DIGITS[b & 0xF];
        }
        return new String(encoded);
    }
}
