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

import org.eclipse.sw360.antenna.policy.engine.CompareArtifactRule;
import org.eclipse.sw360.antenna.policy.engine.PolicyViolation;
import org.eclipse.sw360.antenna.policy.engine.RuleSeverity;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseData;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class EPLvsGPLRule implements CompareArtifactRule {
    private final Ruleset ruleset;

    public EPLvsGPLRule(final Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(final ThirdPartyArtifact leftArtifact,
            final ThirdPartyArtifact rightArtifact) {
        if (hasLicense(leftArtifact, "EPL-1.0")
                && hasLicense(rightArtifact, "GPL-2.0-or-later")) {
            return artifactRaisesPolicyViolation(this, leftArtifact, rightArtifact);
        }
        return artifactAppliesToRule(this, leftArtifact, rightArtifact);
    }

    private boolean hasLicense(ThirdPartyArtifact artifact, String license) {
        return artifact.getLicenses().stream()
                .map(LicenseData::getLicenseId)
                .filter(id -> id.equals(license))
                .count() > 0;
    }

    @Override
    public String getId() {
        return AntennaTestdata.EPL_VS_GPL_ID;
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
        return RuleSeverity.CRITICAL;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
