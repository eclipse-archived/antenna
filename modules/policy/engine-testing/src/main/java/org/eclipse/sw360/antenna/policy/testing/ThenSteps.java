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
package org.eclipse.sw360.antenna.policy.testing;

import cucumber.api.java.en.Then;
import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.policy.workflow.processors.PolicyEngineProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenSteps {
    private static final String RULESETCONFIGKEY = "ruleset.classes";

    private ScenarioState state;

    public ThenSteps(ScenarioState state) {
        this.state = state;
    }

    @Then("^the artifact called \"([^\"]*)\" should fail with policy id \"([^\"]*)\"$")
    public void the_artifact_should_fail(String name, String policy) {
        runRules();

        IEvaluationResult evaluationResult = findEvaluation(policy);

        assertThat(evaluationResult.getFailedArtifacts()).hasSize(1);
        assertThat(evaluationResult.getFailedArtifacts()).containsExactly(state.artifacts.get(name));
    }

    @Then("^the artifact should fail with policy id \"([^\"]*)\"$")
    public void the_artifact_should_fail(String policy) {
        runRules();

        IEvaluationResult evaluationResult = findEvaluation(policy);

        assertThat(state.artifacts).hasSize(1);
        assertThat(evaluationResult.getFailedArtifacts()).hasSize(1);
    }

    @Then("^all artifacts fail with policy id \"([^\"]*)\"$")
    public void all_artifacts_fail(String policy) {
        runRules();

        IEvaluationResult evaluationResult = findEvaluation(policy);

        assertThat(evaluationResult.getFailedArtifacts()).hasSize(state.artifacts.size());
    }

    @Then("^no artifact fails$")
    public void the_artifact_should_pass() {
        runRules();

        state.evaluations.forEach(evaluation -> assertThat(evaluation.getFailedArtifacts()).hasSize(0));
    }

    private IEvaluationResult findEvaluation(String policy) {
        return state.evaluations.stream()
                .filter(evaluation -> evaluation.getId().equals(policy))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find evaluation policy with id " + policy));
    }

    private void runRules() {
        PolicyEngineProcessor testee = new PolicyEngineProcessor();
        Map<String, String> configMap = new HashMap<>();
        configMap.put(RULESETCONFIGKEY, state.rulesets.stream().collect(Collectors.joining(",")));
        testee.configure(configMap);
        state.evaluations.addAll(testee.evaluate(state.artifacts.values()).getEvaluationResults());
    }

}
