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

package org.eclipse.sw360.antenna.workflow.processors.enricher;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


public class P2ResolverTest {
    private static final String DEPENDENCY_REPOSITORY = "repositories";
    private static final String TARGET_DIRECTORY = "filepath";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testProductInstallation() throws AntennaException, IOException {
        Artifact artifact = new Artifact();
        artifact.addFact(new BundleCoordinates("org.junit", "4.12.0.v201504281640"));

        Map<String, String> configMap = new HashMap<>();
        configMap.put(DEPENDENCY_REPOSITORY, "https://download.eclipse.org/releases/neon/201705151400");
        File targetDir = temporaryFolder.newFolder();
        configMap.put(TARGET_DIRECTORY, targetDir.toString());

        P2Resolver resolver = new P2Resolver();
        resolver.configure(configMap);

        List<Artifact> processedArtifacts = new ArrayList<>(resolver.process(Collections.singletonList(artifact)));

        assertThat(new File(Paths.get(targetDir.toString(), "org.junit_4.12.0.v201504281640.jar").toUri())).exists();
        assertThat(new File(Paths.get(targetDir.toString(), "org.junit.source_4.12.0.v201504281640.jar").toUri())).exists();
        assertThat(processedArtifacts.get(0).getFile()).isNotEmpty();
        assertThat(processedArtifacts.get(0).getSourceFile()).isNotEmpty();
    }
}
