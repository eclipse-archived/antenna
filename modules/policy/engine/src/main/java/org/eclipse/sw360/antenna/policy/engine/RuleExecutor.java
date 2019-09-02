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

/**
 * Interface that implements an executor that goes through all {@link Rule} instances of the corresponding type and
 * applies the rule on the available data. The design pattern is, that every type of rules has a corresponding executor.
 * The rule is reduced to a function that checks a condition. If the condition is false, the rule returns a
 * {@link PolicyViolation} that allows the executor to identify failing artifacts.
 */
interface RuleExecutor {
    Collection<PolicyViolation> executeRules(Collection<ThirdPartyArtifact> thirdPartyArtifacts);

    Collection<Ruleset> getRulesets();
}
