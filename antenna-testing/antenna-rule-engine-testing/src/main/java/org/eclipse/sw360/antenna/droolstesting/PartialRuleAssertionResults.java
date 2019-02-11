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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PartialRuleAssertionResults extends AbstractAssert<PartialRuleAssertionResults, RuleResults> {
    private List<Artifact> artifacts;

    PartialRuleAssertionResults(RuleResults results, List<Artifact> artifacts) {
        super(results, PartialRuleAssertionResults.class);
        this.artifacts = artifacts;
    }

    /**
     * Passes test if all artifacts for this partial result fail policy with ID id. Additional artifacts may also fail
     * with the given id.
     *
     * Usage: failsArtifacts(artifact1, artifact2).withPolicyId("MyId") will only pass if artifact1 and artifact2 fail
     * the rule for policy "MyId". If they fail only for different policies or not at all, the test will fail.
     *
     * @param id of evaluation result where the artifacts should have failed the rule
     * @return The original RuleResults for method chaining
     */
    public RuleResultsAssert withPolicyId(String id) {
        IEvaluationResult correctPolicyResult = findEvaluationWithId(id);

        if (!correctPolicyResult.getFailedArtifacts().containsAll(artifacts)) {
            List<Artifact> unexpectedlyPassingArtifacts =
                    filterPassingArtifactsWhichAreExpectedToFail(this.artifacts, correctPolicyResult.getFailedArtifacts());
            failWithMessage("Expected rule " + actual.getName() + " to fail artifacts "
                    + unexpectedlyPassingArtifacts + ".");
        }

        return new RuleResultsAssert(actual);
    }

    /**
     * Passes test if all artifacts for this partial result fail policy with ID id and those are the only artifacts that
     * fail the rule
     *
     * Usage: failsArtifacts(artifact1, artifact2).withPolicyId("MyId") will pass if artifact1 and artifact2 fail
     * the rule for policy "MyId" and are the only failures for that rule.
     *
     * @param id of evaluation result where the artifacts should have failed the rule
     * @return The original RuleResults for method chaining
     */
    public RuleResultsAssert whichAreExactlyThoseWithPolicyId(String id) {
        IEvaluationResult correctPolicyResult = findEvaluationWithId(id);

        if (!correctPolicyResult.getFailedArtifacts().containsAll(artifacts) || !artifacts.containsAll(correctPolicyResult.getFailedArtifacts())) {
            List<Artifact> unexpectedlyPassingArtifacts =
                    filterPassingArtifactsWhichAreExpectedToFail(this.artifacts, correctPolicyResult.getFailedArtifacts());

            List<Artifact> unexpectedlyFailingArtifacts = correctPolicyResult.getFailedArtifacts()
                    .stream()
                    .filter(artifact -> !artifacts.contains(artifact))
                    .collect(Collectors.toList());

            failWithMessage("Expected rule" + actual.getName() + "  to fail artifacts "
                    + unexpectedlyPassingArtifacts + " but instead failed artifacts " + unexpectedlyFailingArtifacts + ".");
        }

        return new RuleResultsAssert(actual);
    }

    private IEvaluationResult findEvaluationWithId(String id) {
        return actual.getEvaluations().stream()
                .filter(evaluation -> evaluation.getId().equals(id))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Policy with Id " + id + " does not exist"));
    }

    private List<Artifact> filterPassingArtifactsWhichAreExpectedToFail(List<Artifact> actualViolations, Set<Artifact> expectedViolations) {
        return expectedViolations.stream()
                .filter(artifact -> !actualViolations.contains(artifact))
                .collect(Collectors.toList());
    }
}
