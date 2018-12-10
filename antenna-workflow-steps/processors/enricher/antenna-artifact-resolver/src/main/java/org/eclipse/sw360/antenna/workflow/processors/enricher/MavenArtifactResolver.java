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

package org.eclipse.sw360.antenna.workflow.processors.enricher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactJar;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactSourceJar;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.bundle.ArtifactRequesterFactory;
import org.eclipse.sw360.antenna.bundle.IArtifactRequester;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.reporting.MessageType;

public class MavenArtifactResolver extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenArtifactResolver.class);
    private Path dependencyTargetDirectory;
    private List<ArtifactSelector> sourceResolvingBlacklist;

    private boolean isIgnoredForSourceResolving(Artifact artifact){
        return sourceResolvingBlacklist.stream()
                .anyMatch(artifactSelector -> artifactSelector.matches(artifact));
    }

    /**
     * Downloads the maven artifacts for the given lists, if possible.
     *
     * @param artifacts
     *            to be resolved
     */
    private void resolveArtifacts(Collection<Artifact> artifacts) {
        // Check directory exists
        File dir = dependencyTargetDirectory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        artifacts.stream()
                .filter(getFilterPredicate())
                .filter(artifact -> ! isIgnoredForSourceResolving(artifact))
                .forEach(artifact -> resolve(artifact, dependencyTargetDirectory));
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

    private void resolve(Artifact artifact, Path dependencyTargetDirectory) {
        Optional<MavenCoordinates> oCoordinates = artifact.askFor(MavenCoordinates.class);
        if(! oCoordinates.isPresent()) {
            return;
        }
        MavenCoordinates coordinates = oCoordinates.get();

        IArtifactRequester artifactRequester = ArtifactRequesterFactory.getArtifactRequester(context);

        // Does artifact exist in repo?
        boolean sourceExists = false, jarExists = false;

        try {
            if (! artifact.getSourceFile().isPresent()) {
                File sourceJar = artifactRequester.requestFile(coordinates, dependencyTargetDirectory, true);
                if(sourceJar != null) {
                    artifact.addFact(new ArtifactSourceJar(sourceJar.toPath()));
                    sourceExists = true;
                }
            } else {
                sourceExists = true;
            }
        } catch (ArtifactDoesNotExistException e) {
            LOGGER.warn("Failed to find source jar: ", e);
        } catch (IOException e) {
            throw new AntennaExecutionException("Downloading sources failed", e);
        }

        try {
            if (! artifact.getFile().isPresent()) {
                File jar = artifactRequester.requestFile(coordinates, dependencyTargetDirectory, false);
                if(jar != null) {
                    artifact.addFact(new ArtifactJar(jar.toPath()));
                    jarExists = true;
                }
            } else {
                jarExists = true;
            }
        } catch (ArtifactDoesNotExistException e) {
            LOGGER.warn("Failed to find jar: ", e);
        } catch (IOException e) {
            throw new AntennaExecutionException("Downloading jar failed", e);
        }

        if (!sourceExists && !jarExists) {
            reporter.add(artifact, MessageType.MISSING_SOURCES, "Maven Artifact Coordinates present but non resolvable sources (maybe sources are available using P2).");
        }
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Resolve maven artifacts, be patient... this could take a long time");
        resolveArtifacts(artifacts);
        LOGGER.info("Resolve maven artifacts... done");
        return artifacts;

    }

    @Override
    public void configure(Map<String, String> configMap) {
        dependencyTargetDirectory = context.getToolConfiguration().getDependenciesDirectory();
        sourceResolvingBlacklist = context.getConfiguration().getIgnoreForSourceResolving();
    }
}