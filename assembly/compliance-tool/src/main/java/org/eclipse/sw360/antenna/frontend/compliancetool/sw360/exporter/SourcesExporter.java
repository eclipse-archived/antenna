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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapterAsync;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * An internally used helper class that handles the download of source
 * attachments for releases.
 * </p>
 * <p>
 * The compliance tool exporter delegates to an instance of this class for all
 * download-related functionality.
 * </p>
 */
class SourcesExporter {
    /**
     * Format to generate a message about an unresolvable attachment.
     */
    private static final String FMT_UNRESOLVABLE_ATTACHMENT = "Could not resolve attachment %s for release %s:%s";

    /**
     * Format to generate a message about a failed attachment download.
     */
    private static final String FMT_DOWNLOAD_ERROR = "Failed to download attachment %s for release %s:%s";

    private static final Logger LOG = LoggerFactory.getLogger(SourcesExporter.class);

    /**
     * The path where downloaded source attachments are stored.
     */
    private final Path sourcePath;

    /**
     * Creates a new instance of {@code SourcesExporter} and sets the directory
     * where sources are to be downloaded.
     *
     * @param sourcePath the path to the sources directory
     */
    public SourcesExporter(Path sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Downloads all source attachments for the given list of releases to the
     * configured download directory. The method ignores (just logs) errors and
     * returns a collection with information about the releases and the
     * attachments that could be downloaded successfully. The download is done
     * in parallel as far as possible (limited by the thread pool used by the
     * HTTP client).
     *
     * @param releaseAdapter the SW360 release client adapter
     * @param releases       the list with releases to be processed
     * @return a collection with the releases and the attachment paths that
     * were downloaded
     */
    public Collection<ReleaseWithSources> downloadSources(SW360ReleaseClientAdapterAsync releaseAdapter,
                                                          Collection<SW360Release> releases) {
        List<CompletableFuture<ReleaseWithSources>> downloadFutures = releases.stream()
                .map(release -> downloadSourcesForRelease(releaseAdapter, release))
                .collect(Collectors.toList());
        return FutureUtils.sequence(downloadFutures, ex -> false).join();
    }

    /**
     * Removes all files from the sources directory which are not referenced by
     * one of the given releases. This method can be called after the download
     * is complete to clean-up the directory from files that are no longer
     * relevant for the compliance workflow.
     *
     * @param releases the list with currently relevant releases
     */
    public void removeUnreferencedFiles(Collection<ReleaseWithSources> releases) {
        Set<Path> referencedPaths = releases.stream()
                .flatMap(release -> release.getSourceAttachmentPaths().stream())
                .collect(Collectors.toSet());

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(sourcePath)) {
            for (Path path : dirStream) {
                if (!referencedPaths.contains(path)) {
                    LOG.info("Removing unreferenced source attachment {}.", path);
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        LOG.warn("Could not delete source attachment {}", path, e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Could not open stream for sources directory {}", sourcePath, e);
        }
    }

    /**
     * Handles the attachment downloads for a single release. All attachments
     * of type <em>source</em> assigned to the release are downloaded (in
     * parallel). A future with the resulting {@code ReleaseWithSources} object
     * is returned.
     *
     * @param releaseAdapter the SW360 release client adapter
     * @param release        the release to be processed
     * @return an object with the result of the download operations
     */
    private CompletableFuture<ReleaseWithSources>
    downloadSourcesForRelease(SW360ReleaseClientAdapterAsync releaseAdapter, SW360Release release) {
        List<CompletableFuture<Path>> downloads = release.getEmbedded().getAttachments().stream()
                .filter(attachment -> attachment.getAttachmentType() == SW360AttachmentType.SOURCE)
                .map(attachment -> downloadAttachment(releaseAdapter, release, attachment))
                .collect(Collectors.toList());
        return FutureUtils.sequence(downloads, SourcesExporter::logDownloadFailure)
                .thenApply(paths -> new ReleaseWithSources(release, new HashSet<>(paths)));
    }

    /**
     * Asynchronously downloads a single source attachment. If the download
     * fails, a meaningful exception message is generated.
     *
     * @param releaseAdapter the release adapter
     * @param release        the release the download is for
     * @param attachment     the attachment to be downloaded
     * @return a future that completes when the download is finished
     */
    private CompletableFuture<Path> downloadAttachment(SW360ReleaseClientAdapterAsync releaseAdapter,
                                                       SW360Release release, SW360SparseAttachment attachment) {
        return FutureUtils.wrapFutureForConditionalFallback(releaseAdapter.downloadAttachment(release,
                attachment, sourcePath), ex -> true,
                () -> FutureUtils.failedFuture(new IllegalStateException(String.format(FMT_DOWNLOAD_ERROR,
                        attachment.getFilename(), release.getName(), release.getVersion()))))
                .thenApply(optPath -> {
                    if (optPath.isPresent()) {
                        return optPath.get();
                    } else {
                        throw new IllegalStateException(String.format(FMT_UNRESOLVABLE_ATTACHMENT,
                                attachment.getFilename(), release.getName(), release.getVersion()));
                    }
                });
    }

    /**
     * A special exception handling function to be passed to
     * {@link FutureUtils#sequence(Collection, Function)}, which just logs the
     * exception. Exceptions during download are ignored and do not cancel the
     * export process.
     *
     * @param ex the exception
     * @return flag whether the exception should cancel the process
     */
    private static boolean logDownloadFailure(Throwable ex) {
        LOG.warn(ex.getMessage());
        return false;
    }

    /**
     * A data class storing information about a release and the paths to the
     * source attachments that have been downloaded.
     */
    static final class ReleaseWithSources {
        /**
         * The release.
         */
        private final SW360Release release;

        /**
         * A set with paths to source attachments that have been downloaded.
         */
        private final Set<Path> sourceAttachmentPaths;

        public ReleaseWithSources(SW360Release release, Set<Path> sourceAttachmentPaths) {
            this.release = release;
            this.sourceAttachmentPaths = Collections.unmodifiableSet(new HashSet<>(sourceAttachmentPaths));
        }

        /**
         * Returns the release.
         *
         * @return the release
         */
        public SW360Release getRelease() {
            return release;
        }

        /**
         * Returns an unmodifiable set with paths to attachments that have been
         * downloaded.
         *
         * @return the set of downloaded attachment paths
         */
        public Set<Path> getSourceAttachmentPaths() {
            return sourceAttachmentPaths;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReleaseWithSources that = (ReleaseWithSources) o;
            return Objects.equals(getRelease(), that.getRelease()) &&
                    Objects.equals(getSourceAttachmentPaths(), that.getSourceAttachmentPaths());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getRelease(), getSourceAttachmentPaths());
        }

        @Override
        public String toString() {
            return "ReleaseWithSources{" +
                    "release=" + release +
                    ", sourceAttachmentPaths=" + sourceAttachmentPaths +
                    '}';
        }
    }
}
