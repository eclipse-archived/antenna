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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * The result object for a {@link Rule} violation. The {@link PolicyEngine} creates for violation an object of this type
 * and returns all {@link ThirdPartyArtifact} objects which have caused the policy violation.
 */
public class ViolationIssue {
    private Rule ruleForResult;
    private Collection<ThirdPartyArtifact> failedArtifacts = new HashSet<>();

    ViolationIssue(final Rule ruleForResult, final Collection<ThirdPartyArtifact> failedArtifacts) {
        this.ruleForResult = ruleForResult;
        this.failedArtifacts.addAll(failedArtifacts);
    }

    /**
     * @return Business id of the associated rule
     */
    public String getId() {
        return ruleForResult.getId();
    }

    /**
     * @return Business name of the associated rule
     */
    public String getName() {
        return ruleForResult.getName();
    }

    /**
     * @return Violation description of the associated rule
     */
    public String getDescription() {
        return ruleForResult.getDescription();
    }

    /**
     * @return Configured severity of the associated rule
     */
    public RuleSeverity getSeverity() {
        return ruleForResult.getSeverity();
    }

    /**
     * @return Associated rule
     */
    public Rule getRule() {
        return ruleForResult;
    }

    /**
     * @return All artifacts that caused a policy violation
     */
    public Collection<ThirdPartyArtifact> getFailedArtifacts() {
        return Collections.unmodifiableCollection(failedArtifacts);
    }
}
