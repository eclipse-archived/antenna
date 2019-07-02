/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.gradle;

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.frontend.stub.gradle.AntennaImpl;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.AbstractTestProject;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.ExampleTestProject;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.checkInternetConnectionAndAssume;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

public class AntennaGradlePluginTest {

    private AbstractTestProject exampleTestProject;
    private Path projectRoot;

    @Before
    public void setUpTestProject() throws IOException {
        exampleTestProject = new ExampleTestProject();

        projectRoot = exampleTestProject.getProjectRoot();

        System.setProperty("user.dir", projectRoot.toAbsolutePath().toString());

        String buildGradle = "plugins {\nid 'org.eclipse.sw360.antenna'\n}\n" +
                "AntennaConfiguration{\npomPath '" + exampleTestProject.getProjectPom() + "'\n}";
        FileUtils.writeStringToFile(projectRoot.resolve("build.gradle").toFile(), buildGradle);
    }

    @After
    public void cleanup() throws Exception {
        exampleTestProject.cleanUpTemporaryProjectFolder();
    }

    @Ignore("steps are not on classpath")
    @Test
    public void testWithoutGradle() throws Exception {
        checkInternetConnectionAndAssume(Assume::assumeTrue);
        AntennaImpl runner = new AntennaImpl("antenna-maven-plugin", exampleTestProject.getProjectPom());
        runner.execute();
        assertThat(projectRoot.resolve("antenna").toFile()).exists(); // TODO
    }

    @Test
    public void testWithGradle() throws Exception {
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
    public void testWithoutGradleSetSystemEnvironmentVariables() throws AntennaException {
        URL pom = AntennaGradlePluginTest.class.getClassLoader().getResource("pom.xml");
        URL propertiesFile = AntennaGradlePluginTest.class.getClassLoader().getResource("antennaTestVariable.properties");

        AntennaImpl runner = new AntennaImpl("antenna-maven-plugin",
                Paths.get(pom.getPath()),
                Paths.get(propertiesFile.getPath()));

        runner.execute();

        Path root = Paths.get(AntennaGradlePluginTest.class.getClassLoader().getResource("build").getPath());

        assertThat(root.resolve("antenna")).exists();
    }
}
