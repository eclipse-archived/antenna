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

import org.assertj.core.api.AbstractAssert;
import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RuleResultsAssert extends AbstractAssert<RuleResultsAssert, RuleResults> {

    RuleResultsAssert(RuleResults ruleResults) {
        super(ruleResults, RuleResultsAssert.class);
    }

    static RuleResultsAssert assertThat(RuleResults actual) {
        return new RuleResultsAssert(actual);
    }

    /**
     * Passes the test only if some artifacts fail the rule
     */
    public void fails() {
        List<Artifact> actualViolations = getAllArtifactViolations();

        if (actualViolations.isEmpty()) {
            failWithMessage("Expected rule " + actual.getName() +
                    " to fail some artifacts but failed none.");
        }
    }

    /**
     * Passes the test only if all artifacts pass the rule
     */
    public void passesAllArtifacts() {
        List<Artifact> actualViolations = getAllArtifactViolations();

        if (!actualViolations.isEmpty()) {
            failWithMessage("Expected rule " + actual.getName() +
                    " to pass all artifacts but failed " + actualViolations + ".");
        }
    }

    /**
     * Fails the test if all given artifacts are listed as artifact failures. There may be more failures.
     * Use this method when asserting artifact failures for a certain policy.
     *
     * @param artifacts expected to fail the rules test
     * @return an assertion object which also encapsulates the artifacts handed into this method
     */
    public PartialRuleAssertionResults failsArtifacts(Artifact... artifacts) {
        List<Artifact> allArtifactViolations = getAllArtifactViolations();

        if (!allArtifactViolations.containsAll(Arrays.asList(artifacts))) {
            List<Artifact> passingArtifactsWhichShouldFail =
                    filterPassingArtifactsWhichAreExpectedToFail(allArtifactViolations, Arrays.asList(artifacts));
            failWithMessage("Expected rule " + actual.getName() +
                    " to fail artifacts " + passingArtifactsWhichShouldFail + " but did not.");
        }

        return new PartialRuleAssertionResults(actual, Arrays.asList(artifacts));
    }

    /**
     * Fails the test if the given list of artifacts is identical to the complete list of failures (including duplicates)
     *
     * @param artifacts expected to fail the rules test
     * @return an assertion object which also encapsulates the artifacts handed into this method
     */
    public PartialRuleAssertionResults failsExactlyArtifacts(Artifact... artifacts) {
        List<Artifact> actualViolations = getAllArtifactViolations();
        List<Artifact> expectedViolations = Arrays.asList(artifacts);

        if (!actualViolations.containsAll(expectedViolations) || !expectedViolations.containsAll(actualViolations)) {
            List<Artifact> passingArtifactsWhichShouldFail =
                    filterPassingArtifactsWhichAreExpectedToFail(actualViolations, expectedViolations);

            List<Artifact> artifactsWhichAlsoFail = actualViolations.stream()
                    .filter(expectedViolations::contains)
                    .collect(Collectors.toList());

            failWithMessage("Expected rule " + actual.getName() + " to fail artifacts "
                    + passingArtifactsWhichShouldFail + " but instead failed additional artifacts " + artifactsWhichAlsoFail + ".");
        }

        return new PartialRuleAssertionResults(actual, expectedViolations);
    }

    private List<Artifact> filterPassingArtifactsWhichAreExpectedToFail(List<Artifact> actualViolations, List<Artifact> expectedViolations) {
        return expectedViolations.stream()
                .filter(artifact -> !actualViolations.contains(artifact))
                .collect(Collectors.toList());
    }

    private List<Artifact> getAllArtifactViolations() {
        return actual.getEvaluations().stream()
                .map(IEvaluationResult::getFailedArtifacts)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
