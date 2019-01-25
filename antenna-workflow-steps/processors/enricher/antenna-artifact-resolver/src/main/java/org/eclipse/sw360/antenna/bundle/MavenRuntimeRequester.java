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
package org.eclipse.sw360.antenna.bundle;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MavenRuntimeRequester extends IArtifactRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRuntimeRequester.class);

    private final RepositorySystem repositorySystem;
    private final ArtifactRepository localRepository;
    private final List<ArtifactRepository> remoteRepositories;

    public MavenRuntimeRequester(AntennaContext context, RepositorySystem repositorySystem, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) {
        super(context);
        this.repositorySystem = repositorySystem;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }

    @Override
    public Optional<File> requestFile(MavenCoordinates coordinates, Path targetDirectory, boolean isSource) {
        if (isSource) {
            return requestFile(coordinates, targetDirectory, "java-source");
        }
        return requestFile(coordinates, targetDirectory, "jar");
    }

    private ArtifactResolutionResult doArtifactRequest(org.apache.maven.artifact.Artifact mvnArtifact) {
        ArtifactResolutionRequest artifactRequest = new ArtifactResolutionRequest();
        artifactRequest.setArtifact(mvnArtifact);
        artifactRequest.setRemoteRepositories(this.remoteRepositories);
        artifactRequest.setLocalRepository(this.localRepository);
        return repositorySystem.resolve(artifactRequest);
    }

    private Optional<File> requestFile(MavenCoordinates mavenCoordinates, Path targetDirectory, String type) {
        String groupId = mavenCoordinates.getGroupId();
        String artifactId = mavenCoordinates.getArtifactId();
        String version = mavenCoordinates.getVersion();

        org.apache.maven.artifact.Artifact mvnArtifact = repositorySystem.createArtifact(groupId, artifactId, version, type);
        ArtifactResolutionResult result = doArtifactRequest(mvnArtifact);

        if (!wasSuccessful(result, mvnArtifact)) {
            LOGGER.error("Could not successfully fetch " + type + " for artifact=[" + groupId + ":" + artifactId + ":" + version + "]");
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
        LOGGER.debug("successfully fetched " + type + " for artifact=[" + groupId + ":" + artifactId + ":" + version + "]");
        return Optional.of(targetFile);
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

    private boolean wasSuccessful(ArtifactResolutionResult result, org.apache.maven.artifact.Artifact mvnArtifact) {
        final List<Artifact> missingArtifacts = result.getMissingArtifacts();
        return missingArtifacts == null ||
                missingArtifacts.isEmpty() ||
                !missingArtifacts.contains(mvnArtifact);
    }
}
