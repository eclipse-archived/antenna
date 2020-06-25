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
import org.eclipse.sw360.antenna.sw360.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360UpdaterImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final SW360MetaDataUpdater metaDataUpdater = mock(SW360MetaDataUpdater.class);

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
        release.setLinks(linkObjectsWithSelf);

        SW360SparseAttachment attachment = new SW360SparseAttachment()
                .setAttachmentType(SW360AttachmentType.SOURCE)
                .setFilename(sourceFile.toString());
        Set<SW360SparseAttachment> sparseAttachments = Collections.singleton(attachment);
        Map<Path, SW360AttachmentType> uploadMap = Collections.singletonMap(sourceFile, SW360AttachmentType.SOURCE);

        SW360ReleaseEmbedded sw360ReleaseEmbedded = new SW360ReleaseEmbedded();
        sw360ReleaseEmbedded.setAttachments(sparseAttachments);

        release.setEmbedded(sw360ReleaseEmbedded);

        when(metaDataUpdater.isUploadSources())
                .thenReturn(true);
        when(metaDataUpdater.uploadAttachments(release, uploadMap))
                .thenReturn(release);

        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version");

        SW360Release releaseWithSources = updater.handleSources(release, artifact);

        assertThat(release).isEqualTo(releaseWithSources);

        verify(metaDataUpdater, atLeastOnce()).uploadAttachments(release, uploadMap);
    }
}
