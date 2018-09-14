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
package org.eclipse.sw360.antenna.analysis.filter;

import org.eclipse.sw360.antenna.model.*;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.junit.Test;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;

public class FilterTest {

    @Test
    public void proprietaryArtifactFilterTest() {
        Artifact artifactProprietaryTestTrue = new Artifact();
        artifactProprietaryTestTrue.setProprietary(true);
        Artifact artifactProprietaryTestFalse = new Artifact();
        artifactProprietaryTestFalse.setProprietary(false);
        ProprietaryArtifactFilter proprietaryFilter = new ProprietaryArtifactFilter();
        assertThat(proprietaryFilter.passed(artifactProprietaryTestFalse)).isEqualTo(true);
        assertThat(proprietaryFilter.passed(artifactProprietaryTestTrue)).isEqualTo(false);
    }

    @Test
    public void matchStateArtifactFilterTest() {
        Artifact artifactMatchStateTestSimilar = new Artifact();
        artifactMatchStateTestSimilar.setMatchState(MatchState.SIMILAR);
        Artifact artifactMatchStateTestUnknown = new Artifact();
        artifactMatchStateTestUnknown.setMatchState(MatchState.UNKNOWN);
        Artifact artifactMatchStateTestExact = new Artifact();
        artifactMatchStateTestExact.setMatchState(MatchState.EXACT);

        Set<MatchState> blacklist = new HashSet<>();
        blacklist.add(MatchState.UNKNOWN);
        blacklist.add(MatchState.SIMILAR);
        MatchStateArtifactFilter filter = new MatchStateArtifactFilter(blacklist);

        assertThat(filter.passed(artifactMatchStateTestExact)).isEqualTo(true);
        assertThat(filter.passed(artifactMatchStateTestSimilar)).isEqualTo(false);
        assertThat(filter.passed(artifactMatchStateTestUnknown)).isEqualTo(false);
    }

    @Test
    public void allowAllArtifactsFilterTest() {
        Artifact artifact = new Artifact();
        AllowAllArtifactsFilter filter = new AllowAllArtifactsFilter();
        assertThat(filter.passed(artifact)).isTrue();
    }

    @Test
    public void ConfigurationFilterTest1() {
        List<ArtifactSelector> examplesList = Collections.EMPTY_LIST;
        BlacklistFilter filterEmpty = new BlacklistFilter(examplesList);
        Artifact example = new Artifact();
        assertThat(filterEmpty.passed(example)).isTrue();
    }


    @Test
    public void ConfigurationFilterTest2() {
        ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier();
        artifactIdentifier.setFilename("testFilename");
        ArtifactSelector example = new ArtifactSelector(artifactIdentifier);
        List<ArtifactSelector> examplesList = Collections.singletonList(example);
        BlacklistFilter filter = new BlacklistFilter(examplesList);

        Artifact artifact = new Artifact();
        artifact.getArtifactIdentifier().setFilename("testFilename");

        assertThat(filter.passed(artifact)).isFalse();
    }


    @Test
    public void ConfigurationFilterTest3() {
        ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier();
        artifactIdentifier.setFilename("test");
        ArtifactSelector example = new ArtifactSelector(artifactIdentifier);
        List<ArtifactSelector> examplesList = Collections.singletonList(example);
        BlacklistFilter filter = new BlacklistFilter(examplesList);

        Artifact artifact = new Artifact();
        artifact.getArtifactIdentifier().setFilename("testFilename");

        assertThat(filter.passed(artifact)).isTrue();
    }
}
