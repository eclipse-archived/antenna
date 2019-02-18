/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.workflow.processors.checkers.AbstractComplianceChecker;
import org.eclipse.sw360.antenna.workflow.processors.checkers.DefaultPolicyEvaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Validates the sources of the given artifacts. An artifact is not valid if : -
 * it has no sources and failOnMissingSources = true - it has incomplete
 * sources, which means, that to many class files of the jar have no
 * corresponding java file in the source jar and failOnIncompleteSources = true.
 * With the IArtifactFilter validForMissingSources and validForIncomplete
 * Sources can be specified if an artifact shall be handled as valid even if it
 * has no source jar an incomplete source jar.
 */
public class SourceValidator extends AbstractComplianceChecker {
    private IProcessingReporter reporter;
    private int threshold = 80;
    private Collection<ArtifactSelector> missingSourcesWhiteList = Collections.emptySet();
    private Collection<ArtifactSelector> incompleteSourcesWhiteList = Collections.emptySet();
    private IEvaluationResult.Severity missingSourcesSeverity;
    private IEvaluationResult.Severity incompleteSourcesSeverity;

    private boolean isArtifactAllowedToHaveNoSourceJar(Artifact artifact) {
        return missingSourcesWhiteList.stream()
                .anyMatch(artifactSelector -> artifactSelector.matches(artifact));
    }

    private boolean isArtifactAllowedToHaveIncompleteSources(Artifact artifact) {
        return isArtifactAllowedToHaveNoSourceJar(artifact) ||  incompleteSourcesWhiteList.stream()
                .anyMatch(artifactSelector -> artifactSelector.matches(artifact));
    }

    private List<IEvaluationResult> validateSources(Artifact artifact) {
        final Optional<Path> artifactSourceFile = artifact.getSourceFile();
        if (!artifactSourceFile.isPresent()){
            if (! isArtifactAllowedToHaveNoSourceJar(artifact)){
                return Collections.singletonList(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                        "SourceValidator::noSourceJar", "No sources-jar available.", missingSourcesSeverity, artifact));
            }else{
                return Collections.singletonList(
                        new DefaultPolicyEvaluation.DefaultEvaluationResult(
                                "SourceValidator::noSourceJar", "Artifact has no jar sources but is handled as valid.", IEvaluationResult.Severity.INFO, artifact));
            }
        }

        final Optional<Path> artifactFile = artifact.getFile();
        if (!artifactFile.isPresent()) {
            if (! isArtifactAllowedToHaveIncompleteSources(artifact)) {
                return Collections.singletonList(
                        new DefaultPolicyEvaluation.DefaultEvaluationResult(
                                "SourceValidator::noJar", "The artifact has no jar. The sources thus can not be verified", incompleteSourcesSeverity, artifact));
            }else{
                return Collections.singletonList(
                        new DefaultPolicyEvaluation.DefaultEvaluationResult(
                                "SourceValidator::noJar", "Artifact has no jar to verify sources but is handled as valid.", IEvaluationResult.Severity.INFO, artifact));
            }
        }

        if (isArtifactAllowedToHaveIncompleteSources(artifact)) {
            return Collections.emptyList();
        }

        try {
            return validateArtifactWithJars(artifact, artifactSourceFile.get(), artifactFile.get());
        } catch (IOException e) {
            String message = "An exception occured during source validation: " + e.getMessage();
            reporter.add(artifact, MessageType.PROCESSING_FAILURE, message);
            return Collections.singletonList(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                    "SourceValidator::noJar", message, IEvaluationResult.Severity.FAIL, artifact));
        }
    }

    private List<IEvaluationResult> validateArtifactWithJars(Artifact artifact, Path artifactSourceFile, Path artifactFile) throws IOException {
        JarFile jar = new JarFile(artifactFile.toFile());
        return validate(artifact, jar, artifactSourceFile.toFile());
    }


    private List<IEvaluationResult> validate(Artifact artifact, JarFile jar, File sourceJar) throws IOException {
        if (0 == sourceJar.length()) {
            return Collections.singletonList(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                    "SourceValidator::jarIsEmpty", "Source jar '" + sourceJar.getName() + "' is an empty file", missingSourcesSeverity, artifact));
        }

        JarFile source = new JarFile(sourceJar);

        List<String> missingClasses = new ArrayList<>();

        int numberOfClassFiles = 0;
        int numberOfMatchingJavaFiles = 0;
        Enumeration<JarEntry> jarEntries = jar.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry entry = jarEntries.nextElement();
            String name = entry.getName();
            if (name.contains(".class") && !name.contains("$")) {
                numberOfClassFiles++;
                if(checkIfJavaFileExists(name, source, missingClasses)){
                    numberOfMatchingJavaFiles++;
                }
            }
        }
        if (numberOfClassFiles > 0 && numberOfMatchingJavaFiles <= (numberOfClassFiles * threshold / 100)) {
            return Collections.singletonList(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                    "SourceValidator::incompleteJar", "The sources are incomplete (only " + numberOfMatchingJavaFiles + " of " + numberOfClassFiles + " could be matched)", incompleteSourcesSeverity, artifact));
        }
        return Collections.emptyList();
    }

    /**
     * Checks for the given class file if a corresponding java file exists in
     * the specified source jar.
     *
     * @param className
     *            Name of the class for which the corresponding java file will
     *            be searched.
     * @param source
     *            Source file in which is looked after the java file.
     * @param missingClasses
     *            List of all missing Classes of an Artifact.
     * @return True if a corresponding java file exists, false otherwise.
     */
    private boolean checkIfJavaFileExists(String className, JarFile source, List<String> missingClasses) {
        className = className.replace(".class", ".java");
        JarEntry temp = source.getJarEntry(className);
        if (null == temp) {
            missingClasses.add(className);
            return false;
        }
        return true;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
        DefaultPolicyEvaluation policyEvaluation = new DefaultPolicyEvaluation();

        artifacts.stream()
                .filter(artifact -> ! artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY))
                .filter(artifact ->  {
                    final Optional<MavenCoordinates> mavenCoordinates = artifact.askFor(MavenCoordinates.class);
                    return mavenCoordinates.isPresent() && ! mavenCoordinates.get().isEmpty();
                })
                .forEach(artifact -> validateSources(artifact)
                        .forEach(policyEvaluation::addEvaluationResult));

        return policyEvaluation;
    }

    @Override
    public String getRulesetDescription() {
        return "Source Validator";
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);

        threshold = Integer.parseInt(getConfigValue("threshold", configMap, "80"));

        missingSourcesWhiteList = context.getConfiguration().getValidForMissingSources();
        incompleteSourcesWhiteList = context.getConfiguration().getValidForIncompleteSources();

        final String MISSING_SOURCES_KEY = "missingSourcesSeverity";
        final String INCOMPLETE_SOURCES_KEY = "incompleteSourcesSeverity";
        missingSourcesSeverity = getSeverityFromConfig(MISSING_SOURCES_KEY, configMap, IEvaluationResult.Severity.FAIL);
        incompleteSourcesSeverity = getSeverityFromConfig(INCOMPLETE_SOURCES_KEY, configMap, IEvaluationResult.Severity.WARN);

        reporter = context.getProcessingReporter();
    }
}
