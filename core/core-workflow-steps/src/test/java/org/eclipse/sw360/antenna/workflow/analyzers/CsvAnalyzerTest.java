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

package org.eclipse.sw360.antenna.workflow.analyzers;

import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.util.ClassCodeSourceLocation;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

public class CsvAnalyzerTest extends AntennaTestWithMockedContext {

    private IProject project;
    private CsvAnalyzer analyzer;
    private License license;

    @Before
    public void setUp() {
        project = Mockito.mock(IProject.class);
        when(antennaContextMock.getProject()).thenReturn(project);
        when(project.getBasedir()).thenReturn(new File(""));
        when(toolConfigMock.getEncoding()).thenReturn(StandardCharsets.UTF_8);
        analyzer = new CsvAnalyzer();
        analyzer.setAntennaContext(antennaContextMock);

        license = new License();
        license.setName("Apache-2.0");
        license.setLongName("Apache Software License 2.0");
    }

    @Test
    public void testCsvAnalyzer() throws AntennaException, URISyntaxException {
        configureAnalyzer("dependencies.csv");

        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        assertThat(artifacts).hasSize(1);
        Artifact foundArtifact = artifacts.iterator().next();
        assertThat(foundArtifact.getMatchState()).isEqualTo(MatchState.EXACT);
        assertThat(foundArtifact.askFor(MavenCoordinates.class).get())
                .isEqualTo(new MavenCoordinates("commons-csv", "org.apache.commons", "1.4"));

        assertThat(foundArtifact.askFor(DeclaredLicenseInformation.class).get())
                .isEqualTo(new DeclaredLicenseInformation(license));

        assertThat(foundArtifact.askFor(ObservedLicenseInformation.class).get())
                .isEqualTo(new ObservedLicenseInformation(license));

        assertThat(foundArtifact.askFor(ArtifactPathnames.class).get().get().get(0)).endsWith("commons-csv.jar");
        assertThat(foundArtifact.askFor(ArtifactSourceUrl.class).get().get()).contains("http://archive.apache.org/dist/commons/csv/source/commons-csv-1.4-src.zip");
    }

    @Test
    public void testThatOldFormatIsSupported() throws AntennaException, URISyntaxException {
        configureAnalyzer("dependenciesOldFormat.csv");

        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        assertThat(artifacts).hasSize(1);
        Artifact foundArtifact = artifacts.iterator().next();
        assertThat(foundArtifact.askFor(MavenCoordinates.class).get())
                .isEqualTo(new MavenCoordinates("commons-csv", "org.apache.commons", "1.4"));

        assertThat(foundArtifact.askFor(DeclaredLicenseInformation.class).get())
                .isEqualTo(new DeclaredLicenseInformation(license));

        assertThat(foundArtifact.askFor(ArtifactPathnames.class).get().get().get(0)).endsWith("commons-csv.jar");
    }

    @Override
    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(antennaContextMock, atLeast(0)).getToolConfiguration();
        verify(antennaContextMock, atLeast(0)).getProject();
        verify(antennaContextMock, atLeast(0)).getProcessingReporter();
        verify(toolConfigMock, atLeast(0)).getEncoding();

        // assert that there were no unexpected interactions with the mocked objects
        verifyNoMoreInteractions(antennaContextMock);
        verifyNoMoreInteractions(toolConfigMock);
    }

    private void configureAnalyzer(String fileName) throws URISyntaxException, AntennaConfigurationException {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("file.path", Paths.get("src", "test", "resources", "CsvAnalyzerTest", fileName).toString());
        configMap.put("base.dir", ClassCodeSourceLocation.getClassCodeSourceLocationAsString(this.getClass()));
        analyzer.configure(configMap);
    }
}
