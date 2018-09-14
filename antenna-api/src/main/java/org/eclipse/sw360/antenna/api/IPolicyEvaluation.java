/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.api;

import java.util.Set;

/**
 * Encapsulates the results of an evaluation by a rule engine.
 */
public interface IPolicyEvaluation {
    /**
     * @return A result set of the policy evaluation. Each result describes the
     *         application of a rule and its result.
     */
    Set<IEvaluationResult> getEvaluationResults();
}
