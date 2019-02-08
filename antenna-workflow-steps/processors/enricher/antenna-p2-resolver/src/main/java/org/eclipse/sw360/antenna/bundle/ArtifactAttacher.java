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

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;


public class ArtifactAttacher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAttacher.class);

    private String targetDirectory;

    public ArtifactAttacher(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void copyDependencies(File artifactDownloadArea, Collection<Artifact> artifactsWithBundleCoordinates) throws IOException {
        for (Artifact artifact : artifactsWithBundleCoordinates) {
            attachArtifacts(artifact, artifactDownloadArea);
        }
    }

    private void attachArtifacts(Artifact artifact, File artifactDownloadArea) throws IOException {
        if (artifact.askFor(BundleCoordinates.class).isPresent()) {
            BundleCoordinates bundleCoordinates = artifact.askFor(BundleCoordinates.class).get();
            attachJar(artifact, artifactDownloadArea, bundleCoordinates);
            attachSource(artifact, artifactDownloadArea, bundleCoordinates);
        }
    }

    private void attachSource(Artifact artifact, File artifactDownloadArea, BundleCoordinates bundleCoordinates) throws IOException {
        String bundleSourceName = bundleCoordinates.getSymbolicName() + ".source_" + bundleCoordinates.getVersion() + ".jar";
        File sourceFile = new File(artifactDownloadArea.toURI().resolve(bundleSourceName));
        if (!artifact.getSourceFile().isPresent() && sourceFile.exists()) {
            File artifactSource = new File(targetDirectory + File.separator + bundleSourceName);
            FileUtils.copyFile(sourceFile, artifactSource);
            artifact.addFact(new ArtifactSourceFile(artifactSource.toPath()));
            LOGGER.info("Attached source artifact for " + artifact + ".");
        }
    }

    private void attachJar(Artifact artifact, File artifactDownloadArea, BundleCoordinates bundleCoordinates) throws IOException {
        String bundleJarName = bundleCoordinates.getSymbolicName() + "_" + bundleCoordinates.getVersion() + ".jar";
        File jarFile = new File(artifactDownloadArea.toURI().resolve(bundleJarName));
        if (!artifact.getFile().isPresent() && jarFile.exists()) {
            File artifactFile = new File(targetDirectory + File.separator + bundleJarName);
            FileUtils.copyFile(jarFile, artifactFile);
            artifact.addFact(new ArtifactFile(artifactFile.toPath()));
            LOGGER.info("Attached artifact for " + artifact + ".");
        }
    }

}
