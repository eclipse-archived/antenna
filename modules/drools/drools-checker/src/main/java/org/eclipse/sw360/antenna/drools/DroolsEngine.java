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

package org.eclipse.sw360.antenna.drools;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.IRuleEngine;
import org.eclipse.sw360.antenna.api.IRulesPackage;
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DroolsEngine implements IRuleEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsEngine.class);
    private static final String RULES_SUBFOLDER = "rules";
    private static final String POLICIES_PROPERTIES_FILENAME = "policies.properties";
    private static final String POLICIES_FOLDER_NAME = "policies";
    private static final String POLICIES_FILENAME = "policies.xml";
    private static final String POLICIES_VERSION = "policies.version";
    private static final String NO_VERSION = "no version string specified";
    private static final String POLICIES_NAME = "policies.name";
    private static final String NO_NAME = "no policy name specified";

    private String rulesetDirectory = "";
    private List<String> rulesetPaths = new ArrayList<>();
    private Path temporaryDirectory;

    private List<InternalRulesPackage> internalRules = new ArrayList<>();
    private List<Path> resolvedPolicyFolderPaths = new ArrayList<>();
    private Optional<String> versionCache = Optional.empty();
    private boolean debug = false;

    public void setRulesetDirectory(String rulesetDirectory) {
        this.rulesetDirectory = rulesetDirectory;
    }

    public void setRulesetPaths(List<String> rulesetPath) {
        this.rulesetPaths = rulesetPath;
    }

    public void setTemporaryDirectory(Path temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) throws AntennaException {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        resolvePolicyFolders();

        addAllRules(kieFileSystem);

        KieSession kieSession = openKieSession(kieServices, kieFileSystem);

        artifacts.forEach(kieSession::insert);

        List<IEvaluationResult> evaluationResults = getEvaluationResults();
        evaluationResults.forEach(kieSession::insert);

        int rulesFired = kieSession.fireAllRules();
        LOGGER.info(rulesFired + " drools rules fired");

        DroolsPolicyEvaluation evaluation = new DroolsPolicyEvaluation();
        evaluationResults.forEach(evaluation::addEvaluationResult);

        if (!debug) {
            deleteExtractedRulesFolders();
        }
        return evaluation;
    }

    private void resolvePolicyFolders() throws AntennaException {
        if (resolvedPolicyFolderPaths.isEmpty()) {
            resolvedPolicyFolderPaths.addAll(extractInbuiltRuleFolders());
            resolvedPolicyFolderPaths.addAll(resolveRuleFoldersPaths());
        }
    }

    private List<Path> extractInbuiltRuleFolders() throws AntennaException {
        List<Path> policiesFolders = new ArrayList<>();
        for (IRulesPackage rulesPackage : ServiceLoader.load(IRulesPackage.class, getClass().getClassLoader())) {
            try {
                Path rulePath = Paths.get(rulesPackage.getRulesetFolder());
                if (rulePath.toFile().isDirectory()) {
                    LOGGER.error("Path to ruleset folder should be inside a jar. Your rulefolder is probably not packed.");
                    continue;
                }
                if (temporaryDirectory == null) {
                    temporaryDirectory = Files.createTempDirectory("temporaryDirectory");
                }
                Path internalRulesPath = temporaryDirectory.resolve("rules" + rulesPackage.hashCode());
                InternalRulesPackage rules = InternalRulesExtractor.extractRules(rulePath, internalRulesPath);
                internalRules.add(rules);
                policiesFolders.add(internalRulesPath.resolve(POLICIES_FOLDER_NAME));
            } catch (IOException e) {
                throw new AntennaException("Error while extracting rules from folder " + rulesPackage.getRulesPackageName() + ": ", e);
            } catch (URISyntaxException e) {
                throw new AntennaException(e.getReason() + "when getting policy folder.", e);
            }
        }
        return policiesFolders;
    }

    private List<Path> resolveRuleFoldersPaths() {
        return rulesetPaths.stream()
                .map(path -> Paths.get(rulesetDirectory, path).normalize())
                .collect(Collectors.toList());
    }

    private void addAllRules(KieFileSystem kieFileSystem) throws AntennaException {
        List<File> ruleFiles = getAllRuleFiles(resolvedPolicyFolderPaths);

        if (ruleFiles.isEmpty()) {
            throw new AntennaException("No rules provided. Please check whether the rules are installed at " +
                    rulesetPaths.stream()
                            .map(path -> Paths.get(rulesetDirectory, path).normalize().toString() + ";")
                            .collect(Collectors.joining(";")));
        }

        ruleFiles.forEach(ruleFile -> {
                    kieFileSystem.write(ResourceFactory.newFileResource(ruleFile).setResourceType(ResourceType.DRL));
                    LOGGER.debug("Adding drools rule " + ruleFile.toString());
                }
        );
    }

    private List<File> getAllRuleFiles(List<Path> folderPaths) {
        return folderPaths
                .stream()
                .map(this::getAllRulesInConfiguredFolder)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<File> getAllRulesInConfiguredFolder(Path path) {
        Path folderPath = path.resolve(RULES_SUBFOLDER).normalize();
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
        for (Path path : resolvedPolicyFolderPaths) {
            resultList.addAll(DroolsEvaluationResultReader.getEvaluationResult(path.resolve(POLICIES_FILENAME)));
        }
        return resultList;
    }

    private KieSession openKieSession(KieServices kieServices, KieFileSystem kieFileSystem) {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        KieRepository kieRepository = kieServices.getRepository();
        KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        return kieContainer.newKieSession();
    }

    private void deleteExtractedRulesFolders() {
        for (InternalRulesPackage internalRule : internalRules) {
            internalRule.close();
        }
        try {
            if (temporaryDirectory != null && temporaryDirectory.toFile().exists()) {
                Files.delete(temporaryDirectory);
            }
        } catch (IOException e) {
            LOGGER.error("Could not delete temporary directory " + temporaryDirectory + ": ", e);
        }
        resolvedPolicyFolderPaths = new ArrayList<>();
    }

    @Override
    public Optional<String> getRulesetVersion() {
        if (versionCache.isPresent()) {
            return versionCache;
        }

        try {
            resolvePolicyFolders();
            versionCache = getRulesetVersions();
            return versionCache;
        } catch (AntennaException ex) {
            LOGGER.error("Could not read versions correctly.", ex);
            return Optional.empty();
        }
    }

    private Optional<String> getRulesetVersions() {
        return Optional.of(resolvedPolicyFolderPaths.stream()
                .map(rulesetPath -> {
                    Properties appProperties = new Properties();
                    Path policiesVersionPath = rulesetPath.resolve(POLICIES_PROPERTIES_FILENAME).normalize();
                    try (FileInputStream policiesFile = new FileInputStream(policiesVersionPath.toFile());
                         InputStream policiesStream = new DataInputStream(policiesFile)) {
                        appProperties.load(policiesStream);
                        return Optional.ofNullable(appProperties.getProperty(POLICIES_NAME)).orElse(NO_NAME) + ":" +
                                Optional.ofNullable(appProperties.getProperty(POLICIES_VERSION)).orElse(NO_VERSION);
                    } catch (IOException ex) {
                        LOGGER.warn("Could not find or read version file. Details: " + ex.getMessage());
                        return rulesetPath + ":" + NO_VERSION;
                    }
                })
                .collect(Collectors.joining(";")));
    }
}
