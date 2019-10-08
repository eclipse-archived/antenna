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
package org.eclipse.sw360.antenna.policy.engine.testdata;

import org.eclipse.sw360.antenna.policy.engine.Rule;
import org.eclipse.sw360.antenna.policy.engine.RuleSeverity;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;

public class TestRule implements Rule {
    private final Ruleset ruleset;

    public TestRule(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    @Override
    public String getId() {
        return "TST";
    }

    @Override
    public String getName() {
        return "Test Rule";
    }

    @Override
    public String getDescription() {
        return "Nothing";
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.WARN;
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }
}
