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

package org.eclipse.sw360.antenna.maven.workflow.processors.enricher;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.maven.ArtifactRequesterFactory;
import org.eclipse.sw360.antenna.maven.ClassifierInformation;
import org.eclipse.sw360.antenna.maven.IArtifactRequester;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactJar;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactSourceJar;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MavenArtifactResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenArtifactResolver.class);
    private static final String PREFERRED_SOURCE_QUALIFIER = "preferredSourceClassifier";
    private static final String SOURCES_REPOSITORY_URL = "sourcesRepositoryUrl";
    private Path dependencyTargetDirectory;
    private List<ArtifactSelector> sourceResolvingBlacklist;
    private String preferredSourceQualifier;
    private URL sourcesRepositoryUrl;

    public MavenArtifactResolver() {
        this.workflowStepOrder = 300;
    }

    private boolean isIgnoredForSourceResolving(Artifact artifact) {
        return sourceResolvingBlacklist.stream()
                .anyMatch(artifactSelector -> artifactSelector.matches(artifact));
    }

    /**
     * Downloads the maven artifacts for the given lists, if possible.
     *
     * @param artifacts to be resolved
     */
    private void resolveArtifacts(Collection<Artifact> artifacts) throws AntennaException {
        // Check directory exists
        File dir = dependencyTargetDirectory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<Artifact> filteredArtifacts = artifacts.stream()
                .filter(getFilterPredicate())
                .filter(artifact -> !isIgnoredForSourceResolving(artifact))
                .collect(Collectors.toList());
        for (Artifact artifact : filteredArtifacts) {
            resolve(artifact, dependencyTargetDirectory);
        }
    }

    private Predicate<Artifact> getFilterPredicate() {
        return artifact -> {
            final Optional<MavenCoordinates> mavenCoordinates = artifact.askFor(MavenCoordinates.class);
            return !artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY) &&
                    mavenCoordinates.isPresent() &&
                    mavenCoordinates.get().getGroupId() != null &&
                    mavenCoordinates.get().getArtifactId() != null;
        };
    }

    private void resolve(Artifact artifact, Path dependencyTargetDirectory) throws AntennaException {
        Optional<MavenCoordinates> oCoordinates = artifact.askFor(MavenCoordinates.class);
        if (!oCoordinates.isPresent()) {
            return;
        }
        MavenCoordinates coordinates = oCoordinates.get();

        IArtifactRequester artifactRequester = sourcesRepositoryUrl != null
                ? ArtifactRequesterFactory.getArtifactRequester(context, sourcesRepositoryUrl)
                : ArtifactRequesterFactory.getArtifactRequester(context);

        // Try to download source with preferred qualifier first
        if (!artifact.getSourceFile().isPresent() && preferredSourceQualifier != null) {
            Optional<File> sourceJar = artifactRequester.requestFile(coordinates, dependencyTargetDirectory, new ClassifierInformation(preferredSourceQualifier, true));
            sourceJar.ifPresent(sourceJarFile -> artifact.addFact(new ArtifactSourceJar(sourceJarFile.toPath())));
        }

        if (!artifact.getSourceFile().isPresent()) {
            Optional<File> sourceJar = artifactRequester.requestFile(coordinates, dependencyTargetDirectory, ClassifierInformation.DEFAULT_SOURCE_JAR);
            sourceJar.ifPresent(sourceJarFile -> artifact.addFact(new ArtifactSourceJar(sourceJarFile.toPath())));
        }

        if (!artifact.getFile().isPresent()) {
            Optional<File> jar = artifactRequester.requestFile(coordinates, dependencyTargetDirectory, ClassifierInformation.DEFAULT_JAR);
            jar.ifPresent(jarFile -> artifact.addFact(new ArtifactJar(jarFile.toPath())));
        }

        if (!artifact.getSourceFile().isPresent() && !artifact.getFile().isPresent()) {
            reporter.add(artifact, MessageType.MISSING_SOURCES, "Maven Artifact Coordinates present but non resolvable sources (maybe sources are available using P2).");
        }
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) throws AntennaException {
        LOGGER.info("Resolve maven artifacts, be patient... this could take a long time");
        resolveArtifacts(artifacts);
        LOGGER.info("Resolve maven artifacts... done");
        return artifacts;

    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        dependencyTargetDirectory = context.getToolConfiguration().getDependenciesDirectory();
        sourceResolvingBlacklist = context.getConfiguration().getIgnoreForSourceResolving();
        String sourcesUrlString = configMap.get(SOURCES_REPOSITORY_URL);
        if (sourcesUrlString != null) {
            try {
                sourcesRepositoryUrl = new URL(sourcesUrlString);
            } catch (MalformedURLException e) {
                throw new AntennaConfigurationException("The URL in 'sourcesRepositoryUrl' is not valid.");
            }
        }
        preferredSourceQualifier = configMap.get(PREFERRED_SOURCE_QUALIFIER);
    }
}