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
package org.eclipse.sw360.antenna.workflow.processors.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.*;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ConfigurationHandlerAddTest extends CommonConfigurationHandlerTest {

    @Parameterized.Parameters(name = "{index}: artifact={0}")
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {{null}, {MatchState.SIMILAR}, {MatchState.EXACT}});
    }

    public ConfigurationHandlerAddTest(MatchState artifactMatchState) {
        super(artifactMatchState);
    }

    @Test
    public void testAddArtifact() {
        List<Artifact> artifactsToAdd = Arrays.asList(specialArtifact, generateDummyArtifact("otherArtifactToAdd"));

        when(configMock.getAddArtifact())
                .thenReturn(artifactsToAdd);

        ConfigurationHandlerAdd handler = new ConfigurationHandlerAdd(antennaContextMock);
        handler.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(5);
        Artifact processedArtifact = artifacts.stream()
                .filter(a -> {
                    final Optional<ArtifactFilename> artifactFilename = a.askFor(ArtifactFilename.class);
                    return artifactFilename.isPresent() && FILENAME.equals(artifactFilename.get().getFilename());
                })
                .findAny()
                .orElseThrow(() -> new RuntimeException("should not happen"));
        assertThat(processedArtifact.askFor(ArtifactMatchingMetadata.class)
                .map(ArtifactMatchingMetadata::getMatchState)
                .orElse(null))
                .isEqualTo(Optional.ofNullable(artifactMatchState).orElse(null));
    }
}
