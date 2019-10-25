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

import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    private ArtifactIdentifier getSelectorForOverwriting() {
        return new ArtifactFilename(FILENAME);
    }

    private Artifact getArtifactToOverwriteWith(MatchState matchState) {
        Artifact artifactToOverwriteWith = new Artifact();
        artifactToOverwriteWith.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, null, OVERRIDE_ID, null));
        Optional.ofNullable(matchState)
                .ifPresent(ms -> artifactToOverwriteWith.addFact(new ArtifactMatchingMetadata(ms)));
        return artifactToOverwriteWith;
    }

    private Map<ArtifactSelector, Artifact> getOverwriteMap(MatchState matchState, String copyrightStatement, String modificationStatus) {
        ArtifactIdentifier artifactSelector = getSelectorForOverwriting();
        Artifact artifactToOverwriteWith = getArtifactToOverwriteWith(matchState);
        artifactToOverwriteWith.addFact(artifactSelector);
        artifactToOverwriteWith.addFact(new CopyrightStatement(copyrightStatement));
        artifactToOverwriteWith.addFact(new ArtifactModificationStatus(modificationStatus));
        Map<ArtifactSelector, Artifact> overwriteMap = new HashMap<>();
        overwriteMap.put(new ArtifactCoordinates(new Coordinate("abc", "def")), generateDummyArtifact("overwriteDummy1"));
        overwriteMap.put(artifactSelector, artifactToOverwriteWith);
        overwriteMap.put(new ArtifactCoordinates(new Coordinate("bcd", "efg")), generateDummyArtifact("overwriteDummy2"));
        return overwriteMap;
    }

    @Test
    public void testOverride() {
        String copyrightStatement = "someCopyright Statement";
        String modificationStatus = "MODIFIED";

        specialArtifact.addFact(new ArtifactModificationStatus(modificationStatus));

        artifacts.add(specialArtifact);
        when(configMock.getOverride())
                .thenReturn(getOverwriteMap(overrideArtifactMatchState, copyrightStatement, null));

        ConfigurationHandlerOverride handler = new ConfigurationHandlerOverride(antennaContextMock);
        handler.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(4);
        Artifact processedArtifact = artifacts.stream()
                .filter(a -> new ArtifactFilename(FILENAME).matches(a))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("should not happen"));
        assertThat(processedArtifact.askFor(ArtifactCoordinates.class).get().getCoordinates().stream()
                .filter(p -> PackageURL.StandardTypes.MAVEN.equals(p.getType()))
                .findAny()
                .get()
                .getName())
                .isEqualTo(OVERRIDE_ID);
        assertThat(processedArtifact.askFor(ArtifactMatchingMetadata.class).map(ArtifactMatchingMetadata::getMatchState).orElse(null)).isEqualTo(expectedMatchState);
        assertThat(processedArtifact.askForGet(CopyrightStatement.class).get()).isEqualTo(copyrightStatement);
        assertThat(processedArtifact.askForGet(ArtifactModificationStatus.class).get()).isEqualTo(modificationStatus);
    }
}
