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

package org.eclipse.sw360.antenna.p2resolver;

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;


public class ArtifactAttacher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAttacher.class);

    private Path targetDirectory;

    public ArtifactAttacher(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void copyDependencies(File artifactDownloadArea, Collection<Artifact> artifactsWithBundleCoordinates) {
        for (Artifact artifact : artifactsWithBundleCoordinates) {
            attachArtifacts(artifact, artifactDownloadArea);
        }
    }

    private void attachArtifacts(Artifact artifact, File artifactDownloadArea) {
        artifact.getCoordinateForType(Coordinate.Types.P2)
            .ifPresent(bundleCoordinate -> {
                try {
                    attachJar(artifact, artifactDownloadArea, bundleCoordinate);
                    attachSource(artifact, artifactDownloadArea, bundleCoordinate);
                } catch (IOException e) {
                    throw new ExecutionException("IOException: ", e);
                }
            });
    }

    private void attachSource(Artifact artifact, File artifactDownloadArea, Coordinate bundleCoordinate) throws IOException {
        String bundleSourceName = bundleCoordinate.getName() + ".source_" + bundleCoordinate.getVersion() + ".jar";
        File sourceFile = new File(artifactDownloadArea.toURI().resolve(bundleSourceName));
        if (!artifact.getSourceFile().isPresent() && sourceFile.exists()) {
            File artifactSource = new File(targetDirectory.toString() + File.separator + bundleSourceName);
            FileUtils.copyFile(sourceFile, artifactSource);
            artifact.addFact(new ArtifactSourceFile(artifactSource.toPath()));
            LOGGER.info("Attached source artifact for " + artifact + ".");
        }
    }

    private void attachJar(Artifact artifact, File artifactDownloadArea, Coordinate bundleCoordinate) throws IOException {
        String bundleJarName = bundleCoordinate.getName() + "_" + bundleCoordinate.getVersion() + ".jar";
        File jarFile = new File(artifactDownloadArea.toURI().resolve(bundleJarName));
        if (!artifact.getFile().isPresent() && jarFile.exists()) {
            File artifactFile = new File(targetDirectory.toString() + File.separator + bundleJarName);
            FileUtils.copyFile(jarFile, artifactFile);
            artifact.addFact(new ArtifactFile(artifactFile.toPath()));
            LOGGER.info("Attached artifact for " + artifact + ".");
        }
    }

}
