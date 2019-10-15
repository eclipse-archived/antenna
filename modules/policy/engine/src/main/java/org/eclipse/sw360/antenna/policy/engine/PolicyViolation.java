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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The result object for a {@link Rule} violation. The {@link PolicyEngine} creates for violations an object of this type
 * and returns all {@link ThirdPartyArtifact} objects which have caused the policy violation.
 */
public class PolicyViolation {
    private Rule rule;
    private Collection<ThirdPartyArtifact> failingArtifacts = new HashSet<>();

    PolicyViolation(final Rule rule, final Collection<ThirdPartyArtifact> failingArtifacts) {
        this.rule = rule;
        this.failingArtifacts.addAll(failingArtifacts);
    }

    /**
     * @return Business id of the associated rule
     */
    public String getId() {
        return rule.getId();
    }

    /**
     * @return Business getName of the associated rule
     */
    public String getName() {
        return rule.getName();
    }

    /**
     * @return Violation description of the associated rule
     */
    public String getDescription() {
        return rule.getDescription();
    }

    /**
     * @return Configured severity of the associated rule
     */
    public RuleSeverity getSeverity() {
        return rule.getSeverity();
    }

    /**
     * @return A hash code that uniquely identifies the violation. It is reproducable over different rule evaluations
     */
    public String getViolationHash() {
        String hashBase = failingArtifacts.stream()
                .map(this::getPurlAsString)
                .flatMap(Collection::stream)
                .collect(Collectors.joining(" : ", rule.getId() + " : ", ""));
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(hashBase.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Programming Error: Hash algorithm, resp. encoding unknown");
        }
    }

    private Collection<String> getPurlAsString(ThirdPartyArtifact artifact) {
        return artifact.getCoordinates().stream()
                .map(Coordinate::canonicalize)
                .collect(Collectors.toList());
    }

    /**
     * @return Associated rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @return All artifacts that caused a policy violation
     */
    public Collection<ThirdPartyArtifact> getFailingArtifacts() {
        return Collections.unmodifiableCollection(failingArtifacts);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PolicyViolation) {
            PolicyViolation other = (PolicyViolation) obj;
            if (rule.getId().equalsIgnoreCase(other.rule.getId())
                    && failingArtifacts.size() == other.failingArtifacts.size()
                    && failingArtifacts.containsAll(other.failingArtifacts)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rule, failingArtifacts);
    }
}
