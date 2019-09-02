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

/**
 * Interface for a generic rule with the standard fields relevant for all policy rules.
 */
public interface Rule {
    /**
     * @return The business id of the rule defined by the policy responsible
     */
    String getId();

    /**
     * @return The business getName of the rule defined by the policy responsible
     */
    String getName();

    /**
     * @return The description given to the user of the policy in case of a policy violation
     */
    String getDescription();

    /**
     * @return The configured severity of a policy violation
     */
    RuleSeverity getSeverity();

    /**
     * @return The associated {@link Ruleset} from which the rule originates
     */
    Ruleset getRuleset();
}
