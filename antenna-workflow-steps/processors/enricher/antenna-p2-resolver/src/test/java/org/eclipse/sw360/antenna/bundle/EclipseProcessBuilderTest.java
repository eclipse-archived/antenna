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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests also document how the process will be called:
 * The process called is the antenna-p2-repository-manager, which is an eclipse product and must be called via the command line.
 * To ensure that calls succeed, when changing any of the test also carefully look at the ProjectArgumentExtractor in antenna-p2.
 * It's behaviour is documented in the file ProjectArgumentExtractorTest.java
 */
public class EclipseProcessBuilderTest {

    @Test
    public void testEclipseProcessBuilderCommandWorksCorrectlyWithHttpRepositoryAndOneArtifact() {
        Artifact artifact = new Artifact();
        artifact.addFact(new BundleCoordinates("TestBundle", "1.0.0"));
        File downloadArea = new File("test_file1");
        File installArea = new File("test_file2");

        ProcessBuilder processBuilder = EclipseProcessBuilder.setupEclipseProcess(
                downloadArea, installArea, Collections.singletonList(artifact), Collections.singletonList("http://www.example.org"));

        assertThat(processBuilder.command()).contains("test_file1/eclipse");
        assertThat(processBuilder.command()).contains("-download-area test_file2");
        assertThat(processBuilder.command()).contains("-repositories http://www.example.org");
        assertThat(processBuilder.command()).contains("-coordinates TestBundle,1.0.0");
    }

    @Test
    public void testEclipseProcessBuilderCommandPrepensFileToUriIfItDoesNotHaveAScheme() {
        Artifact artifact = new Artifact();
        artifact.addFact(new BundleCoordinates("TestBundle", "1.0.0"));
        File downloadArea = new File("test_file1");
        File installArea = new File("test_file2");

        ProcessBuilder processBuilder = EclipseProcessBuilder.setupEclipseProcess(
                downloadArea, installArea, Collections.singletonList(artifact), Collections.singletonList("/home/somebody/repository"));

        assertThat(processBuilder.command()).contains("-repositories file:///home/somebody/repository");
    }

    @Test
    public void testEclipseProcessBuilderCommandCorrectlyChainsArtifacts() {
        Artifact artifact1 = new Artifact();
        artifact1.addFact(new BundleCoordinates("TestBundle1", "1.0.0"));
        Artifact artifact2 = new Artifact();
        artifact2.addFact(new BundleCoordinates("TestBundle2", "1.0.0.v201"));
        File downloadArea = new File("test_file1");
        File installArea = new File("test_file2");

        ProcessBuilder processBuilder = EclipseProcessBuilder.setupEclipseProcess(
                downloadArea, installArea, Arrays.asList(artifact1, artifact2), Collections.singletonList("http://www.example.org"));

        assertThat(processBuilder.command()).contains("-coordinates TestBundle1,1.0.0;TestBundle2,1.0.0.v201");
    }
}
