/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven.workflow.processors.enricher;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.maven.ArtifactRequesterFactory;
import org.eclipse.sw360.antenna.maven.ClassifierInformation;
import org.eclipse.sw360.antenna.maven.IArtifactRequester;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactJar;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactSourceJar;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MavenArtifactResolverImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenArtifactResolverImpl.class);

    private final IProcessingReporter processingReporter;
    private final Path dependencyTargetDirectory;
    private final List<ArtifactSelector> sourceResolvingBlacklist;
    private final String preferredSourceQualifier;
    private final URL sourcesRepositoryUrl;
    private final ProxySettings proxySettings;
    private final Optional<RepositorySystem> optionalRepositorySystem;
    private final Optional<MavenProject> optionalMavenProject;
    private final Optional<LegacySupport> optionalLegacySupport;
    private final boolean isMavenInstalled;
    private final File basedir;

    public MavenArtifactResolverImpl(ProxySettings proxySettings,
                                     Optional<RepositorySystem> optionalRepositorySystem,
                                     Optional<MavenProject> optionalMavenProject,
                                     Optional<LegacySupport> optionalLegacySupport,
                                     Path dependencyTargetDirectory,
                                     List<ArtifactSelector> sourceResolvingBlacklist,
                                     String preferredSourceQualifier,
                                     URL sourcesRepositoryUrl,
                                     IProcessingReporter processingReporter,
                                     boolean isMavenInstalled,
                                     File basedir) {
        this.dependencyTargetDirectory = dependencyTargetDirectory;
        this.sourceResolvingBlacklist = sourceResolvingBlacklist;
        this.sourcesRepositoryUrl = sourcesRepositoryUrl;
        this.preferredSourceQualifier = preferredSourceQualifier;
        this.processingReporter = processingReporter;
        this.proxySettings = proxySettings;
        this.optionalRepositorySystem = optionalRepositorySystem;
        this.optionalMavenProject = optionalMavenProject;
        this.optionalLegacySupport = optionalLegacySupport;
        this.isMavenInstalled = isMavenInstalled;
        this.basedir = basedir;
    }

    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Resolve maven artifacts, be patient... this could take a long time");
        resolveArtifacts(artifacts);
        LOGGER.debug("Resolve maven artifacts... done");
        return artifacts;
    }

    /**
     * Downloads the maven artifacts for the given lists, if possible.
     *
     * @param artifacts to be resolved
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private void resolveArtifacts(Collection<Artifact> artifacts) {
        // Check directory exists
        File dir = dependencyTargetDirectory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        IArtifactRequester artifactRequester = getArtifactRequester();

        List<Artifact> filteredArtifacts = artifacts.stream()
                .filter(getFilterPredicate())
                .filter(artifact -> !isIgnoredForSourceResolving(artifact))
                .collect(Collectors.toList());
        for (Artifact artifact : filteredArtifacts) {
            resolve(artifact, artifactRequester, dependencyTargetDirectory);
        }
    }


    private void resolve(Artifact artifact, IArtifactRequester artifactRequester, Path dependencyTargetDirectory) {
        final Optional<Coordinate> oMavenPurl = artifact.getCoordinateForType(Coordinate.Types.MAVEN);
        if (!oMavenPurl.isPresent()) {
            return;
        }
        final Coordinate coordinate = oMavenPurl.get();
        if (coordinate.getName() == null ||
                coordinate.getNamespace() == null ||
                ! (Coordinate.Types.MAVEN.equals(coordinate.getType()))) {
            return;
        }

        // Try to download source with preferred qualifier first
        if (!artifact.getSourceFile().isPresent() && preferredSourceQualifier != null) {
            Optional<File> sourceJar = artifactRequester.requestFile(coordinate, dependencyTargetDirectory, new ClassifierInformation(preferredSourceQualifier, true));
            sourceJar.ifPresent(sourceJarFile -> artifact.addFact(new ArtifactSourceJar(sourceJarFile.toPath())));
        }

        if (!artifact.getSourceFile().isPresent()) {
            Optional<File> sourceJar = artifactRequester.requestFile(coordinate, dependencyTargetDirectory, ClassifierInformation.DEFAULT_SOURCE_JAR);
            sourceJar.ifPresent(sourceJarFile -> artifact.addFact(new ArtifactSourceJar(sourceJarFile.toPath())));
        }

        if (!artifact.getFile().isPresent()) {
            Optional<File> jar = artifactRequester.requestFile(coordinate, dependencyTargetDirectory, ClassifierInformation.DEFAULT_JAR);
            jar.ifPresent(jarFile -> artifact.addFact(new ArtifactJar(jarFile.toPath())));
        }

        if (!artifact.getSourceFile().isPresent() && !artifact.getFile().isPresent()) {
            processingReporter.add(artifact, MessageType.MISSING_SOURCES, "Maven Artifact Coordinates present but non resolvable sources (maybe sources are available using P2).");
        }
    }

    IArtifactRequester getArtifactRequester(){
        return sourcesRepositoryUrl != null
                ? ArtifactRequesterFactory.getArtifactRequester(
                optionalRepositorySystem, optionalMavenProject, optionalLegacySupport,
                basedir, proxySettings, isMavenInstalled, sourcesRepositoryUrl)
                : ArtifactRequesterFactory.getArtifactRequester(
                optionalRepositorySystem, optionalMavenProject, optionalLegacySupport,
                basedir, proxySettings, isMavenInstalled);
    }


    private boolean isIgnoredForSourceResolving(Artifact artifact) {
        return sourceResolvingBlacklist.stream()
                .anyMatch(artifactSelector -> artifactSelector.matches(artifact));
    }

    private Predicate<Artifact> getFilterPredicate() {
        return artifact -> !artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY);
    }

}
