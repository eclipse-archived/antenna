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

import cucumber.api.java.en.When;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class AddRulesAndEvaluations {
    private WhenStepsHelpers stepsHelpers;

    public AddRulesAndEvaluations(ScenarioState state) {
        this.stepsHelpers = new WhenStepsHelpers(state);
    }

    @When("^I use the rule \"([^\"]*)\"$")
    public void i_use_the_rule(String rule) throws FileNotFoundException, URISyntaxException {
        stepsHelpers.iUseTheRule(rule, this.getClass());
    }
}