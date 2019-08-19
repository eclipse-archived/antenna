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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link RuleExecutor} for rules of type {@link SingleArtifactRule}.
 */
class SingleArtifactExecutor implements RuleExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleArtifactExecutor.class);

    private final Collection<SingleArtifactRule> rules;

    SingleArtifactExecutor(final Collection<SingleArtifactRule> rules) {
        this.rules = rules;
    }

    @Override
    public Collection<ViolationIssue> executeRules(final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {
        return rules.parallelStream()
                .map(rule -> executeRuleOnElements(rule, thirdPartyArtifacts))
                .flatMap(col -> col.stream())
                .collect(Collectors.toList());
    }

    private Collection<ViolationIssue> executeRuleOnElements(final SingleArtifactRule rule, final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {
        return thirdPartyArtifacts.stream()
                .filter(artifact -> hasArtifactPolicyViolation(rule, artifact))
                .map(artifact -> new ViolationIssue(rule, Arrays.asList(artifact)))
                .collect(Collectors.toList());
    }

    private boolean hasArtifactPolicyViolation(SingleArtifactRule rule, ThirdPartyArtifact artifact) {
        try {
            rule.evaluate(artifact);
        } catch (PolicyException e) {
            LOGGER.debug("Policy violation detected for artifact " + artifact.toString() + " for rule " + rule.toString());
            return true;
        }
        return false;
    }
}
