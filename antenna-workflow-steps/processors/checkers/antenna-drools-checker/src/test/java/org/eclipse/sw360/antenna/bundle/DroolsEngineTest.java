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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import static org.assertj.core.api.Assertions.assertThat;

public class DroolsEngineTest {

    private static final String RESOURCE_PATH = "../../../../../policies/policies.properties";
    private DroolsEngine droolsEngine;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource(RESOURCE_PATH);
        Path path = Paths.get(resource.toURI());
        String resourcesFolderPath = path.getParent().getParent().toString();

        droolsEngine = new DroolsEngine();
        droolsEngine.setRulesetDirectory(resourcesFolderPath);
        droolsEngine.setRulesetPath("policies");
    }

    @Test
    public void evaluateIdentifiesNonOSSLicensesAsFailuresWithDummyRule() throws AntennaException {
        Artifact artifact1 = new Artifact();
        artifact1.setProprietary(false);

        Artifact artifact2 = new Artifact();
        artifact2.setProprietary(true);

        IPolicyEvaluation evaluationResults = droolsEngine.evaluate(Arrays.asList(artifact1, artifact2));

        List<Artifact> failedArtifacts = getAllFailedArtifacts(evaluationResults);

        assertThat(failedArtifacts).containsExactly(artifact2);
    }

    @Test(expected = AntennaException.class)
    public void evaluateThrowsAnExceptionIfRulesetCannotBeFound() throws AntennaException {
        droolsEngine = new DroolsEngine();
        droolsEngine.setRulesetDirectory("");
        droolsEngine.evaluate(Collections.emptyList());
    }

    @Test
    public void getVersionsCanReadPropertiesFile() {
        assertThat(droolsEngine.getRulesetVersion()).contains("0.0.0");
    }

    private List<Artifact> getAllFailedArtifacts(IPolicyEvaluation evaluationResults) {
        return evaluationResults.getEvaluationResults()
                    .stream()
                    .map(IEvaluationResult::getFailedArtifacts)
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
    }

}