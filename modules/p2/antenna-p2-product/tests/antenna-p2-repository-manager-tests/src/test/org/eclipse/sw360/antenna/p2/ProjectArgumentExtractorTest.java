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

import org.eclipse.equinox.p2.metadata.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectArgumentExtractorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testArgumentsExtractsRepositoryCorrectly() throws URISyntaxException, P2Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add("-repositories " + new URI("/some/path/to/repo"));

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        assertThat(projectArguments.getRepositories()).containsExactlyInAnyOrder(new URI("/some/path/to/repo"));
    }

    @Test
    public void testArgumentsExtractsSemicolonSeparatedRepositoriesCorrectly() throws URISyntaxException, MalformedURLException, P2Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add("-repositories " + new URI("/some/path/to/repo") + ";" + new URL("https://download.eclipse.org/releases/neon").toURI());

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        assertThat(projectArguments.getRepositories()).containsExactlyInAnyOrder(new URI("/some/path/to/repo"), new URL("https://download.eclipse.org/releases/neon").toURI());
    }

    @Test
    public void testArgumentsExtractsArtifactToVersionedP2Artifact() throws URISyntaxException, MalformedURLException, P2Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add("-coordinates " + "Test_Bundle,0.0.1");

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        assertThat(projectArguments.getP2Artifacts()).containsExactlyInAnyOrder(new P2Artifact("Test_Bundle", Version.createOSGi(0, 0, 1)));
    }

    @Test
    public void testArgumentsExtractsSemicolonSeparatedListOfVersions() throws URISyntaxException, MalformedURLException, P2Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add("-coordinates " + "Test_Bundle,0.0.1;Test_Bundle2,2.0.0.2015");

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        assertThat(projectArguments.getP2Artifacts()).containsExactlyInAnyOrder(new P2Artifact("Test_Bundle", Version.createOSGi(0, 0, 1)), new P2Artifact("Test_Bundle2", Version.createOSGi(2, 0, 0, "2015")));
    }

    @Test
    public void testArgumentsExtractsDownloadAreaCorrectly() throws P2Exception, IOException {
        List<String> arguments = new ArrayList<>();
        File newFolder = temporaryFolder.newFolder();
        arguments.add("-download-area " + newFolder);

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        assertThat(projectArguments.getDownloadArea()).isEqualTo(newFolder);
    }

    @Test(expected = P2Exception.class)
    public void testArgumentsThrowsIfDownloadAreaDoesNotExist() throws P2Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add("-download-area " + temporaryFolder.toString() + File.separator + "test_folder");

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        // Should already throw
    }

    @Test
    public void testArgumentExtractsAllArgumentsIfInProgression() throws P2Exception, URISyntaxException, IOException {
        List<String> arguments = new ArrayList<>();
        File newFolder = temporaryFolder.newFolder();
        arguments.add("-coordinates " + "Test_Bundle,0.0.1;Test_Bundle2,2.0.0.2015");
        arguments.add("-download-area " + newFolder.toPath().toString());
        arguments.add("-repositories " + new URI("/some/path/to/repo"));

        ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(arguments);
        assertThat(projectArguments.getRepositories()).containsExactlyInAnyOrder(new URI("/some/path/to/repo"));
        assertThat(projectArguments.getP2Artifacts()).containsExactlyInAnyOrder(new P2Artifact("Test_Bundle", Version.createOSGi(0, 0, 1)), new P2Artifact("Test_Bundle2", Version.createOSGi(2, 0, 0, "2015")));
        assertThat(projectArguments.getDownloadArea()).isEqualTo(newFolder);
    }
}
