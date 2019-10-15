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
package org.eclipse.sw360.antenna.policy.engine;

import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.policy.engine.model.ThirdPartyArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class with static utility methods to be usable within {@link Rule} implementations
 */
public class RuleUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyEngine.class);

    public static Optional<PolicyViolation> artifactRaisesPolicyViolation(Rule rule,
            ThirdPartyArtifact... failingArtifacts) {
        LOGGER.debug(
                String.format("Artifacts %s raises a policy violation for rule %s", getArtifactString(failingArtifacts),
                        rule.getId()));
        return Optional.of(new PolicyViolation(rule, Arrays.asList(failingArtifacts)));
    }

    public static Optional<PolicyViolation> artifactAppliesToRule(Rule rule, ThirdPartyArtifact... cleanArtifacts) {
        LOGGER.debug(
                String.format("Artifacts %s are clean for rule %s", getArtifactString(cleanArtifacts), rule.getId()));
        return Optional.empty();
    }

    private static String getArtifactString(ThirdPartyArtifact[] failingArtifacts) {
        return Arrays.asList(failingArtifacts).stream()
                .map(ThirdPartyArtifact::getCoordinates)
                .flatMap(Collection::stream)
                .map(Coordinate::canonicalize)
                .collect(Collectors.joining(" : ", "[ ", " ]"));
    }
}
