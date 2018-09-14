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
import java.util.function.Predicate;

import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.ArtifactSelector;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.bundle.ArtifactRequesterFactory;
import org.eclipse.sw360.antenna.bundle.IArtifactRequester;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;

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
     * @param list
     *            to be resolved
     */
    private void resolveArtifacts(Collection<Artifact> list) {
        // Check directory exists
        File dir = dependencyTargetDirectory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        list.stream()
                .filter(artifact -> artifact.getJar() == null)
                .filter(getFilterPredicate())
                .filter(artifact -> ! isIgnoredForSourceResolving(artifact))
                .forEach(artifact -> resolve(artifact, dependencyTargetDirectory));
    }

    private Predicate<Artifact> getFilterPredicate() {
        return artifact -> !artifact.isProprietary() &&
                artifact.getArtifactIdentifier().getMavenCoordinates().getGroupId() != null &&
                artifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId() != null;
    }

    private void resolve(Artifact artifact, Path dependencyTargetDirectory) {
        ArtifactIdentifier identifier = artifact.getArtifactIdentifier();
        IArtifactRequester artifactRequester = ArtifactRequesterFactory.getArtifactRequester(context);

        // Does artifact exist in repo?
        boolean sourceExists = true, jarExists = true;

        try {
            if (null == artifact.getMvnSourceJar()) {
                File sourceJar = artifactRequester.requestFile(identifier, dependencyTargetDirectory, true);
                artifact.setMavenSourceJar(sourceJar);
            }
        } catch (ArtifactDoesNotExistException e) {
            sourceExists = false;
        } catch (IOException e) {
            throw new AntennaExecutionException("Downloading sources failed", e);
        }

        try {
            if (null == artifact.getJar()) {
                File jar = artifactRequester.requestFile(identifier, dependencyTargetDirectory, false);
                artifact.setJar(jar);
            }
        } catch (ArtifactDoesNotExistException e) {
            jarExists = false;
        } catch (IOException e) {
            throw new AntennaExecutionException("Downloading jar failed", e);
        }

        if (null == artifact.getMvnSourceJar() && null == artifact.getJar() && (sourceExists || jarExists)) {
            ProcessingMessage processingMessage = new ProcessingMessage(MessageType.MISSING_SOURCES);
            processingMessage.setIdentifier(identifier);
            processingMessage.setMessage(
                    "Maven Artifact Coordinates present but non resolvable sources (maybe sources are available using P2).");
            if (!reporter.getProcessingReport().getMessageList().contains(processingMessage)) {
                reporter.add(processingMessage);
            }
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