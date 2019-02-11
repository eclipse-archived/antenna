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
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class AbstractDroolsValidatorTest {

    private List<Artifact> artifacts;
    private List<IEvaluationResult> evaluations;

    @Before
    public void init() {
        artifacts = new ArrayList<>();
        evaluations = new ArrayList<>();
    }

    /**
     * Add an artifact which will be scanned by this test
     *
     * @param artifact to be added to the test run
     */
    public void addArtifact(Artifact artifact) {
        artifacts.add(artifact);
    }

    /**
     * Add an evaluation policy used by the rules in the test. Usually, a rule will add artifacts that fail the rule to
     * one or more evaluation results. Those results will later be scanned for failed artifacts. Therefore, all evaluation
     * policy results used by the rule to be tested should be added.
     *
     * @param evaluationResult to be added to the test run
     */
    public void addEvaluationPolicyResult(DroolsEvaluationResult evaluationResult) {
        evaluations.add(evaluationResult);
    }

    /**
     * Given the file name of the rule to be used, creates a Rules object. Use together with RuleResultsAssert via:
     *
     * assertThat(rule("MyCustomRule.drl").whenRunning())...
     *
     * @param filename of the rule (e.g. "MyCustomRule.drl") in the rules project
     * @return a Rule object
     */
    public Rule rule(String filename) {
        return new Rule(this, filename, artifacts, evaluations);
    }

    /**
     * Entry point for evaluating rules. Use as assertThat(rule("MyCustomRule.drl").whenRunning()
     *
     * @param results result of running a rule, obtain via rule("MyCustomRule.drl").whenRunning()
     * @return
     */
    public static RuleResultsAssert assertThat(RuleResults results) {
        return RuleResultsAssert.assertThat(results);
    }
}
