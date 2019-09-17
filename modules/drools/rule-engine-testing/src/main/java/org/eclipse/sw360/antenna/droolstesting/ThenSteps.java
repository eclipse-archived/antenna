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

import cucumber.api.java.en.Then;
import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenSteps {
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
                .orElseThrow(() -> new ExecutionException("Could not find evaluation policy with id " + policy));
    }

    private void runRules() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        File ruleFile = new File(state.rule_resource.getPath());
        kieFileSystem.write(ResourceFactory.newFileResource(ruleFile).setResourceType(ResourceType.DRL));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        KieRepository kieRepository = kieServices.getRepository();
        KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        KieSession kieSession = kieContainer.newKieSession();

        state.artifacts.forEach((key, value) -> kieSession.insert(value));
        state.evaluations.forEach(kieSession::insert);

        kieSession.fireAllRules();
    }

}
