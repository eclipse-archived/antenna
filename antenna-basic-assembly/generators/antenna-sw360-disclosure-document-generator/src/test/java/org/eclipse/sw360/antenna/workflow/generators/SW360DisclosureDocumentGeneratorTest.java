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

package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.GenericArtifactCoordinates;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SW360DisclosureDocumentGeneratorTest {
    private SW360DisclosureDocumentGenerator sw360DisclosureDocumentGenerator;
    @Before
    public void setup() {
        sw360DisclosureDocumentGenerator = new SW360DisclosureDocumentGenerator();
    }

    @Test
    public void getBestArtifactCoordinatesTestForEmpty() {
        final Optional<ArtifactCoordinates> bestArtifactCoordinates = sw360DisclosureDocumentGenerator.getBestArtifactCoordinates(new ArrayList<>());
        assertFalse(bestArtifactCoordinates.isPresent());
    }

    @Test
    public void getBestArtifactCoordinatesTestForSingleton() {
        final ArtifactCoordinates artifactCoordinates = new GenericArtifactCoordinates("Name", "Version");
        final Optional<ArtifactCoordinates> bestArtifactCoordinates = sw360DisclosureDocumentGenerator.getBestArtifactCoordinates(Collections.singleton(artifactCoordinates));
        assertTrue(bestArtifactCoordinates.isPresent());
        assertEquals(bestArtifactCoordinates.get(), artifactCoordinates);
    }

    @Test
    public void getBestArtifactCoordinatesTestForList() {
        final ArtifactCoordinates artifactCoordinates1 = new GenericArtifactCoordinates("Name", "Version");
        final ArtifactCoordinates artifactCoordinates2 = new GenericArtifactCoordinates("Name", "Version2");
        final ArtifactCoordinates artifactCoordinates3 = new GenericArtifactCoordinates(null, "Version2");
        final ArtifactCoordinates artifactCoordinates4 = new GenericArtifactCoordinates("Name", null);
        final ArtifactCoordinates artifactCoordinates5 = new GenericArtifactCoordinates(null, null);

        final Set<ArtifactCoordinates> coordinates = Stream.of(
                artifactCoordinates5,
                artifactCoordinates1,
                artifactCoordinates4,
                artifactCoordinates2,
                artifactCoordinates3).collect(Collectors.toSet());

        final Optional<ArtifactCoordinates> bestArtifactCoordinates = sw360DisclosureDocumentGenerator.getBestArtifactCoordinates(coordinates);
        assertTrue(bestArtifactCoordinates.isPresent());
        assertTrue(bestArtifactCoordinates.get().equals(artifactCoordinates1) ||
                bestArtifactCoordinates.get().equals(artifactCoordinates2));
    }

    @Test
    public void getBestArtifactCoordinatesTestForListWithSuboptimal() {
        final ArtifactCoordinates artifactCoordinates1 = new GenericArtifactCoordinates("Name", null);
        final ArtifactCoordinates artifactCoordinates2 = new GenericArtifactCoordinates("Name2", null);
        final ArtifactCoordinates artifactCoordinates3 = new GenericArtifactCoordinates(null, "Version2");
        final ArtifactCoordinates artifactCoordinates4 = new GenericArtifactCoordinates("Name3", null);
        final ArtifactCoordinates artifactCoordinates5 = new GenericArtifactCoordinates(null, null);

        final Set<ArtifactCoordinates> coordinates = Stream.of(
                artifactCoordinates5,
                artifactCoordinates1,
                artifactCoordinates4,
                artifactCoordinates2,
                artifactCoordinates3).collect(Collectors.toSet());

        final Optional<ArtifactCoordinates> bestArtifactCoordinates = sw360DisclosureDocumentGenerator.getBestArtifactCoordinates(coordinates);
        assertTrue(bestArtifactCoordinates.isPresent());
        assertTrue(bestArtifactCoordinates.get().equals(artifactCoordinates1) ||
                bestArtifactCoordinates.get().equals(artifactCoordinates2) ||
                bestArtifactCoordinates.get().equals(artifactCoordinates4));
    }

    @Test
    public void getBestArtifactCoordinatesTestForListWithVerySuboptimal() {
        final ArtifactCoordinates artifactCoordinates1 = new GenericArtifactCoordinates(null, "Version2");
        final ArtifactCoordinates artifactCoordinates2 = new GenericArtifactCoordinates(null, null);

        final Set<ArtifactCoordinates> coordinates = Stream.of(
                artifactCoordinates1,
                artifactCoordinates2).collect(Collectors.toSet());

        final Optional<ArtifactCoordinates> bestArtifactCoordinates = sw360DisclosureDocumentGenerator.getBestArtifactCoordinates(coordinates);
        assertTrue(bestArtifactCoordinates.isPresent());
        assertEquals(bestArtifactCoordinates.get(), artifactCoordinates1);
    }
}
