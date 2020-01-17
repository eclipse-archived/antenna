/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class WrappedMavenProjectTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testWrappedMavenProject() throws IOException {
        String artifactId = "test";
        String version = "x.x.x";
        File pomFile = folder.newFile("pom.xml");
        String outputDir = folder.newFolder("outputdir").getName();

        Build build = new Build();
        build.setOutputDirectory(outputDir);

        MavenProject mavenProject = new MavenProject();
        mavenProject.setArtifactId(artifactId);
        mavenProject.setVersion(version);
        mavenProject.setFile(pomFile);
        mavenProject.setBuild(build);

        WrappedMavenProject wrappedMavenProject = new WrappedMavenProject(mavenProject);

        assertThat(wrappedMavenProject.getBuildDirectory()).isEqualTo(outputDir);
        assertThat(wrappedMavenProject.getConfigFile()).isEqualTo(pomFile);
        assertThat(wrappedMavenProject.getRawProject()).isEqualTo(mavenProject);
        assertThat(wrappedMavenProject.getProjectId()).isEqualTo(artifactId);
        assertThat(wrappedMavenProject.getVersion()).isEqualTo(version);
    }
}
