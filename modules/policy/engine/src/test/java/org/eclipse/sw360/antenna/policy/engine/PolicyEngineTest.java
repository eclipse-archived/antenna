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
package org.eclipse.sw360.antenna.policy.engine;

import org.eclipse.sw360.antenna.policy.engine.testdata.PolicyEngineTestdata;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyEngineTest {
    private PolicyEngine testee = PolicyEngineConfigurator.configure(PolicyEngineTestdata.RULESETCONFIG);

    @Test
    public void runPolicyEngineTest() {
        Collection<ViolationIssue> result = testee.evaluate(PolicyEngineTestdata.ARTIFACTS);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        Collection<String> ruleIds = result.stream().map(violation -> violation.getId()).distinct().collect(Collectors.toList());
        assertThat(ruleIds.size()).isEqualTo(1);
        assertThat(ruleIds.iterator().next()).isEqualTo("AV");
    }
}
