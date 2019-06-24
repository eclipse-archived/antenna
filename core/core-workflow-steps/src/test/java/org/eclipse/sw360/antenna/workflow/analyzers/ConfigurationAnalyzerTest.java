/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.analyzers;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ConfigurationAnalyzerTest extends AntennaTestWithMockedContext {
    private final MatchState artifactMatchState;
    private static final String FILENAME = "some/path";

    @Parameterized.Parameters(name = "{index}: artifact={0}")
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {{null}, {MatchState.SIMILAR}, {MatchState.EXACT}});
    }

    public ConfigurationAnalyzerTest(MatchState artifactMatchState) {
        this.artifactMatchState = artifactMatchState;
    }

    @Before
    public void setupMock() {
        Artifact specialArtifact = new Artifact();
        specialArtifact.addFact(new ArtifactFilename(FILENAME));

        Artifact otherArtifact = new Artifact();
        otherArtifact.addFact(new BundleCoordinates("otherArtifact", "1.2"));
        otherArtifact.addFact(new ArtifactFilename("otherArtifact"));

        if(artifactMatchState != null){
            specialArtifact.addFact(new ArtifactMatchingMetadata(artifactMatchState));
        }

        List<Artifact> artifactsToAdd = Arrays.asList(specialArtifact, otherArtifact);

        when(configMock.getAddArtifact())
                .thenReturn(artifactsToAdd);
    }

    @After
    public void after() {
        verify(configMock, atLeast(0)).getAddArtifact();
        verify(reporterMock, atLeast(0)).add(any(), any());
        verify(reporterMock, atLeast(0)).add(any(Artifact.class), any(), any());
    }

    @Test
    public void testAddArtifact() {
        ConfigurationAnalyzer handler = new ConfigurationAnalyzer();
        handler.setAntennaContext(antennaContextMock);

        Set<Artifact> artifacts = handler.yield().getArtifacts();

        assertThat(artifacts.size()).isEqualTo(2);
        Artifact processedArtifact = artifacts.stream()
                .filter(a -> {
                    final Optional<ArtifactFilename> artifactFilename = a.askFor(ArtifactFilename.class);
                    return artifactFilename.isPresent() && FILENAME.equals(artifactFilename.get().getBestFilenameGuess().orElse(null));
                })
                .findAny()
                .orElseThrow(() -> new RuntimeException("should not happen"));
        assertThat(processedArtifact.askFor(ArtifactMatchingMetadata.class)
                .map(ArtifactMatchingMetadata::getMatchState)
                .orElse(null))
                .isEqualTo(Optional.ofNullable(artifactMatchState).orElse(null));
    }
}
