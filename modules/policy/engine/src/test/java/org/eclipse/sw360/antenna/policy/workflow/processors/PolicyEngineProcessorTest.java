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
package org.eclipse.sw360.antenna.policy.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.policy.engine.testdata.PolicyEngineTestdata;
import org.eclipse.sw360.antenna.policy.workflow.processors.testdata.AntennaTestdata;
import org.eclipse.sw360.antenna.policy.workflow.processors.testdata.TestCompareRuleset;
import org.eclipse.sw360.antenna.policy.workflow.processors.testdata.TestSingleRuleset;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyEngineProcessorTest {
    private static final String RULESETCONFIGKEY = "ruleset.classes";

    @Test
    public void testPolicyEngineIntegration() {
        PolicyEngineProcessor testee = new PolicyEngineProcessor();
        Map<String, String> configMap = new HashMap<>();
        configMap.put(RULESETCONFIGKEY, PolicyEngineTestdata.RULESET_CLASS);
        testee.configure(configMap);

        Artifact a1 = new Artifact("Source 1");
        Artifact a2 = new Artifact("Source 2");

        IPolicyEvaluation result = testee.evaluate(Arrays.asList(a1, a2));
        Collection<String> ruleIds = result.getEvaluationResults()
                .stream()
                .map(IEvaluationResult::getId)
                .distinct()
                .collect(Collectors.toList());

        assertThat(ruleIds.size()).isEqualTo(1);
        ruleIds.forEach(ruleId -> assertThat(ruleId).isEqualTo(PolicyEngineTestdata.ALWAYS_VIOLATED_ID));
    }

    @Test
    public void testPolicyEngineWithMatchStateRule() {
        PolicyEngineProcessor testee = new PolicyEngineProcessor();
        Map<String, String> configMap = new HashMap<>();
        configMap.put(RULESETCONFIGKEY, AntennaTestdata.SINGLE_RULESET_CLASS);
        testee.configure(configMap);

        IPolicyEvaluation result = testee.evaluate(AntennaTestdata.TEST_MATCH_STATE_ARTIFACTS);
        Collection<Artifact> failingArtifacts = result.getEvaluationResults()
                .stream()
                .map(IEvaluationResult::getFailedArtifacts)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        assertThat(failingArtifacts.size()).isEqualTo(1);
        assertThat(failingArtifacts).contains(AntennaTestdata.UNKNOWN_ARTIFACT);
    }

    @Test
    public void testPolicyEngineWithLicenseRule() {
        PolicyEngineProcessor testee = new PolicyEngineProcessor();
        Map<String, String> configMap = new HashMap<>();
        configMap.put(RULESETCONFIGKEY, AntennaTestdata.COMPARE_RULESET_CLASS);
        testee.configure(configMap);

        IPolicyEvaluation result = testee.evaluate(AntennaTestdata.TEST_LICENSE_ARTIFACTS);
        assertThat(result.getEvaluationResults().size()).isEqualTo(1);
        Collection<Artifact> failingArtifacts = result.getEvaluationResults()
                .stream()
                .map(IEvaluationResult::getFailedArtifacts)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        assertThat(failingArtifacts.size()).isEqualTo(2);
        assertThat(failingArtifacts).contains(AntennaTestdata.EPL_ARTIFACT);
        assertThat(failingArtifacts).contains(AntennaTestdata.GPL_ARTIFACT);
    }

    @Test
    public void testTwoRulesetsAreFound() {
        PolicyEngineProcessor testee = new PolicyEngineProcessor();
        Map<String, String> configMap = new HashMap<>();
        configMap.put(RULESETCONFIGKEY, AntennaTestdata.TEST_RULESETS_LIST);
        testee.configure(configMap);

        String[] description = testee.getRulesetDescription().split("\n");
        assertThat(description.length).isEqualTo(3);
        assertThat(description[1]).contains("1.0.0");
        assertThat(description[2]).contains("1.0.0");
        assertThat(description[1] + description[2]).contains(TestSingleRuleset.class.getSimpleName());
        assertThat(description[1] + description[2]).contains(TestCompareRuleset.class.getSimpleName());
    }
}
