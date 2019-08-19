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

import org.eclipse.sw360.antenna.policy.engine.RuleSet;
import org.eclipse.sw360.antenna.policy.engine.SingleArtifactRule;
import org.eclipse.sw360.antenna.policy.engine.ThirdPartyArtifact;
import org.eclipse.sw360.antenna.policy.engine.RuleSeverity;

public class NeverViolationRule implements SingleArtifactRule {
    private final RuleSet ruleSet;

    public NeverViolationRule(RuleSet ruleSet) {
        this.ruleSet = ruleSet; }

    @Override
    public String getId() {
        return "NV";
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
    public RuleSet getRuleSet() {
        return ruleSet;
    }

    @Override
    public void evaluate(ThirdPartyArtifact artifact) {
        /* no op */
    }
}
