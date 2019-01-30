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

package org.eclipse.sw360.antenna.bundle;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;

import java.util.HashSet;
import java.util.Set;

public class DroolsPolicyEvaluation implements IPolicyEvaluation {
    private Set<IEvaluationResult> evaluationResults = new HashSet<>();

    @Override
    public Set<IEvaluationResult> getEvaluationResults() {
        return evaluationResults;
    }

    public void addEvaluationResult(IEvaluationResult result) {
        this.evaluationResults.add(result);
    }
}
