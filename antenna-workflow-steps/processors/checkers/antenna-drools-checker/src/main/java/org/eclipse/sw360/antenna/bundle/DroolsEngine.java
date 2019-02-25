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
import org.eclipse.sw360.antenna.api.IRuleEngine;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DroolsEngine implements IRuleEngine {
    private Logger logger = LoggerFactory.getLogger(DroolsEngine.class);
    private String rulesetDirectory = "";
    private List<String> rulesetPaths = new ArrayList<>();
    private static final String RULES_SUBFOLDER = "rules";
    private static final String POLICIES_PROPERTIES_FILENAME = "policies.properties";
    private static final String POLICIES_FILENAME = "policies.xml";
    private static final String POLICIES_VERSION = "policies.version";
    private static final String NO_VERSION = "no version string specified";

    public void setRulesetDirectory(String rulesetDirectory) {
        this.rulesetDirectory = rulesetDirectory;
    }

    public void setRulesetPaths(List<String> rulesetPath) {
        this.rulesetPaths = rulesetPath;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) throws AntennaException {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        addAllRules(kieFileSystem);

        KieSession kieSession = openKieSession(kieServices, kieFileSystem);
        artifacts.forEach(kieSession::insert);

        List<IEvaluationResult> evaluationResults = getEvaluationResults();
        evaluationResults.forEach(kieSession::insert);

        int rulesFired = kieSession.fireAllRules();
        logger.info(rulesFired + " drools rules fired");

        DroolsPolicyEvaluation evaluation = new DroolsPolicyEvaluation();
        evaluationResults.forEach(evaluation::addEvaluationResult);

        return evaluation;
    }

    private void addAllRules(KieFileSystem kieFileSystem) throws AntennaException {
        List<File> ruleFiles = getAllRulesInConfiguredFolders();
        if (ruleFiles.isEmpty()) {
            throw new AntennaException("No rules provided. Please check whether the rules are installed at " +
                    rulesetPaths.stream()
                            .map(path -> Paths.get(rulesetDirectory, path).normalize().toString() + ";")
                            .collect(Collectors.joining(";")));
        }

        ruleFiles.forEach(ruleFile ->
                kieFileSystem.write(ResourceFactory.newFileResource(ruleFile).setResourceType(ResourceType.DRL))
        );
    }

    private KieSession openKieSession(KieServices kieServices, KieFileSystem kieFileSystem) {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        KieRepository kieRepository = kieServices.getRepository();
        KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        return kieContainer.newKieSession();
    }

    private List<File> getAllRulesInConfiguredFolders() {
        return rulesetPaths.stream()
                .map(this::getAllRulesInConfiguredFolder)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<File> getAllRulesInConfiguredFolder(String path) {
        Path folderPath = Paths.get(rulesetDirectory, path, RULES_SUBFOLDER).normalize();
        try {
            DroolsRuleFolderVisitor collectRuleFiles = new DroolsRuleFolderVisitor();
            Files.walkFileTree(folderPath, collectRuleFiles);
            return collectRuleFiles.getRuleFiles();
        } catch (IOException ex) {
            // Code is unreachable, DroolsRuleFolderVisitor will handle all IOExceptions
            throw new AntennaExecutionException("Exception when scanning directory" + folderPath.toString());
        }
    }

    private List<IEvaluationResult> getEvaluationResults() throws AntennaException {
        List<IEvaluationResult> resultList = new ArrayList<>();
        for(String path : rulesetPaths) {
            resultList.addAll(DroolsEvaluationResultReader.getEvaluationResult(Paths.get(rulesetDirectory, path, POLICIES_FILENAME)));
        }
        return resultList;
    }

    @Override
    public Optional<String> getRulesetVersion() {
        return Optional.of(rulesetPaths.stream()
                .map(rulesetPath -> {
                    Properties appProperties = new Properties();
                    Path policiesVersionPath = Paths.get(rulesetDirectory, rulesetPath, POLICIES_PROPERTIES_FILENAME).normalize();
                    try (FileInputStream policiesFile = new FileInputStream((policiesVersionPath.toFile()));
                         InputStream policiesStream = new DataInputStream(policiesFile)) {
                        appProperties.load(policiesStream);
                        return rulesetPath + ":" + Optional.ofNullable(appProperties.getProperty(POLICIES_VERSION)).orElse(NO_VERSION);
                    } catch (IOException ex) {
                        logger.warn("Could not find or read version file. Details: " + ex.getMessage());
                        return rulesetPath + ":" + NO_VERSION;
                    }
                })
                .collect(Collectors.joining(";")));
    }
}
