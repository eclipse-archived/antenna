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
package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchStateValidatorTest extends AntennaTestWithMockedContext {

    private MatchStateValidator matchStateValidator;
    private Artifact artifactUNKNOWN;
    private Artifact artifactProprietaryUNKNOWN;
    private Artifact artifactEXACT;
    private Artifact artifactProprietaryEXACT;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        matchStateValidator = new MatchStateValidator();
        matchStateValidator.setAntennaContext(antennaContextMock);
        matchStateValidator.configure(Collections.emptyMap());


        artifactUNKNOWN = new Artifact();
        artifactUNKNOWN.addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));

        artifactProprietaryUNKNOWN = new Artifact();
        artifactProprietaryUNKNOWN.addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        artifactProprietaryUNKNOWN.setFlag(Artifact.IS_PROPRIETARY_FLAG_KEY);

        artifactEXACT = new Artifact();
        artifactEXACT.addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        artifactProprietaryEXACT = new Artifact();
        artifactProprietaryEXACT.addFact(new ArtifactMatchingMetadata(MatchState.EXACT));
        artifactProprietaryEXACT.setFlag(Artifact.IS_PROPRIETARY_FLAG_KEY);
    }

    @Test
    public void validateMatchStateTest1() {

        List<Artifact> artifacts = new ArrayList<>();
        artifacts.add(artifactEXACT);
        artifacts.add(artifactProprietaryUNKNOWN);
        artifacts.add(artifactProprietaryEXACT);
        artifacts.add(artifactUNKNOWN);
        assertThat(matchStateValidator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void validateMatchStateTest2() {
        List<Artifact> artifacts = new ArrayList<>();
        artifacts.add(artifactEXACT);
        artifacts.add(artifactProprietaryUNKNOWN);
        artifacts.add(artifactProprietaryEXACT);
        assertThat(matchStateValidator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void validateMatchStateTest3() {
        List<Artifact> artifacts = new ArrayList<>();
        artifacts.add(artifactEXACT);
        artifacts.add(artifactProprietaryUNKNOWN);
        assertThat(matchStateValidator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void validateMatchStateTest4() {
        List<Artifact> artifacts = new ArrayList<>();
        artifacts.add(artifactEXACT);
        assertThat(matchStateValidator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }
}
