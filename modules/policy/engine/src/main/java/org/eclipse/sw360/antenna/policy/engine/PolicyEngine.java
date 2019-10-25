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

import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Policy engine that executes all rules on the given data by calling the different {@link RuleExecutor} instances.
 */
public class PolicyEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyEngine.class);

    private final Collection<RuleExecutor> executors;

    public PolicyEngine(final Collection<RuleExecutor> executors) {
        this.executors = executors;
    }

    public Collection<PolicyViolation> evaluate(final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {
        LOGGER.debug("Artifacts are " + thirdPartyArtifacts.stream()
                .map(ThirdPartyArtifact::getCoordinates)
                .flatMap(Collection::stream)
                .map(Coordinate::canonicalize)
                .collect(Collectors.joining(",", "[", "]")));

        Collection<PolicyViolation> violations =  executors.parallelStream()
                .map(executor -> executor.executeRules(thirdPartyArtifacts))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (violations.size() > 0) {
            LOGGER.warn("Number of violations found: " + violations.size());
        } else {
            LOGGER.info("No violations found");
        }

        return violations;
    }

    public Collection<Ruleset> getRulesets() {
        return executors.stream()
                .map(RuleExecutor::getRulesets)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}

