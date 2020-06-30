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
package org.eclipse.sw360.antenna.sw360.workflow.generators;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseLinkObjects;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SW360UpdaterImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final SW360MetaDataUpdater metaDataUpdater = mock(SW360MetaDataUpdater.class);

    private static SW360Release createRelease(String name, Path sourceFile) {
        SW360Release release = TestUtils.mkSW360Release(name);
        Self self = new Self("http://localhost:8080/releases/12345");
        SW360ReleaseLinkObjects linkObjectsWithSelf = new SW360ReleaseLinkObjects();
        linkObjectsWithSelf.setSelf(self);
        release.setLinks(linkObjectsWithSelf);

        SW360SparseAttachment attachment = new SW360SparseAttachment()
                .setAttachmentType(SW360AttachmentType.SOURCE)
                .setFilename(sourceFile.toString());
        Set<SW360SparseAttachment> sparseAttachments = Collections.singleton(attachment);
        SW360ReleaseEmbedded sw360ReleaseEmbedded = new SW360ReleaseEmbedded();
        sw360ReleaseEmbedded.setAttachments(sparseAttachments);
        release.setEmbedded(sw360ReleaseEmbedded);
        return release;
    }

    private Path createTestFile() throws IOException {
        return temporaryFolder.newFile("test.attachment").toPath();
    }

    private void checkUpdaterProduce(boolean updateReleases) {
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                updateReleases, false, false);

        Set<Artifact> artifacts = Collections.singleton(TestUtils.mkArtifact("test", true));

        updater.produce(artifacts);

        verify(metaDataUpdater).getOrCreateRelease(any(), eq(updateReleases));
        verify(metaDataUpdater, atLeast(1)).createProject(eq("test"), eq("version"), any());
    }

    @Test
    public void testUpdaterProduceNoUpdateOfReleases() {
        checkUpdaterProduce(false);
    }

    @Test
    public void testUpdaterProduceWithUpdateOfReleases() {
        checkUpdaterProduce(true);
    }

    @Test
    public void testUpdaterHandleSourceArtifact() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);

        when(metaDataUpdater.uploadAttachments(release, uploadMap))
                .thenReturn(updatedRelease);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, true, false);

        SW360Release releaseWithSources = updater.handleSourceArtifact(release, artifact);

        assertThat(releaseWithSources).isEqualTo(updatedRelease);

        verify(metaDataUpdater, atLeastOnce()).uploadAttachments(release, uploadMap);
        verify(metaDataUpdater, never()).deleteSourceAttachments(any());
    }

    @Test
    public void testUpdaterHandleSourceArtifactUploadDisabled() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, false, true);

        SW360Release releaseWithSources = updater.handleSourceArtifact(release, artifact);

        assertThat(releaseWithSources).isEqualTo(release);
        verifyZeroInteractions(metaDataUpdater);
    }

    @Test
    public void testUpdaterHandleSourceArtifactDeleteSources() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release srcDeletedRelease = createRelease("testAttachmentsDeleted", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);

        when(metaDataUpdater.deleteSourceAttachments(release)).thenReturn(srcDeletedRelease);
        when(metaDataUpdater.uploadAttachments(srcDeletedRelease, uploadMap))
                .thenReturn(updatedRelease);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, true, true);

        SW360Release releaseWithSources = updater.handleSourceArtifact(release, artifact);

        assertThat(releaseWithSources).isEqualTo(updatedRelease);

        InOrder inOrder = Mockito.inOrder(metaDataUpdater);
        inOrder.verify(metaDataUpdater).deleteSourceAttachments(release);
        inOrder.verify(metaDataUpdater).uploadAttachments(srcDeletedRelease, uploadMap);
    }

    @Test
    public void testUpdaterHandleSourceArtifactDeleteFailure() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);

        when(metaDataUpdater.deleteSourceAttachments(release))
                .thenThrow(new SW360ClientException("Delete operation failed"));
        when(metaDataUpdater.uploadAttachments(release, uploadMap))
                .thenReturn(updatedRelease);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, true, true);

        SW360Release releaseWithSources = updater.handleSourceArtifact(release, artifact);

        assertThat(releaseWithSources).isEqualTo(updatedRelease);
    }
}
