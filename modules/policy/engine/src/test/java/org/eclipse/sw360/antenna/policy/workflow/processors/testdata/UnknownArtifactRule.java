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
package org.eclipse.sw360.antenna.policy.workflow.processors.testdata;

import org.eclipse.sw360.antenna.policy.engine.*;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class UnknownArtifactRule implements SingleArtifactRule {
    private final Ruleset ruleSet;

    public UnknownArtifactRule(final Ruleset ruleSet) {
        this.ruleSet = ruleSet;
    }

    @Override
    public Optional<PolicyViolation> evaluate(final ThirdPartyArtifact thirdPartyArtifact) {
        if(!thirdPartyArtifact.isIdentified()) {
            return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
        }
        return artifactAppliesToRule(this, thirdPartyArtifact);
    }

    @Override
    public String getId() {
        return AntennaTestdata.UNKNOWNARTIFACTID;
    }

    @Override
    public String getName() {
        return "Unknown Artifact";
    }

    @Override
    public String getDescription() {
        return "Artifact could not be identified, check with the OS service for identification";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.WARN;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleSet;
    }
}
