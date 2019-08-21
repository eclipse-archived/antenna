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

public class EPLvsGPLRule implements CompareArtifactRule {
    private final Ruleset ruleSet;

    public EPLvsGPLRule(final Ruleset ruleSet) {
        this.ruleSet = ruleSet;
    }

    @Override
    public Optional<PolicyViolation> evaluate(final ThirdPartyArtifact leftArtifact,
            final ThirdPartyArtifact rightArtifact) {
        if(leftArtifact.hasLicense("EPL.*") && rightArtifact.hasLicense("GPL.*")) {
            return artifactRaisesPolicyViolation(this, leftArtifact, rightArtifact);
        }
        return artifactAppliesToRule(this, leftArtifact, rightArtifact);
    }

    @Override
    public String getId() {
        return AntennaTestdata.EPLVSGPLID;
    }

    @Override
    public String getName() {
        return "EPL vs GPL rule";
    }

    @Override
    public String getDescription() {
        return "The project contains both EPL and GPL licensed components, although the two license groups are incompatible";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.ERROR;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleSet;
    }
}
