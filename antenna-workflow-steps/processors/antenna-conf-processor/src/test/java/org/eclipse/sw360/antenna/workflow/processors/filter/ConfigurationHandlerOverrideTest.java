/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors.filter;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.ArtifactSelector;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ConfigurationHandlerOverrideTest extends CommonConfigurationHandlerTest {
    private String OVERRIDE_ID = "overrideId";


    private MatchState overrideArtifactMatchState;
    private MatchState expectedMatchState;

    @Parameterized.Parameters(name = "{index}: artifact={0} override={1} expectedAfterOverride={2}")
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {
                {null,               null,               null},
                {null,               MatchState.SIMILAR, MatchState.SIMILAR},
                {MatchState.SIMILAR, MatchState.SIMILAR, MatchState.SIMILAR},
                {MatchState.SIMILAR, null,               MatchState.SIMILAR},
                {null,               MatchState.EXACT,   MatchState.EXACT},
                {MatchState.EXACT,   MatchState.EXACT,   MatchState.EXACT},
                {MatchState.EXACT,   null,               MatchState.EXACT},
                {MatchState.SIMILAR, MatchState.EXACT,   MatchState.EXACT},
                {MatchState.EXACT,   MatchState.SIMILAR, MatchState.SIMILAR},
        });
    }

    public ConfigurationHandlerOverrideTest(MatchState artifactMatchState, MatchState overrideArtifactMatchState, MatchState expectedMatchState) {
        super(artifactMatchState);
        this.overrideArtifactMatchState = overrideArtifactMatchState;
        this.expectedMatchState = expectedMatchState;
    }

    private ArtifactSelector getSelectorForOverwriting() {
        ArtifactIdentifier keyIdentifier = new ArtifactIdentifier();
        keyIdentifier.setFilename(FILENAME);
        return new ArtifactSelector(keyIdentifier);
    }

    private Artifact getArtifactToOverwriteWith(MatchState matchState) {
        Artifact artifactToOverwriteWith = new Artifact();
        ArtifactIdentifier overrideValue = new ArtifactIdentifier();
        MavenCoordinates mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setArtifactId(OVERRIDE_ID);
        overrideValue.setMavenCoordinates(mavenCoordinates);
        artifactToOverwriteWith.setArtifactIdentifier(overrideValue);
        Optional.ofNullable(matchState)
                .ifPresent(artifactToOverwriteWith::setMatchState);
        return artifactToOverwriteWith;
    }

    private Map<ArtifactSelector, Artifact> getOverwriteMap(MatchState matchState, String copyrightStatement, String modificationStatus) {
        ArtifactSelector artifactSelector = getSelectorForOverwriting();
        Artifact artifactToOverwriteWith = getArtifactToOverwriteWith(matchState);
        artifactToOverwriteWith.setCopyrightStatement(copyrightStatement);
        artifactToOverwriteWith.setModificationStatus(modificationStatus);
        Map<ArtifactSelector, Artifact> overwriteMap = new HashMap<>();
        overwriteMap.put(new ArtifactSelector(new ArtifactIdentifier()), generateDummyArtifact("overwriteDummy1"));
        overwriteMap.put(artifactSelector, artifactToOverwriteWith);
        overwriteMap.put(new ArtifactSelector(new ArtifactIdentifier()), generateDummyArtifact("overwriteDummy2"));
        return overwriteMap;
    }

    @Test
    public void testOverride() {
        String copyrightStatement = "someCopyright Statement";
        String modificationStatus = "MODIFIED";

        specialArtifact.setModificationStatus(modificationStatus);

        artifacts.add(specialArtifact);
        when(configMock.getOverride())
                .thenReturn(getOverwriteMap(overrideArtifactMatchState, copyrightStatement, null));

        ConfigurationHandlerOverride handler = new ConfigurationHandlerOverride(antennaContextMock);
        handler.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(4);
        Artifact processedArtifact = artifacts.stream()
                .filter(a -> FILENAME.equals(a.getArtifactIdentifier().getFilename()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("should not happen"));
        assertThat(processedArtifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId())
                .isEqualTo(OVERRIDE_ID);
        assertThat(processedArtifact.getMatchState()).isEqualTo(expectedMatchState);
        assertThat(processedArtifact.getCopyrightStatement()).isEqualTo(copyrightStatement);
        assertThat(processedArtifact.getModificationStatus()).isEqualTo(modificationStatus);
    }
}
