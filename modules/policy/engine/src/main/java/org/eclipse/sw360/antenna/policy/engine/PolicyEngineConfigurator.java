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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wiring class for the {@link PolicyEngine}. It expects a list of qualified class names implementing a {@link Ruleset}
 * and instantiates the {@link PolicyEngine} infrastructure with the {@link Rule} objects defined in them.
 */
public class PolicyEngineConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyEngineConfigurator.class);

    private static final String SINGLE_ARTIFACT_EXECUTOR = SingleArtifactExecutor.class.getName();
    private static final String COMPARE_ARTIFACT_EXECUTOR = CompareArtifactExecutor.class.getName();

    /**
     * The general configuration method for the {@link PolicyEngine}, it instantiates the infrastructure and returns the
     * entry point for policy evaluations.
     *
     * @param rulesetRefs List of {@link Ruleset} implementations as qualified class name. The classes need to be
     *                    available of the classpath.
     * @return The {@link PolicyEngine} instance to start policy evaluations
     * @throws IllegalArgumentException If no rule set given or not all classes could be found on the classpath
     */
    public static PolicyEngine configure(final Collection<String> rulesetRefs) throws IllegalArgumentException {
        if (rulesetRefs == null || rulesetRefs.isEmpty()) {
            throw new IllegalArgumentException("Configuration Error: No rule set reference given");
        }

        LOGGER.debug("Configuring the policy engine with rulesetRefs " + rulesetRefs.toString());

        final Map<String, Set<Rule>> executorToRuleMapping = rulesetRefs.stream()
                .map(PolicyEngineConfigurator::createRuleset)
                .map(Ruleset::getRules)
                .flatMap(Collection::stream)
                .map(PolicyEngineConfigurator::mapRuleToExecutor)
                .collect(Collectors.groupingBy(RuleToExecutor::getExecutorClass,
                        Collectors.mapping(RuleToExecutor::getRule, Collectors.toSet())));

        PolicyEngine resultEngine = new PolicyEngine(createExecutors(executorToRuleMapping));

        LOGGER.debug("Policy engine created");

        return resultEngine;
    }

    private static Ruleset createRuleset(final String ruleClassRef) throws IllegalArgumentException {
        try {
            return (Ruleset) Class.forName(ruleClassRef).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
                NoSuchMethodException | InvocationTargetException | ClassCastException e) {
            throw new IllegalArgumentException(
                    "Configuration Error: Instantiation of rule type failed: " + ruleClassRef, e);
        }
    }

    static class RuleToExecutor {
        private final Rule rule;
        private final String executorClass;

        private RuleToExecutor(final Rule rule, final String executorClass) {
            this.rule = rule;
            this.executorClass = executorClass;
        }

        Rule getRule() {
            return rule;
        }

        String getExecutorClass() {
            return executorClass;
        }
    }

    private static RuleToExecutor mapRuleToExecutor(final Rule rule) {
        if (rule instanceof SingleArtifactRule) {
            return new RuleToExecutor(rule, SINGLE_ARTIFACT_EXECUTOR);
        } else if (rule instanceof CompareArtifactRule) {
            return new RuleToExecutor(rule, COMPARE_ARTIFACT_EXECUTOR);
        }
        throw new IllegalStateException("Programming Error: Rule type defined without Executor: "
                + rule.getClass().getName());
    }

    private static Collection<RuleExecutor> createExecutors(final Map<String, Set<Rule>> executorToRuleMapping) {
        return executorToRuleMapping.entrySet()
                .stream()
                .map(entry -> createExecutor(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static RuleExecutor createExecutor(String executorClass, Collection<Rule> rules) {
        if (SINGLE_ARTIFACT_EXECUTOR.equals(executorClass)) {
            return new SingleArtifactExecutor(rules.stream()
                    .map(SingleArtifactRule.class::cast)
                    .collect(Collectors.toList()));
        } else if (COMPARE_ARTIFACT_EXECUTOR.equals(executorClass)) {
            return new CompareArtifactExecutor(rules.stream()
                    .map(CompareArtifactRule.class::cast)
                    .collect(Collectors.toList()));
        }
        throw new IllegalStateException("Programming Error: Unknown executor class");
    }
}
