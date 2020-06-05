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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapterAsync;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseEmbedded;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.failedFuture;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourcesExporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * The path where to store downloaded files.
     */
    private Path sourcePath;

    /**
     * Mock for the release client adapter.
     */
    private SW360ReleaseClientAdapterAsync releaseAdapter;

    /**
     * The exporter to be tested.
     */
    private SourcesExporter sourcesExporter;

    @Before
    public void setUp() throws IOException {
        sourcePath = folder.newFolder().toPath();
        releaseAdapter = mock(SW360ReleaseClientAdapterAsync.class);
        sourcesExporter = new SourcesExporter(sourcePath);
    }

    /**
     * Generates a number of source attachments for a test release.
     *
     * @param releaseIndex    the index of the test release
     * @param attachmentCount the number of attachments to generate
     * @return a set with the generated attachments
     */
    private static Set<SW360SparseAttachment> createAttachments(int releaseIndex, int attachmentCount) {
        Set<SW360SparseAttachment> attachments = new HashSet<>();
        for (int i = 0; i < attachmentCount; i++) {
            SW360SparseAttachment attachment = createAttachment(releaseIndex, i);
            attachments.add(attachment);
        }
        return attachments;
    }

    /**
     * Creates a test attachment from the given parameters.
     *
     * @param releaseIndex    the index of the test release
     * @param attachmentIndex the index of the test attachment
     * @return the test attachment
     */
    private static SW360SparseAttachment createAttachment(int releaseIndex, int attachmentIndex) {
        SW360SparseAttachment attachment = new SW360SparseAttachment();
        attachment.setAttachmentType(SW360AttachmentType.SOURCE);
        attachment.setFilename("release" + releaseIndex + "_source" + attachmentIndex + ".jar");
        return attachment;
    }

    /**
     * Creates a test release that contains the given set of attachments.
     * attachments.
     *
     * @param releaseIndex the index of the test release
     * @param attachments  the set with attachments
     * @return the test release
     */
    private static SW360Release createReleaseWithAttachments(int releaseIndex, Set<SW360SparseAttachment> attachments) {
        SW360Release release = new SW360Release();
        release.setName("release" + releaseIndex);
        SW360ReleaseEmbedded embedded = new SW360ReleaseEmbedded();
        release.setEmbedded(embedded);
        embedded.setAttachments(attachments);
        return release;
    }

    /**
     * Generates the path for the given attachment.
     *
     * @param attachment the attachment
     * @return the path where to download this attachment
     */
    private Path attachmentPath(SW360SparseAttachment attachment) {
        return sourcePath.resolve(attachment.getFilename());
    }

    /**
     * Creates a {@code ReleaseWithSources} object from the given data.
     *
     * @param release     the release
     * @param attachments the set with attachments
     * @return the resulting release with sources
     */
    private SourcesExporter.ReleaseWithSources createReleaseWithSources(SW360Release release,
                                                                        Set<SW360SparseAttachment> attachments) {
        Set<Path> paths = attachments.stream()
                .map(this::attachmentPath)
                .collect(Collectors.toSet());
        return new SourcesExporter.ReleaseWithSources(release, paths);
    }

    /**
     * Prepares the mock for release client to expect successful download
     * operations for the given set of attachments.
     *
     * @param release     the release affected
     * @param attachments the attachments to be downloaded
     */
    private void expectDownloads(SW360Release release, Set<SW360SparseAttachment> attachments) {
        attachments.forEach(attachment -> {
            Path path = attachmentPath(attachment);
            when(releaseAdapter.downloadAttachment(release, attachment, sourcePath))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(path)));
        });
    }

    @Test
    public void testEqualsReleaseWithSources() {
        EqualsVerifier.forClass(SourcesExporter.ReleaseWithSources.class)
                .verify();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReleaseWithSourcesAttachmentsUnmodifiable() {
        Set<SW360SparseAttachment> attachments = createAttachments(3, 8);
        SourcesExporter.ReleaseWithSources releaseWithSources =
                createReleaseWithSources(createReleaseWithAttachments(3, attachments), attachments);

        releaseWithSources.getSourceAttachmentPaths().clear();
    }

    @Test
    public void testAttachmentsAreDownloadedSuccessfully() {
        Set<SW360SparseAttachment> attachments1 = createAttachments(1, 1);
        Set<SW360SparseAttachment> attachments2 = createAttachments(2, 2);
        SW360Release release1 = createReleaseWithAttachments(1, attachments1);
        SW360Release release2 = createReleaseWithAttachments(2, attachments2);
        List<SW360Release> releases = Arrays.asList(release1, release2);
        List<SourcesExporter.ReleaseWithSources> expResult =
                Arrays.asList(createReleaseWithSources(release1, attachments1),
                        createReleaseWithSources(release2, attachments2));
        expectDownloads(release1, attachments1);
        expectDownloads(release2, attachments2);

        Collection<SourcesExporter.ReleaseWithSources> result =
                sourcesExporter.downloadSources(releaseAdapter, releases);
        assertThat(result).containsExactlyInAnyOrderElementsOf(expResult);
    }

    @Test
    public void testUnresolvableAttachmentsAreHandled() {
        SW360SparseAttachment attachment = createAttachment(1, 1);
        SW360Release release = createReleaseWithAttachments(1, Collections.singleton(attachment));
        when(releaseAdapter.downloadAttachment(release, attachment, sourcePath))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        Collection<SourcesExporter.ReleaseWithSources> result =
                sourcesExporter.downloadSources(releaseAdapter, Collections.singleton(release));
        assertThat(result).containsOnly(new SourcesExporter.ReleaseWithSources(release, Collections.emptySet()));
    }

    @Test
    public void testOnlySourceAttachmentsAreDownloaded() {
        Set<SW360SparseAttachment> sourceAttachments = createAttachments(1, 4);
        SW360SparseAttachment otherAttachment = createAttachment(1, 42);
        otherAttachment.setAttachmentType(SW360AttachmentType.SCREENSHOT);
        Set<SW360SparseAttachment> attachments = new HashSet<>(sourceAttachments);
        attachments.add(otherAttachment);
        SW360Release release = createReleaseWithAttachments(1, attachments);
        SourcesExporter.ReleaseWithSources releaseWithSources = createReleaseWithSources(release, sourceAttachments);
        expectDownloads(release, sourceAttachments);

        Collection<SourcesExporter.ReleaseWithSources> result =
                sourcesExporter.downloadSources(releaseAdapter, Collections.singleton(release));
        assertThat(result).containsOnly(releaseWithSources);
    }

    @Test
    public void testExceptionsDuringAttachmentDownloadAreHandled() {
        Set<SW360SparseAttachment> sourceAttachments = createAttachments(1, 2);
        SW360SparseAttachment failedAttachment = createAttachment(1, 42);
        Set<SW360SparseAttachment> attachments = new HashSet<>(sourceAttachments);
        attachments.add(failedAttachment);
        SW360Release release = createReleaseWithAttachments(1, attachments);
        SourcesExporter.ReleaseWithSources releaseWithSources = createReleaseWithSources(release, sourceAttachments);
        expectDownloads(release, sourceAttachments);
        when(releaseAdapter.downloadAttachment(release, failedAttachment, sourcePath))
                .thenReturn(failedFuture(new IOException("Download failed")));

        Collection<SourcesExporter.ReleaseWithSources> result =
                sourcesExporter.downloadSources(releaseAdapter, Collections.singleton(release));
        assertThat(result).containsOnly(releaseWithSources);
    }

    /**
     * Creates a test file at the given location.
     *
     * @param path the path of the test file
     * @return the path to the file that was created
     */
    private static Path createTestFile(Path path) {
        try {
            return Files.write(path, "This is a test file".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AssertionError("Could not create test file " + path);
        }
    }

    /**
     * Creates files for all the attachments of the given release.
     *
     * @param releaseWithSources the release
     */
    private static void createAttachmentFiles(SourcesExporter.ReleaseWithSources releaseWithSources) {
        releaseWithSources.getSourceAttachmentPaths()
                .forEach(SourcesExporterTest::createTestFile);
    }

    /**
     * Checks that the given file exists.
     *
     * @param path the path to be checked
     * @return a flag whether this file exists
     */
    private static boolean checkFileExists(Path path) {
        return Files.exists(path);
    }

    /**
     * Checks that all the attachment files referenced by the given release
     * exist on the local disk.
     *
     * @param release the release to be checked
     */
    private static void checkAllAttachmentsExist(SourcesExporter.ReleaseWithSources release) {
        assertThat(release.getSourceAttachmentPaths().stream().allMatch(SourcesExporterTest::checkFileExists))
                .isTrue();
    }

    @Test
    public void testUnreferencedFilesCanBeRemoved() {
        Set<SW360SparseAttachment> attachments1 = createAttachments(1, 2);
        Set<SW360SparseAttachment> attachments2 = createAttachments(2, 3);
        SourcesExporter.ReleaseWithSources release1 =
                createReleaseWithSources(createReleaseWithAttachments(1, attachments1), attachments1);
        SourcesExporter.ReleaseWithSources release2 =
                createReleaseWithSources(createReleaseWithAttachments(2, attachments2), attachments2);
        Path unreferencedPath = attachmentPath(createAttachment(17, 47));
        createTestFile(unreferencedPath);
        createAttachmentFiles(release1);
        createAttachmentFiles(release2);

        sourcesExporter.removeUnreferencedFiles(Arrays.asList(release1, release2));
        checkAllAttachmentsExist(release1);
        checkAllAttachmentsExist(release2);
        assertThat(checkFileExists(unreferencedPath)).isFalse();
    }

    @Test
    public void testIOExceptionIsHandledWhenRemovingUnreferencedFiles() {
        Path nonExisting = sourcePath.resolve("non/existing/sub/folder");
        sourcesExporter = new SourcesExporter(nonExisting);

        sourcesExporter.removeUnreferencedFiles(Collections.emptyList());
    }

    @Test
    public void testIOExceptionDuringSingleFileRemoveOperationIsIgnored() throws IOException {
        Path subFolder = Files.createDirectory(sourcePath.resolve("sub"));
        Path subFile = createTestFile(subFolder.resolve("test.txt"));
        Path file1 = createTestFile(sourcePath.resolve("file1.txt"));
        Path file2 = createTestFile(sourcePath.resolve("ultimateFile.txt"));

        sourcesExporter.removeUnreferencedFiles(Collections.emptyList());
        assertThat(checkFileExists(file1)).isFalse();
        assertThat(checkFileExists(file2)).isFalse();
        assertThat(checkFileExists(subFile)).isTrue();
    }
}
