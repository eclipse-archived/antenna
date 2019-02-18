/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.droolstesting;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.bundle.DroolsEvaluationResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.eclipse.sw360.antenna.droolstesting.AbstractDroolsValidatorTest.assertThat;

public class DroolsAssertionsTest {

    @Test(expected = AssertionError.class)
    public void passesAllArtifactsFailsTestIfSomeArtifactIsInFailure() {
        Artifact artifact = new Artifact();

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).passesAllArtifacts();
    }

    @Test
    public void failsPassesTestIfSomeArtifactIsInFailure() {
        Artifact artifact = new Artifact();

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).fails();
    }

    @Test
    public void failsArtifactsPassesTestIfAllExpectedArtifactsFail() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact1, artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).failsArtifacts(artifact1, artifact2);
    }

    @Test
    public void failsArtifactsPassesTestIfOnlyASubsetOfFailedArtifactsIsDeclaredAsFailure() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact1, artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).failsArtifacts(artifact1);
    }

    @Test(expected = AssertionError.class)
    public void failsArtifactsFailsTestIfSomeDeclaredFailureIsMissing() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).failsArtifacts(artifact1);
    }

    @Test(expected = AssertionError.class)
    public void failsExactlyArtifactsFailsTestIfOnlyASubsetOfFailedArtifactsIsDeclaredAsFailure() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact1, artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).failsExactlyArtifacts(artifact1);
    }

    @Test(expected = AssertionError.class)
    public void failsExactlyArtifactsFailsTestIfUndeclaredArtifactsFail() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact1, artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).failsExactlyArtifacts(artifact2);
    }

    @Test
    public void withPolicyIdPassesTestIfDeclaredArtifactsFail() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults = getTestIdEvaluation(artifact1, artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Collections.singletonList(droolsEvaluationResults));

        assertThat(testing).failsArtifacts(artifact2).withPolicyId("TestId");
    }

    @Test(expected = AssertionError.class)
    public void whichAreExactlyThoseWithPolicyIdFailsTestIfSomeArtifactsAreMissing() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));
        Artifact artifact3 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.SIMILAR));

        IEvaluationResult droolsEvaluationResults1 = getTestIdEvaluation(artifact1, artifact3);
        IEvaluationResult droolsEvaluationResults2 = getWrongTestIdEvaluation(artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Arrays.asList(droolsEvaluationResults1, droolsEvaluationResults2));

        assertThat(testing).failsArtifacts(artifact1).whichAreExactlyThoseWithPolicyId("TestId");
    }

    @Test(expected = AssertionError.class)
    public void withPolicyIdFailsTestIfArtifactsFailWithWrongPolicyFail() {
        Artifact artifact1 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.UNKNOWN));
        Artifact artifact2 = new Artifact().addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        IEvaluationResult droolsEvaluationResults1 = getTestIdEvaluation();
        IEvaluationResult droolsEvaluationResults2 = getWrongTestIdEvaluation(artifact1, artifact2);

        RuleResults testing = new RuleResults("MyCustomRule", Arrays.asList(droolsEvaluationResults1, droolsEvaluationResults2));

        assertThat(testing).failsArtifacts(artifact1, artifact2).withPolicyId("TestId");
    }

    private IEvaluationResult getTestIdEvaluation(Artifact... artifacts) {
        return new DroolsEvaluationResult("TestId", "For testing",
                IEvaluationResult.Severity.INFO, new HashSet<>(Arrays.asList(artifacts)));
    }

    private IEvaluationResult getWrongTestIdEvaluation(Artifact... artifacts) {
        return new DroolsEvaluationResult("WrongTestId", "For testing",
                IEvaluationResult.Severity.FAIL, new HashSet<>(Arrays.asList(artifacts)));
    }

}
