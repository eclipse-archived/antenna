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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactAttacherTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    File downloadFolder;
    File targetFolder;

    @Before
    public void setUp() throws IOException {
        downloadFolder = folder.newFolder();
        targetFolder = folder.newFolder();
    }

    @Test
    public void copyDependenciesAttachesArtifactsCorrectly() throws IOException {
        String jarName = "TestBundle_0.0.1.jar";
        String sourceName = "TestBundle.source_0.0.1.jar";
        Artifact artifact = createArtifactWithBundleCoordinates(jarName);
        createJarFileInDownloadFolder(jarName);
        createJarFileInDownloadFolder(sourceName);

        ArtifactAttacher attacher = new ArtifactAttacher(targetFolder.toPath());

        attacher.copyDependencies(downloadFolder, Collections.singletonList(artifact));

        assertThat(artifact.getFile()).isPresent();
        assertThat(copiedFile(jarName)).exists();
        assertThat(artifact.getSourceFile()).isPresent();
        assertThat(copiedFile(sourceName)).exists();
    }

    @Test
    public void copyDependenciesDoesNotAttachFilesIfMissing() throws IOException {
        String jarName = "TestBundle_0.0.1.jar";
        String sourceName = "TestBundle.source_0.0.1.jar";
        Artifact artifact = createArtifactWithBundleCoordinates(jarName);
        createJarFileInDownloadFolder(sourceName);

        ArtifactAttacher attacher = new ArtifactAttacher(targetFolder.toPath());

        attacher.copyDependencies(downloadFolder, Collections.singletonList(artifact));

        assertThat(artifact.getFile()).isEmpty();
        assertThat(copiedFile(jarName)).doesNotExist();
        assertThat(artifact.getSourceFile()).isPresent();
        assertThat(copiedFile(sourceName)).exists();
    }

    @Test
    public void copyDependenciesAttachesFilesOnlyIfArtifactDoesNotAlreadyHaveThem() throws IOException {
        String jarName = "TestBundle_0.0.1.jar";
        String sourceName = "TestBundle.source_0.0.1.jar";
        Artifact artifact = createArtifactWithBundleCoordinates(jarName);
        artifact.addFact(new ArtifactFile(Paths.get("empty/path")));
        createJarFileInDownloadFolder(jarName);
        createJarFileInDownloadFolder(sourceName);

        ArtifactAttacher attacher = new ArtifactAttacher(targetFolder.toPath());

        attacher.copyDependencies(downloadFolder, Collections.singletonList(artifact));

        assertThat(artifact.getFile()).isPresent();
        assertThat(copiedFile(jarName)).doesNotExist();
        assertThat(artifact.getSourceFile()).isPresent();
        assertThat(copiedFile(sourceName)).exists();
    }

    private File copiedFile(String jarName) {
        return new File(targetFolder.toPath().resolve(jarName).toUri());
    }

    private void createJarFileInDownloadFolder(String jarName) throws IOException {
        new File(downloadFolder.toPath().resolve(jarName).toUri()).createNewFile();
    }

    private Artifact createArtifactWithBundleCoordinates(String jarName) {
        Artifact artifact = new Artifact();
        artifact.addCoordinate(new Coordinate(Coordinate.Types.P2, jarName.split("_")[0], jarName.split("_")[1].replace(".jar", "")));
        return artifact;
    }

}
