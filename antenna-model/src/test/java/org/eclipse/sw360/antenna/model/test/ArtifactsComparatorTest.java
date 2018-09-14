/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.util.ArtifactsComparator;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ArtifactsComparatorTest {

    @Test
    public void test() {
        Artifact artifact = new Artifact();
        artifact.getArtifactIdentifier().setFilename("filename");
        Artifact compareArtifact = new Artifact();
        compareArtifact.getArtifactIdentifier().setFilename("compareFilename");
        ArtifactsComparator comparator = new ArtifactsComparator();
        assertThat(comparator.compare(artifact, artifact)).isEqualTo(0);
    }
}
