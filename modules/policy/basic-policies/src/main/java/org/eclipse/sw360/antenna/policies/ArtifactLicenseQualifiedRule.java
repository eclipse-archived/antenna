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

import org.eclipse.sw360.antenna.policy.engine.*;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseState;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class ArtifactLicenseQualifiedRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public ArtifactLicenseQualifiedRule(Ruleset ruleset) {
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
        return thirdPartyArtifact.getLicenseState() == LicenseState.DECLARED_AND_OBSERVED ||
                thirdPartyArtifact.getLicenseState() == LicenseState.EXPLICITLY_SET;
    }

    @Override
    public String getId() {
        return "ArtifactLicenseQualified";
    }

    @Override
    public String getName() {
        return "Artifact License Qualified Rule";
    }

    @Override
    public String getDescription() {
        return "The artifact needs either a declared and an observed license or an explicit license statement. " +
                "This is not the case, so the valid component license is unclear!";
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
