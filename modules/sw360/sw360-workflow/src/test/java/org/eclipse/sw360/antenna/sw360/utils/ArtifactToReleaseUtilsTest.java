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
package org.eclipse.sw360.antenna.sw360.utils;

import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactToReleaseUtilsTest {
    @Test
    public void testRoundTripReleaseArtifactRelease () {
        SW360Release release = TestUtils.mkSW360Release("release");
        Artifact artifact = ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(release, new Artifact());
        SW360Release roundTripRelease = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);

        Assertions.assertThat(roundTripRelease).isEqualTo(release);
    }

    @Test
    public void testRoundTripArtifactReleaseArtifact() {
        Artifact artifact = TestUtils.mkArtifact("artifact", false);
        SW360Release releaseFromArtifact = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);
        Artifact artifactFromRelease = ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(releaseFromArtifact, new Artifact("SW360"));

        assertThat(artifactFromRelease.askForAll(ArtifactFact.class)).containsAll(artifact.askForAll(ArtifactFact.class));
        assertThat(artifactFromRelease).isEqualTo(artifact);
    }
}
