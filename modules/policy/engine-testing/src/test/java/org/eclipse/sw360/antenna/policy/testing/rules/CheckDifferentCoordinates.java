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
package org.eclipse.sw360.antenna.policy.testing.rules;

import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.policy.engine.*;

import java.util.Optional;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class CheckDifferentCoordinates implements CompareArtifactRule {
    private final Ruleset ruleset;

    public CheckDifferentCoordinates(final Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact leftArtifact, ThirdPartyArtifact rightArtifact) {
        System.out.println("Execute with " + leftArtifact.toString() + " and " + rightArtifact.toString());
        if (countComponentTypes(leftArtifact, rightArtifact) > 1) {
            System.out.println("Policy Violation");
            return artifactRaisesPolicyViolation(this, leftArtifact, rightArtifact);
        }
        return artifactAppliesToRule(this, leftArtifact, rightArtifact);
    }

    private long countComponentTypes(ThirdPartyArtifact leftArtifact, ThirdPartyArtifact rightArtifact) {
        return Stream.of(leftArtifact, rightArtifact)
                .map(ThirdPartyArtifact::getPurl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(PackageURL::getType)
                .distinct()
                .count();
    }

    @Override
    public String getId() {
        return "T2";
    }

    @Override
    public String getName() {
        return "Check components of same type";
    }

    @Override
    public String getDescription() {
        return "Two components from different technologies have been found";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.INFO;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
