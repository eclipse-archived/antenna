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

package org.eclipse.sw360.antenna.p2resolver.workflow.processors.enricher;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.p2resolver.OperatingSystemSpecifics;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.checkInternetConnectionAndAssume;
import static org.mockito.Mockito.*;


public class P2ResolverTest extends AntennaTestWithMockedContext {
    private static final String DEPENDENCY_REPOSITORY = "repositories";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private P2Resolver resolver;
    private File dependencyDir;

    @Before
    public void setup() throws IOException {
        File installationDir = temporaryFolder.newFolder();
        dependencyDir = temporaryFolder.newFolder();
        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(installationDir.toPath());
        when(toolConfigMock.getDependenciesDirectory()).thenReturn(dependencyDir.toPath());
        when(toolConfigMock.getEncoding()).thenReturn(StandardCharsets.UTF_8);

        resolver = new P2Resolver();
        resolver.setAntennaContext(antennaContextMock);

        Map<String, String> configMap = new HashMap<>();
        configMap.put(DEPENDENCY_REPOSITORY, "https://download.eclipse.org/releases/neon/201705151400");
        resolver.configure(configMap);
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
        verify(toolConfigMock, atLeast(0)).getAntennaTargetDirectory();
        verify(toolConfigMock, atLeast(0)).getDependenciesDirectory();
        verify(toolConfigMock, atLeast(0)).getEncoding();
    }

    @Test
    public void testProductInstallation() throws AntennaException {
        checkInternetConnectionAndAssume(Assume::assumeTrue);
        Assume.assumeFalse("The linux looks unusual and probably does not support the p2 product.", OperatingSystemSpecifics.isLinux() && ! Files.exists(Paths.get("/usr/lib/")));

        Artifact artifact = new Artifact();
        artifact.addFact(new BundleCoordinates("org.junit", "4.12.0.v201504281640"));


        List<Artifact> processedArtifacts = new ArrayList<>(resolver.process(Collections.singletonList(artifact)));

        assertThat(new File(Paths.get(dependencyDir.toString(), "org.junit_4.12.0.v201504281640.jar").toUri())).exists();
        assertThat(new File(Paths.get(dependencyDir.toString(), "org.junit.source_4.12.0.v201504281640.jar").toUri())).exists();
        assertThat(processedArtifacts.get(0).getFile()).isNotEmpty();
        assertThat(processedArtifacts.get(0).getSourceFile()).isNotEmpty();
    }
}
