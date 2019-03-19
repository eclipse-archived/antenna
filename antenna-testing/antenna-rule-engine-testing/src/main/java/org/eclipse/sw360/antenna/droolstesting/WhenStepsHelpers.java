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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.bundle.DroolsEvaluationResultReader;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class WhenStepsHelpers {
    private ScenarioState state;
    private static final String RELATIVE_PATH_TO_RULES_FOLDER = "../../../../../policies/rules/";
    private static final String RELATIVE_PATH_TO_POLICIES_XML = "../../../../../policies/policies.xml";

    public WhenStepsHelpers(ScenarioState state) {
        this.state = state;
    }

    public void iUseTheRule(String rule, Class<?> clazz) throws FileNotFoundException, AntennaException, URISyntaxException {
        addRule(rule, clazz);
        addEvaluations(clazz);
    }

    private void addRule(String rule, Class<?> clazz) throws FileNotFoundException {
        state.rule_resource = getUrlToRuleFile(rule + ".drl", clazz);
    }

    private void addEvaluations(Class<?> clazz) throws URISyntaxException, AntennaException {
        state.evaluations.addAll(DroolsEvaluationResultReader.getEvaluationResult(
                Paths.get(clazz.getResource(RELATIVE_PATH_TO_POLICIES_XML).toURI())));
    }

    private URL getUrlToRuleFile(String rule, Class<?> clazz) throws FileNotFoundException {
        URL resource = clazz.getResource(RELATIVE_PATH_TO_RULES_FOLDER + rule);

        if (resource == null) {
            throw new FileNotFoundException("Could not find file for rule " + rule + ".");
        }
        return resource;
    }
}
