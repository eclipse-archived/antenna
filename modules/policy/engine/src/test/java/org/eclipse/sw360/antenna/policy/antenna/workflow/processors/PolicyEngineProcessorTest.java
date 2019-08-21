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
package org.eclipse.sw360.antenna.policy.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyEngineProcessorTest {

    @Test
    public void testPolicyEngineIntegration() throws AntennaConfigurationException {
        PolicyEngineProcessor testee = new PolicyEngineProcessor();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("ruleset.classes", "org.eclipse.sw360.antenna.policy.engine.testdata.TestRuleSet");
        testee.configure(configMap);

        Artifact a1 = new Artifact("Source 1");
        Artifact a2 = new Artifact("Source 2");

        IPolicyEvaluation result = testee.evaluate(Arrays.asList(a1, a2));
        Map<String, IEvaluationResult> mappedResults = result
                .getEvaluationResults()
                .stream()
                .collect(Collectors.toMap(res -> res.getId(), res -> res));
        assertThat(mappedResults.get("AV").getFailedArtifacts().size()).isEqualTo(2);
        assertThat(mappedResults.get("NV").getFailedArtifacts().size()).isEqualTo(0);

    }
}
