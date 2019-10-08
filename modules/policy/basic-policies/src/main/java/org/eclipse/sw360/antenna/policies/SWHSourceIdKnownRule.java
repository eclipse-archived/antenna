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
package org.eclipse.sw360.antenna.policies;

import org.eclipse.sw360.antenna.policy.engine.PolicyViolation;
import org.eclipse.sw360.antenna.policy.engine.RuleSeverity;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;
import org.eclipse.sw360.antenna.policy.engine.SingleArtifactRule;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class SWHSourceIdKnownRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public SWHSourceIdKnownRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact thirdPartyArtifact) {
        if (thirdPartyArtifact.isProprietary() || thirdPartyArtifact.getSWHSourceId().isPresent()) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }

        return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
    }

    @Override
    public String getId() {
        return "SWHAvailable";
    }

    @Override
    public String getName() {
        return "Software Heritage Source Id Known Rule";
    }

    @Override
    public String getDescription() {
        return "The artifact software heritage source Id is not known!";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.WARN;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
