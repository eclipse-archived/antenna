/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.gradle;

import org.eclipse.sw360.antenna.frontend.stub.gradle.AntennaImpl;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.AbstractTestProject;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.ExampleTestProject;
import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.checkInternetConnectionAndAssume;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AntennaGradlePluginTest {

    @Mock
    private Project project = mock(Project.class);

    private AbstractTestProject exampleTestProject;
    private Path projectRoot;

    @Before
    public void setUpTestProject() throws IOException {
        exampleTestProject = new ExampleTestProject();

        projectRoot = exampleTestProject.getProjectRoot();

        System.setProperty("user.dir", projectRoot.toAbsolutePath().toString());

        String buildGradle = "plugins {\nid 'org.eclipse.sw360.antenna'\n}\n" +
        "AntennaConfiguration{\ntoolConfigurationPath '" + exampleTestProject.getProjectPom() + "'\n}";
        Files.write(projectRoot.resolve("build.gradle"), buildGradle.getBytes(StandardCharsets.UTF_8));

        when(project.getBuildDir())
                .thenReturn(projectRoot.resolve("build").toFile());
        when(project.getRootDir())
                .thenReturn(projectRoot.toFile());
        when(project.getName())
                .thenReturn(exampleTestProject.getExpectedProjectArtifactId());
    }

    @After
    public void cleanup() throws IOException {
        exampleTestProject.cleanUpTemporaryProjectFolder();
    }

    @Ignore("steps are not on classpath")
    @Test
    public void testWithoutGradle() {
        checkInternetConnectionAndAssume(Assume::assumeTrue);
        AntennaImpl runner = new AntennaImpl("antenna-maven-plugin", exampleTestProject.getProjectPom(), project);
        runner.execute();
        assertThat(projectRoot.resolve("antenna").toFile()).exists(); // TODO
    }

    @Test
    public void testWithGradle() {
        checkInternetConnectionAndAssume(Assume::assumeTrue);
        boolean withDebug = false; // whether to enable debugging
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRoot.toFile())
                .withPluginClasspath()
                .withArguments(GradlePlugin.TASK_NAME, "--stacktrace")
                .withDebug(withDebug)
                .forwardOutput()
                .build();


        assertThat(result.task(":" + GradlePlugin.TASK_NAME).getOutcome()).isEqualTo(SUCCESS);

        assertThat(projectRoot.resolve("build/antenna").toFile()).exists();
        assertThat(projectRoot.resolve("build/antenna/3rdparty-licenses.html").toFile()).exists();
        assertThat(projectRoot.resolve("build/antenna/sources.zip").toFile()).exists();
    }

    @Test
    public void testWithoutGradleSetSystemEnvironmentVariables() throws URISyntaxException {
        URI pom = AntennaGradlePluginTest.class.getClassLoader().getResource("pom.xml").toURI();
        URI propertiesFile = AntennaGradlePluginTest.class.getClassLoader().getResource("antennaTestVariable.properties").toURI();

        AntennaImpl runner = new AntennaImpl("antenna-maven-plugin",
                Paths.get(pom),
                project,
                Paths.get(propertiesFile));

        runner.execute();

        Path root = projectRoot.resolve("build");

        assertThat(root.resolve("antenna")).exists();
    }

    @Test
    public void testWithPomInSubDirs() throws IOException {
        Path pomParentPath = exampleTestProject.getProjectPom().getParent().normalize();
        Path dest = Paths.get(pomParentPath.toString(), "src", "pom.xml");
        Files.move(exampleTestProject.getProjectPom(), dest, REPLACE_EXISTING);

        String buildGradle = "plugins {\nid 'org.eclipse.sw360.antenna'\n}\n" +
                "AntennaConfiguration{\ntoolConfigurationPath '" + dest + "'\n}";
        Files.write(projectRoot.resolve("build.gradle"), buildGradle.getBytes(StandardCharsets.UTF_8));

        AntennaImpl runner = new AntennaImpl("antenna-maven-plugin",
                dest,
                project);

        runner.execute();

        assertThat(dest.getParent().resolve("build").toFile()).doesNotExist();

        assertThat(projectRoot.resolve("build/antenna").toFile()).exists();
        assertThat(projectRoot.resolve("build/antenna/3rdparty-licenses.html").toFile()).exists();
        assertThat(projectRoot.resolve("build/antenna/sources.zip").toFile()).exists();
    }
}
