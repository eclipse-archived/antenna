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
package org.eclipse.sw360.antenna.maven;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MavenRuntimeRequester extends IArtifactRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRuntimeRequester.class);

    private final RepositorySystem repositorySystem;
    private final ArtifactRepository localRepository;
    private final List<ArtifactRepository> remoteRepositories;

    public MavenRuntimeRequester(AntennaContext context, RepositorySystem repositorySystem, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories, Optional<URL> sourcesRepositoryUrl) {
        super(context);
        if (sourcesRepositoryUrl.isPresent()) {
            List<ArtifactRepository> repositories = new ArrayList<>();
            ArtifactRepository droolsRepo = new MavenArtifactRepository(
                    "userRepo",
                    sourcesRepositoryUrl.get().toString(),
                    new DefaultRepositoryLayout(),
                    new ArtifactRepositoryPolicy(),
                    new ArtifactRepositoryPolicy());
            repositories.add(droolsRepo);
            repositories.addAll(remoteRepositories);
            this.remoteRepositories = repositories;
        } else {
            this.remoteRepositories = remoteRepositories;
        }
        this.repositorySystem = repositorySystem;
        this.localRepository = localRepository;
        LOGGER.debug("Maven is running, using the MavenRuntimeRequester for artifact resolution");
    }

    @Override
    public Optional<File> requestFile(MavenCoordinates coordinates, Path targetDirectory, ClassifierInformation classifierInformation) {
        if (classifierInformation.isSource) {
            return requestFile(coordinates, targetDirectory, "java-source", classifierInformation);
        }
        return requestFile(coordinates, targetDirectory, "jar", classifierInformation);
    }

    private Optional<File> requestFile(MavenCoordinates mavenCoordinates, Path targetDirectory, String type, ClassifierInformation classifier) {
        String groupId = mavenCoordinates.getGroupId();
        String artifactId = mavenCoordinates.getArtifactId();
        String version = mavenCoordinates.getVersion();

        Artifact mvnArtifact = classifier.classifier.isEmpty()
                ? repositorySystem.createArtifact(groupId, artifactId, version, type)
                : repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type, classifier.classifier);

        ArtifactResolutionResult result = doArtifactRequest(mvnArtifact);

        String classifierExtension = classifier.classifier.isEmpty() ? "" : "-" + classifier.classifier;
        if (!wasSuccessful(result, mvnArtifact)) {
            LOGGER.error("Could not successfully fetch " + type + " for artifact=[" + groupId + ":" + artifactId + ":" + version + classifierExtension + "]");
            return Optional.empty();
        }
        File artifactFile = mvnArtifact.getFile();
        if (!artifactFile.exists()) {
            LOGGER.error("Fetching of File from repository was successful, but expected file=[" + artifactFile + "] does not exists");
            return Optional.empty();
        }
        File targetFile = targetDirectory.resolve(artifactFile.getName()).toFile();
        if (targetFile.exists()) {
            if (contentEquals(artifactFile, targetFile)) {
                LOGGER.info("File " + targetFile + " was already fetched previously");
                return Optional.of(targetFile);
            }
            LOGGER.warn("File " + targetFile + " was already fetched previously, but is different to " + artifactFile + ", fall back to using the second");
            return Optional.of(artifactFile);
        }
        copyFile(artifactFile, targetFile);
        LOGGER.debug("successfully fetched " + type + " for artifact=[" + groupId + ":" + artifactId + ":" + version + classifierExtension + "]");
        return Optional.of(targetFile);
    }

    private ArtifactResolutionResult doArtifactRequest(Artifact mvnArtifact) {
        LOGGER.debug("Resolving maven artifact " + mvnArtifact.getArtifactId() + " in repositories "
                + remoteRepositories.stream().map(ArtifactRepository::getUrl).collect(Collectors.joining(", ")));

        ArtifactResolutionRequest artifactRequest = new ArtifactResolutionRequest();
        artifactRequest.setArtifact(mvnArtifact);
        artifactRequest.setResolveTransitively(false);  // We only want the specific artifact, its dependencies will be handled separately
        artifactRequest.setRemoteRepositories(this.remoteRepositories);
        artifactRequest.setLocalRepository(this.localRepository);
        return repositorySystem.resolve(artifactRequest);
    }

    private boolean wasSuccessful(ArtifactResolutionResult result, Artifact mvnArtifact) {
        final List<ArtifactResolutionException> resolutionErrors = result.getErrorArtifactExceptions();
        final List<Artifact> missingArtifacts = result.getMissingArtifacts();
        return resolutionErrors.isEmpty() && (missingArtifacts == null ||
                missingArtifacts.isEmpty() ||
                !missingArtifacts.contains(mvnArtifact));
    }

    private boolean contentEquals(File artifactFile, File targetFile) {
        try {
            return FileUtils.contentEquals(artifactFile, targetFile);
        } catch (IOException e) {
            throw new AntennaExecutionException("File content could not be compared: " + e.getMessage(), e);
        }
    }

    private void copyFile(File artifactFile, File targetFile) {
        try {
            FileUtils.copyFile(artifactFile, targetFile);
        } catch (IOException e) {
            throw new AntennaExecutionException("File could not be copied: " + e.getMessage(), e);
        }
    }
}
