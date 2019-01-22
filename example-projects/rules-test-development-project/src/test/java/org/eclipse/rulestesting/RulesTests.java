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
package org.eclipse.rulestesting;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.bundle.DroolsEvaluationResult;
import org.eclipse.sw360.antenna.droolstesting.AbstractDroolsValidatorTest;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.api.IEvaluationResult;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieRepository;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.io.ResourceType;

public class RulesTests extends AbstractDroolsValidatorTest {
    @Test
    public void testArtifactWithRuleAndFailedArtifact() throws Exception {
        Artifact artifact = new Artifact();
        addArtifact(artifact);

        addEvaluationPolicyResult(
                new DroolsEvaluationResult("Unknown", "Some description of unknown artifacts", IEvaluationResult.Severity.FAIL, null));

        assertThat(rule("UnknownArtifactRule.drl").whenRunning())
                .failsExactlyArtifacts(artifact)
                .withPolicyId("Unknown");
    }

    @Test
    public void testArtifactWithRuleAndPassedArtifacts() throws Exception {
        Artifact artifact = new Artifact();
        artifact.addFact(new ArtifactMatchingMetadata(MatchState.EXACT));
        addArtifact(artifact);

        addEvaluationPolicyResult(
                new DroolsEvaluationResult("Unknown", "Some description of unknown artifacts", IEvaluationResult.Severity.FAIL, null));

        assertThat(rule("UnknownArtifactRule.drl").whenRunning()).passesAllArtifacts();
    }
}