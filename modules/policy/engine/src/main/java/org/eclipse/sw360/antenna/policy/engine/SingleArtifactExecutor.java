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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link RuleExecutor} for rules of type {@link SingleArtifactRule}.
 */
class SingleArtifactExecutor implements RuleExecutor {
    private final Collection<SingleArtifactRule> rules;

    SingleArtifactExecutor(final Collection<SingleArtifactRule> rules) {
        this.rules = rules;
    }

    @Override
    public Collection<PolicyViolation> executeRules(final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {
        return rules.parallelStream()
                .map(rule -> executeRuleOnElements(rule, thirdPartyArtifacts))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<PolicyViolation> executeRuleOnElements(final SingleArtifactRule rule,
            final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {
        return thirdPartyArtifacts.stream()
                .map(artifact -> rule.evaluate(artifact))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Ruleset> getRulesets() {
        return rules.stream().map(Rule::getRuleset).collect(Collectors.toSet());
    }

}
