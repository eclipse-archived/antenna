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

import org.eclipse.sw360.antenna.frontend.gradle.AntennaImpl;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProject;
import org.eclipse.sw360.antenna.frontend.testProjects.ExampleTestProject;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertTrue;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.assumeToBeConnectedToTheInternet;

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
        assumeToBeConnectedToTheInternet();
        AntennaImpl runner = new AntennaImpl(exampleTestProject.getProjectPom());
        runner.execute();
        assertTrue(projectRoot.resolve("antenna").toFile().exists()); // TODO
    }

    @Test
    public void testWithGradle() throws Exception {
        assumeToBeConnectedToTheInternet();
        boolean withDebug = false; // whether to enable debugging
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRoot.toFile())
                .withPluginClasspath()
                .withArguments(AntennaBasicGradlePlugin.TASK_NAME, "--stacktrace")
                .withDebug(withDebug)
                .forwardOutput()
                .build();


        assertThat(result.task(":" + AntennaBasicGradlePlugin.TASK_NAME).getOutcome(),
                equalTo(SUCCESS));

        assertTrue(projectRoot.resolve("build/antenna").toFile().exists());
        assertTrue(projectRoot.resolve("build/antenna/AntennaDisclosureDocument.txt").toFile().exists());
        assertTrue(projectRoot.resolve("build/antenna/sources.zip").toFile().exists());
    }
}
