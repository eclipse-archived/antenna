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
import org.eclipse.sw360.antenna.policy.engine.Ruleset;

import java.util.Arrays;
import java.util.Collection;

public class TestRuleset implements Ruleset {
    @Override
    public String getName() {
        return "TestRuleset";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Collection<Rule> getRules() {
        return Arrays.asList(new AlwaysViolationRule(this), new NeverViolationRule(this));
    }
}
