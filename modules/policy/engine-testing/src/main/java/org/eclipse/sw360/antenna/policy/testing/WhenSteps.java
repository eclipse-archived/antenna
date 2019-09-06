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
package org.eclipse.sw360.antenna.policy.testing;

import cucumber.api.java.en.When;
import org.eclipse.sw360.antenna.policy.engine.Ruleset;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

public class WhenSteps {
    private ScenarioState state;

    public WhenSteps(ScenarioState state) {
        this.state = state;
    }

    @When("^I use the rule \"([^\"]*)\"$")
    public void i_use_the_rule(String rule) {
        Reflections searchEngine = new Reflections("");
        Set<Class<? extends Ruleset>> rulesets = searchEngine.getSubTypesOf(Ruleset.class);
        Ruleset relevantRuleset = rulesets.stream()
                .map(this::createRuleset)
                .filter(rs -> containsRule(rs, rule))
                .findFirst().orElseThrow(IllegalStateException::new);
        state.rulesets = Arrays.asList(relevantRuleset.getClass().getName());

    }

    private boolean containsRule(Ruleset ruleset, String ruleId) {
        return ruleset.getRules().stream().anyMatch(rule -> ruleId.equalsIgnoreCase(rule.getId()));
    }

    private Ruleset createRuleset(Class<? extends Ruleset> rulesetclass) {
        try {
            return (Ruleset) rulesetclass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
