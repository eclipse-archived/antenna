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
import org.eclipse.sw360.antenna.model.artifact.facts.ConfiguredLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ReleaseLinkObjects;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360UpdaterImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final SW360MetaDataUpdater metaDataUpdater = mock(SW360MetaDataUpdater.class);

    private static SW360Release createRelease(String name, Path sourceFile) {
        SW360Release release = TestUtils.mkSW360Release(name);
        Self self = new Self("http://localhost:8080/releases/" + System.identityHashCode(release));
        SW360ReleaseLinkObjects linkObjectsWithSelf = new SW360ReleaseLinkObjects();
        linkObjectsWithSelf.setSelf(self);
        release.setLinks(linkObjectsWithSelf);
        SW360ReleaseEmbedded sw360ReleaseEmbedded = new SW360ReleaseEmbedded();

        if (sourceFile != null) {
            SW360SparseAttachment attachment = createAttachment(sourceFile, SW360AttachmentType.SOURCE);
            Set<SW360SparseAttachment> sparseAttachments = Collections.singleton(attachment);
            sw360ReleaseEmbedded.setAttachments(sparseAttachments);
        }
        release.setEmbedded(sw360ReleaseEmbedded);
        return release;
    }

    private static SW360SparseAttachment createAttachment(Path sourceFile, SW360AttachmentType type) {
        return new SW360SparseAttachment()
                .setAttachmentType(type)
                .setFilename(sourceFile.getFileName().toString());
    }

    private Path createTestFile() throws IOException {
        return temporaryFolder.newFile().toPath();
    }

    private void checkUpdaterProduce(boolean updateReleases) {
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                updateReleases, false, false);

        Set<Artifact> artifacts = Collections.singleton(TestUtils.mkArtifact("test", true));

        updater.produce(artifacts);

        verify(metaDataUpdater).getOrCreateRelease(any(), eq(updateReleases), eq(false));
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
    public void testUpdaterArtifactToReleaseWithUploadsNoUploads() {
        Artifact artifact = TestUtils.mkArtifact("test", false);
        License license = new License("testLicense");
        SW360License sw360License = new SW360License();
        sw360License.setShortName("sw360TestLicense");
        artifact.addFact(new ConfiguredLicenseInformation(license));
        SW360Release release = createRelease("test", null);
        SW360Release updatedRelease = createRelease("testUpdated", null);
        when(metaDataUpdater.getLicenses(Collections.singletonList(license)))
                .thenReturn(Collections.singleton(sw360License));
        when(metaDataUpdater.getOrCreateRelease(release, true))
                .thenReturn(updatedRelease);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                true, true, false);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, Collections.emptyMap());
        assertThat(result.getTarget()).isEqualTo(updatedRelease);
        assertThat(release.getMainLicenseIds()).containsOnly(sw360License.getShortName());
        verify(metaDataUpdater, never()).uploadAttachments(any(), any(), anyBoolean());
    }

    @Test
    public void testUpdaterArtifactToReleaseHandleSourceArtifact() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release createdRelease = createRelease("testCreated", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);
        AttachmentUploadResult<SW360Release> uploadResult = new AttachmentUploadResult<>(updatedRelease);

        when(metaDataUpdater.getOrCreateRelease(release, false))
                .thenReturn(createdRelease);
        when(metaDataUpdater.uploadAttachments(createdRelease, uploadMap, false))
                .thenReturn(uploadResult);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, true, false);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, Collections.emptyMap());
        assertThat(result).isEqualTo(uploadResult);
        verify(metaDataUpdater, never()).deleteSourceAttachments(any());
    }

    @Test
    public void testUpdaterArtifactToReleaseUploadDisabled() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release createdRelease = createRelease("testCreated", sourceFile);
        when(metaDataUpdater.getOrCreateRelease(release, true))
                .thenReturn(createdRelease);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                true, false, false);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, Collections.emptyMap());

        assertThat(result.getTarget()).isEqualTo(createdRelease);
        verify(metaDataUpdater, never()).uploadAttachments(any(), any(), anyBoolean());
    }

    @Test
    public void testUpdaterArtifactToReleaseAdditionalUpload() throws IOException {
        Path uploadPath = createTestFile();
        Map<Path, SW360AttachmentType> uploadMap =
                Collections.singletonMap(uploadPath, SW360AttachmentType.CLEARING_REPORT);
        Artifact artifact = TestUtils.mkArtifact("test", false);
        SW360Release release = createRelease("test", null);
        SW360Release createdRelease = createRelease("testCreated", null);
        SW360Release uploadedRelease = createRelease("uploadedRelease", null);
        AttachmentUploadResult<SW360Release> uploadResult = new AttachmentUploadResult<>(uploadedRelease);
        when(metaDataUpdater.getOrCreateRelease(release, false))
                .thenReturn(createdRelease);
        when(metaDataUpdater.uploadAttachments(createdRelease, uploadMap, false))
                .thenReturn(uploadResult);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, false, false);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, uploadMap);
        assertThat(result).isEqualTo(uploadResult);
    }

    @Test
    public void testUpdaterArtifactToReleaseWithSourceAndAdditionalUpload() throws IOException {
        Path sourceFile = createTestFile();
        Path uploadFile = createTestFile();
        Map<Path, SW360AttachmentType> uploadMap =
                Collections.singletonMap(uploadFile, SW360AttachmentType.CLEARING_REPORT);
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release createdRelease = createRelease("testCreated", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        AttachmentUploadResult<SW360Release> uploadResult = new AttachmentUploadResult<>(updatedRelease);
        Map<Path, SW360AttachmentType> expUploads = new HashMap<>();
        expUploads.put(sourceFile, SW360AttachmentType.SOURCE);
        expUploads.put(uploadFile, SW360AttachmentType.CLEARING_REPORT);

        when(metaDataUpdater.getOrCreateRelease(release, false))
                .thenReturn(createdRelease);
        when(metaDataUpdater.uploadAttachments(createdRelease, expUploads, false))
                .thenReturn(uploadResult);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, true, false);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, uploadMap);
        assertThat(result).isEqualTo(uploadResult);
    }

    private Predicate<SW360SparseAttachment> extractPredicateForDeleteAttachments(SW360Release createdRelease) {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Predicate<SW360SparseAttachment>> captor = ArgumentCaptor.forClass(Predicate.class);
        verify(metaDataUpdater).deleteAttachments(eq(createdRelease), captor.capture());
        return captor.getValue();
    }

    @Test
    public void testUpdaterArtifactToReleaseDeleteSourcesNoSource() {
        Artifact artifact = TestUtils.mkArtifact("test", false);
        SW360Release release = createRelease("test", null);
        SW360Release createdRelease = createRelease("testCreated", null);
        SW360Release deletedRelease = createRelease("releaseNoAttachments", null);
        when(metaDataUpdater.getOrCreateRelease(release, false))
                .thenReturn(createdRelease);
        when(metaDataUpdater.deleteAttachments(eq(createdRelease), any()))
                .thenReturn(deletedRelease);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, false, true);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, Collections.emptyMap());
        assertThat(result.getTarget()).isEqualTo(deletedRelease);
        Predicate<SW360SparseAttachment> predicate = extractPredicateForDeleteAttachments(createdRelease);
        assertThat(predicate.test(createAttachment(Paths.get("foo"), SW360AttachmentType.SOURCE)))
                .isTrue();
        assertThat(predicate.test(createAttachment(Paths.get("foo"), SW360AttachmentType.BINARY)))
                .isFalse();
    }

    @Test
    public void testUpdaterArtifactToReleaseDeleteSources() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release createdRelease = createRelease("testCreated", sourceFile);
        SW360Release srcDeletedRelease = createRelease("testAttachmentsDeleted", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);
        AttachmentUploadResult<SW360Release> uploadResult = new AttachmentUploadResult<>(updatedRelease);

        when(metaDataUpdater.getOrCreateRelease(release, false))
                .thenReturn(createdRelease);
        when(metaDataUpdater.deleteAttachments(eq(createdRelease), any())).thenReturn(srcDeletedRelease);
        when(metaDataUpdater.uploadAttachments(srcDeletedRelease, uploadMap, true))
                .thenReturn(uploadResult);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                false, true, true);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, uploadMap);
        assertThat(result).isEqualTo(uploadResult);
        Predicate<SW360SparseAttachment> predicate = extractPredicateForDeleteAttachments(createdRelease);
        assertThat(predicate.test(createAttachment(Paths.get("foo"), SW360AttachmentType.SOURCE)))
                .isTrue();
        assertThat(predicate.test(createAttachment(sourceFile, SW360AttachmentType.SOURCE)))
                .isFalse();
    }

    @Test
    public void testUpdaterArtifactToReleaseDeleteFailure() throws IOException {
        Path sourceFile = createTestFile();
        Artifact artifact = TestUtils.mkArtifact("test", false);
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = createRelease("test", sourceFile);
        SW360Release createdRelease = createRelease("testCreated", sourceFile);
        SW360Release updatedRelease = createRelease("testUpdated", sourceFile);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);
        AttachmentUploadResult<SW360Release> uploadResult = new AttachmentUploadResult<>(updatedRelease);

        when(metaDataUpdater.getOrCreateRelease(release, true)).thenReturn(createdRelease);
        when(metaDataUpdater.deleteAttachments(any(), any()))
                .thenThrow(new SW360ClientException("Delete operation failed"));
        when(metaDataUpdater.uploadAttachments(createdRelease, uploadMap, true))
                .thenReturn(uploadResult);
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version",
                true, true, true);

        AttachmentUploadResult<SW360Release> result =
                updater.artifactToReleaseWithUploads(artifact, release, Collections.emptyMap());
        assertThat(result).isEqualTo(uploadResult);
    }
}
