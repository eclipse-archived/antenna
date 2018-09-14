/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;
import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.workflow.processors.filter.ConfigurationHandlerAdd;
import org.eclipse.sw360.antenna.workflow.processors.filter.ConfigurationHandlerOverride;
import org.eclipse.sw360.antenna.xml.XMLResolverJaxB;
import org.junit.rules.TemporaryFolder;

public class ConfigurationResolverTest extends AntennaTestWithMockedContext {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    Configuration config;

    private File getResourceAsFile(String path) {
        return Optional.ofNullable(getClass().getClassLoader())
                .map(c -> c.getResourceAsStream(path))
                .map(stream -> {
                    try {
                        File out = tmpFolder.newFile(path);
                        FileUtils.copyInputStreamToFile(stream, out);
                        return out;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElseThrow(() -> new RuntimeException("failed to get " + path));
    }

    @Before
    public void init() throws AntennaConfigurationException {
        File xmlFile = getResourceAsFile("antennaconf.xml");
        XMLResolverJaxB resolverXML = new XMLResolverJaxB(Charset.forName("UTF-8"));
        AntennaConfig antennaConfig = resolverXML.resolveXML(xmlFile);
        config = new Configuration(antennaConfig);
        when(antennaContextMock.getConfiguration())
                .thenReturn(config);
    }

    @After
    public void after() {
        verify(reporterMock, atLeast(0)).addProcessingMessage(any(), any(), any());
    }

    @Test
    public void resolveDownloadConfigurationTest() {
        List<Artifact> artifacts = new ArrayList<>();
        Artifact artifact = new Artifact();
        artifact.getArtifactIdentifier().setFilename("director-ant.jar");
        artifacts.add(artifact);
        Artifact artifact1 = new Artifact();
        artifact1.getArtifactIdentifier().setFilename("slf4j-api-1.6.6.jar");
        artifacts.add(artifact1);

        ConfigurationHandlerOverride resolver = new ConfigurationHandlerOverride(antennaContextMock);
        resolver.process(artifacts);

        assertThat(artifact.getArtifactIdentifier().getFilename()).isEqualTo("director-ant.jar");
        assertThat(artifact1.isIgnoreForDownload()).isTrue();
    }

    @Test
    public void overrideValuesTest() {
        ConfigurationHandlerOverride resolver = new ConfigurationHandlerOverride(antennaContextMock);
        List<Artifact> artifacts = new ArrayList<>();
        Artifact artifact = new Artifact();
        artifact.getArtifactIdentifier().setFilename("overrideAll.jar");
        artifacts.add(artifact);
        artifacts.add(artifact);

        resolver.process(artifacts);

        assertThat(artifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId()).isEqualTo("testID");
        assertThat(artifact.getArtifactIdentifier().getMavenCoordinates().getGroupId()).isEqualTo("testGroupId");
        assertThat(artifact.getArtifactIdentifier().getMavenCoordinates().getVersion()).isEqualTo("testVersion");
        assertThat(artifact.getArtifactIdentifier().getBundleCoordinates().getSymbolicName()).isEqualTo("testName");
        assertThat(artifact.getArtifactIdentifier().getBundleCoordinates().getBundleVersion()).isEqualTo("testVersion");
    }

    @Test
    public void forbiddenLicensesText() {
        assertThat(config.getFinalLicenses().size()).isEqualTo(1);
    }

    @Test
    public void addArtifactsTest() {
        ConfigurationHandlerAdd resolver = new ConfigurationHandlerAdd(antennaContextMock);

        List<Artifact> artifacts = new ArrayList<>();
        resolver.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(2);
        assertThat(artifacts.get(0).isProprietary()).isFalse();
        assertThat(artifacts.get(0).getMatchState()).isEqualTo(MatchState.EXACT);
        assertThat(artifacts.get(0).getPathnames()).isNotNull();


        ArtifactIdentifier identifier = new ArtifactIdentifier();
        identifier.setFilename("addArtifact.jar");
        MavenCoordinates mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setArtifactId("addArtifactId");
        mavenCoordinates.setGroupId("addGroupId");
        mavenCoordinates.setVersion("addVersion");
        identifier.setMavenCoordinates(mavenCoordinates);
        BundleCoordinates bundleCoordinates = new BundleCoordinates();
        bundleCoordinates.setSymbolicName("addSymbolicName");
        bundleCoordinates.setBundleVersion("addBundleVersion");
        identifier.setBundleCoordinates(bundleCoordinates);
        assertThat(artifacts.get(0).getArtifactIdentifier()).isEqualTo(identifier);
    }
}
