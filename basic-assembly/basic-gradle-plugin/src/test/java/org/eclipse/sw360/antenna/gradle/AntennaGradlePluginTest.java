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
package org.eclipse.sw360.antenna.gradle;

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.frontend.gradle.AntennaImpl;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProject;
import org.eclipse.sw360.antenna.frontend.testProjects.ExampleTestProject;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

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

        String buildGradle = "plugins {\nid 'org.eclipse.sw360.antenna'\n}\n"+
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
        AntennaImpl runner = new AntennaImpl("basic-maven-plugin", exampleTestProject.getProjectPom());
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
                .withArguments(BasicGradlePlugin.TASK_NAME, "--stacktrace")
                .withDebug(withDebug)
                .forwardOutput()
                .build();


        assertThat(result.task(":" + BasicGradlePlugin.TASK_NAME).getOutcome()).isEqualTo(SUCCESS);

        assertThat(projectRoot.resolve("build/antenna").toFile()).exists();
        assertThat(projectRoot.resolve("build/antenna/3rdparty-licenses.html").toFile()).exists();
        assertThat(projectRoot.resolve("build/antenna/sources.zip").toFile()).exists();
    }
}
