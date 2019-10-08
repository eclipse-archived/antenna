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

public class ArtifactLicenseKnownRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public ArtifactLicenseKnownRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact thirdPartyArtifact) {
        if (thirdPartyArtifact.isProprietary() || thirdPartyArtifact.getLicenseState() != LicenseState.NO_LICENSE) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }
        return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
    }

    @Override
    public String getId() {
        return "ArtifactLicenseKnown";
    }

    @Override
    public String getName() {
        return "Artifact License Known Rule";
    }

    @Override
    public String getDescription() {
        return "Artifact has no known license information, cannot be redistributed!";
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
