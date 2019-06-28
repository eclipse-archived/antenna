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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ArtifactDownloader {

    private final IArtifactRepository artifactRepository;
    private final File targetDirectory;

    ArtifactDownloader(IArtifactRepository artifactRepository, File targetDirectory) {
        this.artifactRepository = artifactRepository;
        this.targetDirectory = targetDirectory;
    }

    public List<File> downloadInstallableUnits(IInstallableUnit installableUnits) throws IOException {
        Collection<IArtifactKey> artifactKeys = installableUnits.getArtifacts();
        List<File> installedFiles = new ArrayList<>();
        for (IArtifactKey artifactKey : artifactKeys) {
            downloadArtifact(artifactKey).ifPresent(installedFiles::add);
        }
        return installedFiles;
    }

    private Optional<File> downloadArtifact(IArtifactKey artifactKey) throws IOException {
        File destination = createArtifactName(artifactKey);
        IArtifactDescriptor[] artifactDescriptors = artifactRepository.getArtifactDescriptors(artifactKey);
        if (artifactDescriptors.length > 0) {
            try (FileOutputStream installableUnitJar = new FileOutputStream(destination)) {
                // There should only ever be one ArtifactDescriptor per P2Artifact
                artifactRepository.getArtifact(
                        artifactDescriptors[0], installableUnitJar, new NullProgressMonitor());
                return Optional.of(destination);
            }
        }
        return Optional.empty();
    }

    private File createArtifactName(IArtifactKey artifactKey) {
        return Paths.get(
                this.targetDirectory.toPath().toString(),
                artifactKey.getId() + "_" + artifactKey.getVersion() + ".jar")
                .toFile();
    }
}
