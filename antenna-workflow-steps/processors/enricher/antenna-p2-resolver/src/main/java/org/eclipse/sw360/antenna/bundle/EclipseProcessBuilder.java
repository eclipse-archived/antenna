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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * For P2 resolution, we rely on an eclipse project.
 * <p>
 * Since the eclipse project is an OSGi project and not easily embeddable into a maven plugin which can be integrated
 * into a maven-tycho build as well as a gradle plugin, we run it as a headless console application.
 * <p>
 * To pass information to the product, command line arguments are used. The format of these command line arguments
 * is defined here. The counterpart (receiver) is defined in ProjectArgumentExtractor in the antenna-p2-repository-manager
 */
public class EclipseProcessBuilder {
    private static final String LAUNCHER = "eclipse";
    private static final String DOWNLOAD_AREA = "-download-area";
    private static final String REPOSITORIES = "-repositories";
    private static final String ARTIFACTS = "-coordinates";

    public static ProcessBuilder setupEclipseProcess(File productInstallationArea, File artifactDownloadArea, Collection<Artifact> artifacts, List<String> repositories) {
        File eclipse_executable = productInstallationArea.toPath().resolve(LAUNCHER).normalize().toFile();
        eclipse_executable.setExecutable(true);

        return new ProcessBuilder(
                eclipse_executable.toPath().toString(),
                createArgument(DOWNLOAD_AREA, artifactDownloadArea.toString()),
                createArgument(REPOSITORIES, repositories(repositories)),
                createArgument(ARTIFACTS, extractBundleArtifacts(artifacts)));
    }

    private static String repositories(List<String> repositories) {
        return repositories.stream()
                .map(EclipseProcessBuilder::sanitizeUri)
                .collect(Collectors.joining(";"));
    }

    private static String sanitizeUri(String repository) {
        // File based URIs should be prepended with "file://" to work correctly.
        if (repository.startsWith("http") || repository.startsWith("https")) {
            return repository;
        }
        return Paths.get(repository).normalize().toUri().toString();
    }

    private static String extractBundleArtifacts(Collection<Artifact> artifacts) {
        return artifacts.stream()
                .map(artifact -> artifact.askFor(BundleCoordinates.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(bundleCoordinates -> bundleCoordinates.getSymbolicName() + "," + bundleCoordinates.getVersion() + "")
                .collect(Collectors.joining(";"));
    }

    private static String createArgument(String argumentKey, String argumentValue) {
        return argumentKey + " " + argumentValue;
    }
}
