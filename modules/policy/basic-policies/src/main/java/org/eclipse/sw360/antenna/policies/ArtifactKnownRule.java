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

import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.policy.engine.PolicyViolation;
import org.eclipse.sw360.antenna.policy.engine.RuleSeverity;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;
import org.eclipse.sw360.antenna.policy.engine.SingleArtifactRule;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class ArtifactKnownRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public ArtifactKnownRule(final Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(final ThirdPartyArtifact thirdPartyArtifact) {
        if (thirdPartyArtifact.isProprietary() || coordinatesAvailable(thirdPartyArtifact)) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }
        return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
    }

    private boolean coordinatesAvailable(final ThirdPartyArtifact thirdPartyArtifact) {
        return thirdPartyArtifact.getPurls().stream()
                .filter(purl -> purl.getType() != PackageURL.StandardTypes.GENERIC)
                .count() > 0;
    }

    @Override
    public String getId() {
        return "ArtifactIdentified";
    }

    @Override
    public String getName() {
        return "Artifact Known Rule";
    }

    @Override
    public String getDescription() {
        return "Artifact could not be identified, no technology specific coordinates available!";
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
