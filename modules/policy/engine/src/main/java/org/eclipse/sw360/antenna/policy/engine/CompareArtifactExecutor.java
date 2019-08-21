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
 * Implementation of the {@link RuleExecutor} for getRules of type {@link CompareArtifactRule}.
 */
class CompareArtifactExecutor implements RuleExecutor {
    private final Collection<CompareArtifactRule> rules;

    public CompareArtifactExecutor(final Collection<CompareArtifactRule> rules) {
        this.rules = rules;
    }

    @Override
    public Collection<PolicyViolation> executeRules(final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {
        return rules.parallelStream()
                .map(rule -> executeRuleOnArtifacts(rule, thirdPartyArtifacts))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<PolicyViolation> executeRuleOnArtifacts(final CompareArtifactRule rule,
            final Collection<ThirdPartyArtifact> thirdPartyArtifacts) {

        return thirdPartyArtifacts.stream()
                .map(artifact -> findViolationsForLeftHandSide(rule, artifact, thirdPartyArtifacts))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<PolicyViolation> findViolationsForLeftHandSide(final CompareArtifactRule rule,
            final ThirdPartyArtifact leftHandSide, final Collection<ThirdPartyArtifact> rightHandSides) {

        return rightHandSides.stream()
                .map(rightHandSide -> rule.evaluate(leftHandSide, rightHandSide))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Ruleset> getRuleSets() {
        return rules.stream().map(Rule::getRuleset).collect(Collectors.toSet());
    }
}
