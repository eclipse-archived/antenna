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
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class UnknownArtifactRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public UnknownArtifactRule(final Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(final ThirdPartyArtifact thirdPartyArtifact) {
        if(thirdPartyArtifact.getCoordinates().size() == 0) {
            return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
        }
        return artifactAppliesToRule(this, thirdPartyArtifact);
    }

    @Override
    public String getId() {
        return AntennaTestdata.UNKNOWN_ARTIFACT_ID;
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
        return RuleSeverity.SEVERE;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
