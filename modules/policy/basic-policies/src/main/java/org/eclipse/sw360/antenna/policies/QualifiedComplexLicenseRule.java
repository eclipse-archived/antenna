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
import org.eclipse.sw360.antenna.policy.engine.model.LicenseState;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class QualifiedComplexLicenseRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public QualifiedComplexLicenseRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact thirdPartyArtifact) {
        if (thirdPartyArtifact.isProprietary() || licenseStateOk(thirdPartyArtifact)) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }
        return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
    }

    private boolean licenseStateOk(ThirdPartyArtifact thirdPartyArtifact) {
        return thirdPartyArtifact.getLicenseState() == LicenseState.EXPLICITLY_SET
                || thirdPartyArtifact.getLicenses().size() <= 2;
    }

    @Override
    public String getId() {
        return "QualifiedComplexLicense";
    }

    @Override
    public String getName() {
        return "Qualified Complex License Rule";
    }

    @Override
    public String getDescription() {
        return "Artifact has a complex license situation, i.e., more than 2 licenses involved. " +
                "It should be approved by a compliance expert!";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.SEVERE;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
