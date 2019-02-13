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

package org.eclipse.sw360.antenna.droolstesting;

import org.eclipse.sw360.antenna.api.IEvaluationResult;

import java.util.List;

public class RuleResults {
    private List<IEvaluationResult> evaluations;
    private String name;

    RuleResults(String name, List<IEvaluationResult> evaluations) {
        this.evaluations = evaluations;
        this.name = name;
    }

    List<IEvaluationResult> getEvaluations() {
        return evaluations;
    }

    String getName() {
        return name;
    }
}
