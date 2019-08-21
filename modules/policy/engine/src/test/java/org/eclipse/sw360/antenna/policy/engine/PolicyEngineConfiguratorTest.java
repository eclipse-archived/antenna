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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyEngineConfiguratorTest {
    @Test
    public void testProperConfiguration() throws NoSuchFieldException, IllegalAccessException {
        PolicyEngine testResult = PolicyEngineConfigurator.configure(PolicyEngineTestdata.RULESETCONFIG);
        assertThat(testResult).isNotNull();
        Collection<RuleExecutor> createdExecutor = extractExecutor(testResult);
        assertThat(createdExecutor.size()).isEqualTo(1);
        assertThat(identifyExecutors(createdExecutor, SingleArtifactExecutor.class.getName())).isTrue();
        assertThat(identifyExecutors(createdExecutor, CompareArtifactExecutor.class.getName())).isFalse();
        Collection<Rule> createdRules = extractRules(createdExecutor);
        assertThat(createdRules.size()).isEqualTo(2);
        createdRules.forEach(rule  -> assertThat(rule).isInstanceOf(SingleArtifactRule.class));
    }

    private boolean identifyExecutors(Collection<RuleExecutor> createdExecutors, String requestedType) {
        return createdExecutors.stream()
                .anyMatch(executor -> executor.getClass().getName().equals(requestedType));
    }

    private Collection<Rule> extractRules(final Collection<RuleExecutor> testResult) {
        return testResult.stream()
                .map(executor -> accessRules(executor))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<Rule> accessRules(RuleExecutor executor) {
        try {
            final Field ruleField = executor.getClass().getDeclaredField("rules");
            ruleField.setAccessible(true);
            return (Collection<Rule>) ruleField.get(executor);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private Collection<RuleExecutor> extractExecutor(
            final PolicyEngine testResult) throws IllegalAccessException, NoSuchFieldException {
        final Field executorField = PolicyEngine.class.getDeclaredField("executors");
        executorField.setAccessible(true);
        return (Collection<RuleExecutor>) executorField.get(testResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongClassname() {
        Collection<String> ruleSets = new ArrayList<>();
        ruleSets.addAll(PolicyEngineTestdata.RULESETCONFIG);
        ruleSets.add("org.eclipse.sw360.antenna.policy.engine.DummyClass");
        PolicyEngineConfigurator.configure(ruleSets);
    }

    @Test(expected = IllegalStateException.class)
    public void testRuleWithoutExecutor() {
        PolicyEngineConfigurator.configure(PolicyEngineTestdata.FAILINGRULESETCONFIG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoRulesets() {
        PolicyEngineConfigurator.configure(new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullParam() {
        PolicyEngineConfigurator.configure(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotRulesetClass() {
        PolicyEngineConfigurator.configure(PolicyEngineTestdata.NORULESETCLASSCONFIG);
    }
}
