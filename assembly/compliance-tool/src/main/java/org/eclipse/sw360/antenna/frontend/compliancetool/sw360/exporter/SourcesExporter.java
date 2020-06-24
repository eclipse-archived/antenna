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

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360AttachmentUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapterAsync;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * download-related functionality. Downloads are done in parallel for all
 * source attachments assigned to releases. Files are stored in a directory
 * structure below the configured sources directory. To avoid clashes with file
 * names (there is no guarantee that only unique file names are used for
 * attachments), for each release a dedicated folder is created in which its
 * source attachments are downloaded. The folder structure is derived from the
 * release name and its version.
 * </p>
 */
class SourcesExporter {
    /**
     * A {@code Comparator} for sorting {@code ReleaseWithSources} objects.
     * This comparator is used to sort the list of releases before it is
     * written to the output CSV file.
     */
    static final Comparator<ReleaseWithSources> RELEASES_COMPARATOR = createReleaseComparator();

    /**
     * Format to generate a message about a failed attachment download.
     */
    private static final String FMT_DOWNLOAD_ERROR = "Failed to download attachment %s for release %s:%s";

    /**
     * A string with characters that are allowed in path names generated from
     * release names. If a release name, its version, or an attachment file
     * name contains characters not contained in this string, they are replaced
     * to avoid problems with file system operations.
     */
    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "0123456789.:-";

    /**
     * The character to replace illegal characters in path names.
     */
    private static final char REPLACE_CHARACTER = '_';

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
     * Removes all files and directories from the sources directory which are
     * not referenced by one of the given releases. This method can be called
     * after the download is complete to clean-up the directory from files that
     * are no longer relevant for the compliance workflow.
     *
     * @param releases the list with currently relevant releases
     */
    public void removeUnreferencedFiles(Collection<ReleaseWithSources> releases) {
        RemoveUnreferencedFilesVisitor visitor = new RemoveUnreferencedFilesVisitor(releases);
        try {
            Files.walkFileTree(sourcePath, visitor);
        } catch (IOException e) {
            LOG.error("Could not traverse sources directory {}", sourcePath, e);
        }
    }

    /**
     * Calculates the SHA-1 hash for a local attachment file.
     *
     * @param localPath the local path to the attachment file
     * @return the SHA-1 hash calculated for this file
     * @throws org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException if an error occurs
     */
    String calculateLocalAttachmentHash(Path localPath) {
        return SW360AttachmentUtils.calculateSha1Hash(localPath);
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
        Path releaseFolder = pathForRelease(release);
        List<CompletableFuture<Path>> downloads = release.getEmbedded().getAttachments().stream()
                .filter(attachment -> attachment.getAttachmentType() == SW360AttachmentType.SOURCE)
                .map(attachment -> downloadAttachment(releaseAdapter, release, attachment, releaseFolder))
                .collect(Collectors.toList());
        return FutureUtils.sequence(downloads, SourcesExporter::logDownloadFailure)
                .thenApply(paths -> new ReleaseWithSources(release, new HashSet<>(paths)));
    }

    /**
     * Asynchronously downloads a single source attachment. A download is
     * triggered only if no local file with the expected hash exists. If the
     * download fails, a meaningful exception message is generated.
     *
     * @param releaseAdapter the release adapter
     * @param release        the release the download is for
     * @param attachment     the attachment to be downloaded
     * @param releasePath    the path where to store the release's attachments
     * @return a future that completes when the download is finished
     */
    private CompletableFuture<Path> downloadAttachment(SW360ReleaseClientAdapterAsync releaseAdapter,
                                                       SW360Release release, SW360SparseAttachment attachment,
                                                       Path releasePath) {
        return getLocalAttachmentPath(attachment, releasePath)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> FutureUtils.wrapFutureForConditionalFallback(releaseAdapter.processAttachment(release,
                        attachment.getAttachmentId(),
                        createDownloadProcessor(releasePath, attachment)), ex -> true,
                        () -> FutureUtils.failedFuture(new IllegalStateException(String.format(FMT_DOWNLOAD_ERROR,
                                attachment.getFilename(), release.getName(), release.getVersion()))))
                );
    }

    /**
     * Tries to find the local path for an attachment. This method prevents
     * unnecessary downloads by checking whether the attachment already exists
     * locally and - based on the hash - has not been modified. If this is the
     * case, an {@code Optional} with the local path is returned. Otherwise,
     * result is an empty {@code Optional}, and the attachment must be
     * downloaded.
     *
     * @param attachment  the attachment affected
     * @param releasePath the path where to store the release's attachments
     * @return an {@code Optional} with the local attachment path
     */
    private Optional<Path> getLocalAttachmentPath(SW360SparseAttachment attachment, Path releasePath) {
        Path localPath = releasePath.resolve(attachment.getFilename());
        try {
            return Files.exists(localPath) &&
                    calculateLocalAttachmentHash(localPath).equals(attachment.getSha1()) ?
                    Optional.of(localPath) : Optional.empty();
        } catch (SW360ClientException e) {
            LOG.warn("Failed to verify local attachment file", e);
            return Optional.empty();
        }
    }

    /**
     * Determines the local path where files associated with the given release
     * are stored. All the releases of a component are stored in the same sub
     * folder; the attachments of a specific release are in a sub folder whose
     * name is derived from the version.
     *
     * @param release the release
     * @return the storage directory for the attachments of this release
     */
    private Path pathForRelease(SW360Release release) {
        return sourcePath.resolve(sanitizePath(release.getName()))
                .resolve(sanitizePath(release.getVersion()));
    }

    /**
     * Creates the processor to download an attachment of a release.
     *
     * @param releasePath the download path for the release
     * @param attachment  the attachment to be downloaded
     * @return the processor to download this attachment
     */
    private static SW360AttachmentUtils.AttachmentDownloadProcessorCreateDownloadFolderWithParents
    createDownloadProcessor(Path releasePath, SW360SparseAttachment attachment) {
        return new SW360AttachmentUtils.AttachmentDownloadProcessorCreateDownloadFolderWithParents(releasePath,
                sanitizePath(attachment.getFilename()), StandardCopyOption.REPLACE_EXISTING);
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
     * Makes sure that a string to be used as path name contains only allowed
     * characters. All other characters are replaced by an underscore.
     *
     * @param name the name to be sanitized
     * @return the name with problematic characters replaced
     */
    private static String sanitizePath(String name) {
        if (StringUtils.containsOnly(name, ALLOWED_CHARACTERS)) {
            return name;
        }

        StrBuilder buf = new StrBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            buf.append(ALLOWED_CHARACTERS.indexOf(c) >= 0 ? c : REPLACE_CHARACTER);
        }
        return buf.toString();
    }

    /**
     * Creates a {@code Comparator} for sorting a list of
     * {@code ReleaseWithSources} objects.
     *
     * @return the {@code Comparator}
     */
    private static Comparator<ReleaseWithSources> createReleaseComparator() {
        Comparator<ReleaseWithSources> cCreatedAsc = Comparator.comparing(rel -> rel.getRelease().getCreatedOn());
        Comparator<ReleaseWithSources> cCreated = cCreatedAsc.reversed();
        return cCreated.thenComparing(rel -> rel.getRelease().getName())
                .thenComparing(rel -> rel.getRelease().getVersion());
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

    /**
     * An implementation of a file visitor to iterate over the sources folder
     * and to remove all files and folders that are not referenced by the
     * current set of releases. As the releases of a component share parts of
     * the folder structure, it has to be checked carefully, which files can
     * actually be removed.
     */
    private static class RemoveUnreferencedFilesVisitor extends SimpleFileVisitor<Path> {
        /**
         * A set with tuples for all existing releases and versions.
         */
        private final Set<Pair<String, String>> releaseVersions;

        /**
         * A set with the names of all the existing releases.
         */
        private final Set<String> releaseNames;

        /**
         * Stores the name of the release that is associated with the currently
         * visited directory.
         */
        private String currentReleaseName;

        /**
         * Keeps track on the level in the current directory structure.
         */
        private int level = -1;

        /**
         * A flag whether elements encountered during the traversal of the
         * directory structure should be deleted.
         */
        private boolean shouldDelete;

        /**
         * Creates a new instance of {@code RemoveUnreferencedFilesVisitor} and
         * initializes it with all existing releases.
         *
         * @param releases a collection with all known releases
         */
        public RemoveUnreferencedFilesVisitor(Collection<ReleaseWithSources> releases) {
            releaseVersions = extractReleaseVersions(releases);
            releaseNames = extractReleaseNames(releases);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            level++;
            updateTraversalState(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            deleteIfNecessary(file, "Removing unreferenced source attachment {}.");
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            level--;
            if (level >= 0) {
                deleteIfNecessary(dir, "Removing unreferenced directory {}.");
            }
            updateTraversalState(dir.getParent());
            return FileVisitResult.CONTINUE;
        }

        /**
         * Updates the current state of the traversal after changing the
         * current directory. Keeps track on the release associated with the
         * current directory; checks whether it exists and if files need to be
         * deleted.
         *
         * @param dir the current directory in the traversal
         */
        private void updateTraversalState(Path dir) {
            if (level == 1) {
                currentReleaseName = String.valueOf(dir.getFileName());
                shouldDelete = !releaseNames.contains(currentReleaseName);
            } else if (level == 2) {
                Pair<String, String> relVer = Pair.of(currentReleaseName, String.valueOf(dir.getFileName()));
                shouldDelete = !releaseVersions.contains(relVer);
            } else {
                shouldDelete = level >= 0;
            }
        }

        /**
         * Deletes an element from a folder structure depending on the current
         * state of the traversal. Logs a message and handles exceptions.
         *
         * @param path the path to be deleted
         * @param msg  the message to be logged
         */
        private void deleteIfNecessary(Path path, String msg) {
            if (shouldDelete) {
                LOG.info(msg, path);
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    LOG.warn("Could not delete source attachment {}", path, e);
                }
            }
        }

        /**
         * Generates a set with tuples for all known releases and their
         * versions.
         *
         * @param releases a collection with all known releases
         * @return a set with the known releases and versions
         */
        private static Set<Pair<String, String>> extractReleaseVersions(Collection<ReleaseWithSources> releases) {
            return releases.stream()
                    .map(release -> Pair.of(sanitizePath(release.getRelease().getName()),
                            sanitizePath(release.getRelease().getVersion())))
                    .collect(Collectors.toSet());
        }

        /**
         * Generates a set with the names of all known releases.
         *
         * @param releases a collection with all known releases
         * @return a set with all existing release names
         */
        private static Set<String> extractReleaseNames(Collection<ReleaseWithSources> releases) {
            return releases.stream()
                    .map(release -> sanitizePath(release.getRelease().getName()))
                    .collect(Collectors.toSet());
        }
    }
}
