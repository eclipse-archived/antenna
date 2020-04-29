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
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToAttachmentUtils;
import org.eclipse.sw360.antenna.sw360.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SW360UpdaterImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private SW360MetaDataUpdater metaDataUpdater = mock(SW360MetaDataUpdater.class);

    @Test
    public void testUpdaterProduce() {
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version");

        Set<Artifact> artifacts = Collections.singleton(TestUtils.mkArtifact("test", true));

        updater.produce(artifacts);

        verify(metaDataUpdater, atLeast(1)).createProject(eq("test"), eq("version"), any());
    }

    @Test
    public void testUpdaterHandleSources() throws IOException {
        Artifact artifact = TestUtils.mkArtifact("test", false);
        File attachmentFile = temporaryFolder.newFile("test.attachment");
        Path sourceFile = attachmentFile.toPath();
        artifact.addFact(new ArtifactSourceFile(sourceFile));
        SW360Release release = TestUtils.mkSW360Release("test");
        Self self = new Self("http://localhost:8080/releases/12345");
        SW360ReleaseLinkObjects linkObjectsWithSelf = new SW360ReleaseLinkObjects();
        linkObjectsWithSelf.setSelf(self);
        release.set_Links(linkObjectsWithSelf);

        Map<Path, SW360AttachmentType> attachmentsFromArtifact = ArtifactToAttachmentUtils.getAttachmentsFromArtifact(artifact);

        Set<SW360SparseAttachment> sparseAttachments = attachmentsFromArtifact.entrySet().stream()
                .map(entry -> new SW360SparseAttachment()
                        .setAttachmentType(entry.getValue())
                        .setFilename(entry.getKey().toString()))
                .collect(Collectors.toSet());

        SW360ReleaseEmbedded sw360ReleaseEmbedded = new SW360ReleaseEmbedded();
        sw360ReleaseEmbedded.setAttachments(sparseAttachments);

        SW360Release releaseWithAttachment = release;
        releaseWithAttachment.set_Embedded(sw360ReleaseEmbedded);

        when(metaDataUpdater.isUploadSources())
                .thenReturn(true);
        when(metaDataUpdater.uploadAttachments(release, attachmentsFromArtifact))
                .thenReturn(releaseWithAttachment);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version");

        SW360Release releaseWithSources = updater.handleSources(release, artifact);

        assertThat(releaseWithAttachment).isEqualTo(releaseWithSources);

        verify(metaDataUpdater, atLeastOnce()).uploadAttachments(release, attachmentsFromArtifact);
    }
}
