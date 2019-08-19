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

import org.apache.commons.collections4.IterableUtils;
import org.eclipse.sw360.antenna.policy.engine.testdata.PolicyEngineTestdata;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyEngineConfiguratorTest {
    @Test
    public void testPolicyEngineConfigurator () throws NoSuchFieldException, IllegalAccessException {
        PolicyEngine testResult = PolicyEngineConfigurator.configure(PolicyEngineTestdata.RULESETCONFIG);
        assertThat(testResult).isNotNull();
        Collection<RuleExecutor> createdExecutor = extractExecutor(testResult);
        assertThat(createdExecutor.size()).isEqualTo(1);
        assertThat(IterableUtils.get(createdExecutor, 0)).isInstanceOf(SingleArtifactExecutor.class);
        Collection<Rule> createdRules = extractRules(createdExecutor);
        assertThat(createdRules.size()).isEqualTo(2);
        assertThat(IterableUtils.get(createdRules,0)).isInstanceOf(SingleArtifactRule.class);
        assertThat(IterableUtils.get(createdRules,1)).isInstanceOf(SingleArtifactRule.class);
    }

    private Collection<Rule> extractRules(final Collection<RuleExecutor> testResult) throws IllegalAccessException, NoSuchFieldException {
        final SingleArtifactExecutor relevantExecutor = (SingleArtifactExecutor) IterableUtils.get(testResult, 0);
        final Field ruleField = SingleArtifactExecutor.class.getDeclaredField("rules");
        ruleField.setAccessible(true);
        return (Collection<Rule>) ruleField.get(relevantExecutor);
    }

    private Collection<RuleExecutor> extractExecutor(final PolicyEngine testResult) throws IllegalAccessException, NoSuchFieldException {
        final Field executorField = PolicyEngine.class.getDeclaredField("executors");
        executorField.setAccessible(true);
        return (Collection<RuleExecutor>) executorField.get(testResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPolicyEngineConfiguratorWrongClassname () {
        Collection<String> rules = new ArrayList<>();
        rules.addAll(PolicyEngineTestdata.RULESETCONFIG);
        rules.add("org.eclipse.sw360.antenna.policy.engine.DummyClass");
        PolicyEngineConfigurator.configure(rules);
    }

    @Test(expected = IllegalStateException.class)
    public void testPolicyEngineConfiguratorRuleWithoutExecutor () {
        Collection<String> rules = new ArrayList<>();
        rules.addAll(PolicyEngineTestdata.FAILINGRULESETCONFIG);
        PolicyEngineConfigurator.configure(rules);
    }
}
