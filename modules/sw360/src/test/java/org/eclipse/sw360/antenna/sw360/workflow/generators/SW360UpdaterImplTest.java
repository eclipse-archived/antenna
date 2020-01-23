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
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseLinkObjects;
import org.eclipse.sw360.antenna.sw360.utils.TestUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;

public class SW360UpdaterImplTest {
    private SW360MetaDataUpdater metaDataUpdater = mock(SW360MetaDataUpdater.class);

    @Test
    public void testUpdaterProduce() {
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version");

        Set<Artifact> artifacts = Collections.singleton(TestUtils.mkArtifact("test", true));

        updater.produce(artifacts);

        verify(metaDataUpdater, atLeast(1)).createProject(eq("test"), eq("version"), any());
    }

    @Test
    public void testUpdaterHandleSources() {
        SW360UpdaterImpl updater = new SW360UpdaterImpl(metaDataUpdater, "test", "version");

        when(metaDataUpdater.isUploadSources())
                .thenReturn(true);

        Artifact artifact = TestUtils.mkArtifact("test", false);
        SW360Release release = TestUtils.mkSW360Release("test");
        Self self = new Self("http://localhost:8080/releases/12345");
        SW360ReleaseLinkObjects linkObjectsWithSelf = new SW360ReleaseLinkObjects();
        linkObjectsWithSelf.setSelf(self);
        release.set_Links(linkObjectsWithSelf);

        updater.handleSources(release, artifact);
    }
}
