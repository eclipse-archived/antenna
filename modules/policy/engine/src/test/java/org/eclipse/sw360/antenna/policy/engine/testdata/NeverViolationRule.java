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
package org.eclipse.sw360.antenna.policy.engine.testdata;

import org.eclipse.sw360.antenna.policy.engine.*;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;

public class NeverViolationRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public NeverViolationRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public String getId() {
        return PolicyEngineTestdata.NEVER_VIOLATED_ID;
    }

    @Override
    public String getName() {
        return "Never violated Rule";
    }

    @Override
    public String getDescription() {
        return "Rule is never violated";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.WARN;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact artifact) {
        return artifactAppliesToRule(this, artifact);
    }
}
