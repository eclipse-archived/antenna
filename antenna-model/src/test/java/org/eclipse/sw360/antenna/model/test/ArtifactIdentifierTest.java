/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ArtifactIdentifierTest {

    @Test
    public void testThatFilenameMatches() {
        ArtifactIdentifier artifactIdentifier = new ArtifactFilename("someFilename");
        ArtifactIdentifier artifactIdentifierInArtifact = new ArtifactFilename("someFilename");

        Artifact artifact = new Artifact();
        artifact.addFact(artifactIdentifierInArtifact);

        assertThat(artifactIdentifier.matches(artifact)).isTrue();
    }

    @Test
    public void testThatFilenameMatchesNotEverything() {
        ArtifactIdentifier artifactIdentifier = new ArtifactFilename("someOtherFilename");
        ArtifactIdentifier artifactIdentifierInArtifact = new ArtifactFilename("someFilename");

        Artifact artifact = new Artifact();
        artifact.addFact(artifactIdentifierInArtifact);

        assertThat(artifactIdentifier.matches(artifact)).isFalse();
    }

    @Test
    public void testThatFilenameMatchesPathnames() {
        ArtifactIdentifier artifactIdentifier = new ArtifactFilename("someFilename");
        ArtifactIdentifier artifactIdentifierInArtifact = new ArtifactPathnames("path/to/someFilename", "path/to/some/otherFilename");

        Artifact artifact = new Artifact();
        artifact.addFact(artifactIdentifierInArtifact);

        assertThat(artifactIdentifier.matches(artifact)).isTrue();
    }

    @Test
    public void testThatFilenameMatchesNotEveryPathnames() {
        ArtifactIdentifier artifactIdentifier = new ArtifactFilename("someThirdFilename");
        ArtifactIdentifier artifactIdentifierInArtifact = new ArtifactPathnames("path/to/someFilename", "path/to/some/otherFilename");

        Artifact artifact = new Artifact();
        artifact.addFact(artifactIdentifierInArtifact);

        assertThat(artifactIdentifier.matches(artifact)).isFalse();
    }
}
