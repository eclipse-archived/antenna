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
import org.eclipse.sw360.antenna.policy.engine.model.LicenseData;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class LicenseQualifiedRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public LicenseQualifiedRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact thirdPartyArtifact) {
        if (thirdPartyArtifact.isProprietary() || allLicensesAreOk(thirdPartyArtifact)) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }
        return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
    }

    private boolean allLicensesAreOk(ThirdPartyArtifact thirdPartyArtifact) {
        return thirdPartyArtifact.getLicenses().stream()
                .map(LicenseData::getLicenseText)
                .filter(optionalText -> !optionalText.isPresent())
                .count() == 0;
    }

    @Override
    public String getId() {
        return "LicenseQualified";
    }

    @Override
    public String getName() {
        return "License Qualified Rule";
    }

    @Override
    public String getDescription() {
        return "A license referenced by the artifact has no associated license text, it cannot be redistributed!";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.CRITICAL;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
