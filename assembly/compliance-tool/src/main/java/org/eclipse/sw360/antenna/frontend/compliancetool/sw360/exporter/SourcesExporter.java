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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private static final Logger LOG = LoggerFactory.getLogger(SourcesExporter.class);

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
        final ConcurrentMap<ReleaseWithSources, Boolean> processedReleases = new ConcurrentHashMap<>();
        CompletableFuture<?>[] releaseFutures = releases.stream()
                .map(release -> downloadSourcesForRelease(releaseAdapter, release)
                        .thenApply(relWithSrc -> processedReleases.put(relWithSrc, Boolean.TRUE)))
                .toArray(CompletableFuture[]::new);
        CompletableFuture<Void> downloadFuture = CompletableFuture.allOf(releaseFutures);

        downloadFuture.join();
        return processedReleases.keySet();
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
        final ConcurrentMap<Path, Boolean> paths = new ConcurrentHashMap<>();
        CompletableFuture<?>[] downloads = release.getEmbedded().getAttachments().stream()
                .filter(attachment -> attachment.getAttachmentType() == SW360AttachmentType.SOURCE)
                .map(attachment -> downloadAttachment(releaseAdapter, release, attachment, paths))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(downloads)
                .thenApply(v -> new ReleaseWithSources(release, paths.keySet()));
    }

    /**
     * Downloads a single source attachment. If the download is successful,
     * the result is stored in the given map.
     *
     * @param releaseAdapter the release adapter
     * @param release        the release the download is for
     * @param attachment     the attachment to be downloaded
     * @param paths          the map for storing the download result
     * @return a future that completes when the download is finished
     */
    private CompletableFuture<Object> downloadAttachment(SW360ReleaseClientAdapterAsync releaseAdapter,
                                                         SW360Release release, SW360SparseAttachment attachment,
                                                         ConcurrentMap<Path, Boolean> paths) {
        return releaseAdapter.downloadAttachment(release, attachment, sourcePath)
                .handle((optPath, ex) -> {
                    if (ex == null) {
                        if (optPath.isPresent()) {
                            paths.put(optPath.get(), Boolean.TRUE);
                        } else {
                            LOG.warn("Could not resolve attachment {} for release {}:{}", attachment.getFilename(),
                                    release.getName(), release.getVersion());
                        }
                    } else {
                        LOG.error("Failed to download attachment {} for release {}:{}", attachment.getFilename(),
                                release.getName(), release.getVersion(), ex);
                    }
                    return null;
                });
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
    }
}
