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

package org.eclipse.sw360.antenna.p2;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class P2Repository {
    private MetadataRepository metadata;
    private ArtifactDownloader downloader;

    public P2Repository(IMetadataRepository metadataRepository, IArtifactRepository artifactRepository, File targetDirectory) {
        this(new MetadataRepository(metadataRepository), new ArtifactDownloader(artifactRepository, targetDirectory));
    }

    public P2Repository(MetadataRepository metadataRepository, ArtifactDownloader artifactDownloader) {
        this.metadata = metadataRepository;
        this.downloader = artifactDownloader;
    }

    public void resolveArtifact(P2Artifact p2Artifact) throws P2Exception {
        if (isBundleArtifactWithMissingJarOrSources(p2Artifact)) {
            if (!p2Artifact.getJarPath().isPresent()) {
                resolveJarFile(p2Artifact);
            }
            if (!p2Artifact.getSourcePath().isPresent()) {
                resolveSourceFile(p2Artifact);
            }
        }
    }

    private static boolean isBundleArtifactWithMissingJarOrSources(P2Artifact p2Artifact) {
        return !(p2Artifact.getJarPath().isPresent() && p2Artifact.getSourcePath().isPresent());
    }

    private void resolveJarFile(P2Artifact p2Artifact) throws P2Exception {
        String symbolicName = p2Artifact.getBundleSymbolicName();
        Version version = p2Artifact.getVersion();

        resolveFile(symbolicName, version, p2Artifact::setJarPath);
    }

    private void resolveSourceFile(P2Artifact p2Artifact) throws P2Exception {
        // Source jars have the same symbolic name and only a ".source" attached
        String symbolicName = p2Artifact.getBundleSymbolicName() + ".source";
        Version version = p2Artifact.getVersion();

        resolveFile(symbolicName, version, p2Artifact::setSourcePath);
    }

    private void resolveFile(String symbolicName, Version version, Consumer<Path> fileAdder) throws P2Exception {
        Set<IInstallableUnit> installableUnits = metadata.queryRepository(symbolicName);
        for (IInstallableUnit installableUnit : installableUnits) {
            if (symbolicName.equals(installableUnit.getId()) && version.equals(installableUnit.getVersion())) {
                attachFile(fileAdder, installableUnit);
                System.out.println("Found artifact for " + symbolicName);
                return;
            }
        }
    }

    private void attachFile(Consumer<Path> fileAdder, IInstallableUnit installableUnit) throws P2Exception {
        try {
            List<File> files = downloader.downloadInstallableUnits(installableUnit);
            if (files.size() > 1) {
                throw new P2Exception("Downloaded several artifacts for " + installableUnit.getId() + ".");
            }
            fileAdder.accept(files.get(0).toPath());
        } catch (IOException e) {
            // This can be okay. Maybe add proper logging later.
        }
    }
}
