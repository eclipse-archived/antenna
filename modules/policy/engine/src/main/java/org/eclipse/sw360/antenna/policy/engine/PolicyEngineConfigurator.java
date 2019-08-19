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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wiring class for the {@link PolicyEngine}. It expects a list of qualified class names implementing a {@link RuleSet}
 * and instantiates the {@link PolicyEngine} infrastructure with the {@link Rule} objects defined in them.
 */
public class PolicyEngineConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyEngineConfigurator.class);

    /**
     * The general configuration method for the {@link PolicyEngine}, it instantiates the infrastructure and returns the
     * entry point for policy evaluations.
     *
     * @param ruleSetRefs List of {@link RuleSet} implementations as qualified class name. The classes need to be
     *                     available of the classpath.
     * @return The {@link PolicyEngine} instance to start policy evaluations.
     * @throws IllegalArgumentException If not all classes could be found on the classpath.
     */
    public static PolicyEngine configure(final Collection<String> ruleSetRefs) throws IllegalArgumentException {
        LOGGER.debug("Configuring the policy engine with ruleSetRefs " + ruleSetRefs.toString());

        final Map<String, Collection<Rule>> ruleToExecutorMapping = new HashMap<>();

        ruleSetRefs.stream()
                .map(PolicyEngineConfigurator::createRuleSet)
                .flatMap(rs -> rs.rules().stream())
                .map(PolicyEngineConfigurator::mapRuleToExecutor)
                .forEach(data -> addRuleToExecutor(ruleToExecutorMapping, data.executorClass, data.rule));

        PolicyEngine resultEngine = new PolicyEngine(createExecutors(ruleToExecutorMapping));

        LOGGER.debug("Policy engine created");

        return resultEngine;
    }

    private static RuleSet createRuleSet(final String ruleClassRef) {
        try {
            return (RuleSet) Class.forName(ruleClassRef).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Configuration Error: Instantiation of rule type failed: " + ruleClassRef, e);
        }
    }

    private static class RuleToExecutor {
        private final Rule rule;
        private final String executorClass;

        private RuleToExecutor(final Rule rule, final String executorClazz) {
            this.rule = rule;
            this.executorClass = executorClazz;
        }
    }

    private static RuleToExecutor mapRuleToExecutor(final Rule rule) {
        if (rule instanceof SingleArtifactRule) {
            return new RuleToExecutor(rule, SingleArtifactExecutor.class.getName());
        }
        throw new IllegalStateException("Programming Error: Rule type defined without Executor: " + rule.getClass().getName());
    }

    private static void addRuleToExecutor(final Map<String, Collection<Rule>> ruleToExecutorMapping,
                                          final String executor, final Rule rule) {
        final Collection<Rule> listOfRules = Optional.ofNullable(ruleToExecutorMapping.get(executor)).orElse(new ArrayList<>());
        listOfRules.add(rule);
        ruleToExecutorMapping.put(executor, listOfRules);
    }

    private static Collection<RuleExecutor> createExecutors(final Map<String, Collection<Rule>> ruleToExecutorMapping) {
        final Collection<RuleExecutor> executors = new ArrayList<>();
        executors.add(new SingleArtifactExecutor(ruleToSingleElementRule(ruleToExecutorMapping)));
        return executors;
    }

    private static Collection<SingleArtifactRule> ruleToSingleElementRule(final Map<String, Collection<Rule>> ruleToExecutorMapping) {
        return ruleToExecutorMapping.get(SingleArtifactExecutor.class.getName())
                .stream()
                .map(rule -> (SingleArtifactRule) rule)
                .collect(Collectors.toList());
    }
}
