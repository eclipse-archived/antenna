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
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Rule {
    private final String name;
    private final String pathToRule;
    private AbstractDroolsValidatorTest validatorTest;
    private List<Artifact> artifacts;
    private List<IEvaluationResult> evaluations;

    Rule(AbstractDroolsValidatorTest test, String pathToRule, String rule, List<Artifact> artifacts, List<IEvaluationResult> evaluations) {
        this.validatorTest = test;
        this.artifacts = artifacts;
        this.evaluations = evaluations;
        this.name = rule;
        this.pathToRule = pathToRule;
    }

    /**
     * Run the rule. Using all artifacts and policy evaluations added before creating the rule, this command runs the
     * rules and outputs all artifacts that fail the rules grouped by policy evaluation. In the sw360antenna workflow step,
     * this list is then processed further.
     *
     * @return results of running the rule, i.e. a list of all evaluation results containing all failed artifacts together
     *   with the severity evaluation.
     * @throws FileNotFoundException if the rule file cannot be found in the resource folder
     */
    public RuleResults whenRunning() throws FileNotFoundException {
        URL resource = getUrlToRuleFile(pathToRule + "/" + name);
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        File ruleFile = new File(resource.getPath());
        kieFileSystem.write(ResourceFactory.newFileResource(ruleFile).setResourceType(ResourceType.DRL));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        KieRepository kieRepository = kieServices.getRepository();
        KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        KieSession kieSession = kieContainer.newKieSession();

        artifacts.forEach(kieSession::insert);
        evaluations.forEach(kieSession::insert);

        int rulesFired = kieSession.fireAllRules();

        return new RuleResults(name, evaluations);
    }

    private URL getUrlToRuleFile(String rule) throws FileNotFoundException {
        URL resource = validatorTest.getClass().getResource(rule);

        if (resource == null) {
            throw new FileNotFoundException("Could not find file for rule " + rule + ".");
        }
        return resource;
    }
}
