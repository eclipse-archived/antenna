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
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactAppliesToRule;
import static org.eclipse.sw360.antenna.policy.engine.RuleUtils.artifactRaisesPolicyViolation;

public class SourcesAvailableRule implements SingleArtifactRule {
    private final Ruleset ruleset;

    public SourcesAvailableRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public Optional<PolicyViolation> evaluate(ThirdPartyArtifact thirdPartyArtifact) {
        if (thirdPartyArtifact.isProprietary()) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }

        if (thirdPartyArtifact.getSourceFileOrLink()
                .filter(url -> url.getProtocol().equals("file"))
                .map(this::urlToPath)
                .filter(Files::exists)
                .isPresent()) {
            return artifactAppliesToRule(this, thirdPartyArtifact);
        }

        return artifactRaisesPolicyViolation(this, thirdPartyArtifact);
    }

    private Path urlToPath(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public String getId() {
        return "SourcesAvailable";
    }

    @Override
    public String getName() {
        return "Sources Available Rule";
    }

    @Override
    public String getDescription() {
        return "The artifact sources are not available locally!";
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
