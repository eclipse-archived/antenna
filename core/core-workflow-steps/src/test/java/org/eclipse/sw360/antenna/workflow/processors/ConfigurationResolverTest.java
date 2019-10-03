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

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.workflow.processors.filter.ConfigurationHandlerOverride;
import org.eclipse.sw360.antenna.xml.XMLResolverJaxB;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
                .orElseThrow(() -> new IllegalStateException("failed to get " + path));
    }

    @Before
    public void init() {
        File xmlFile = getResourceAsFile("antennaconf.xml");
        XMLResolverJaxB resolverXML = new XMLResolverJaxB(StandardCharsets.UTF_8);
        AntennaConfig antennaConfig = resolverXML.resolveXML(xmlFile);
        config = new Configuration(antennaConfig);
        when(antennaContextMock.getConfiguration())
                .thenReturn(config);
    }

    @After
    public void after() {
        verify(reporterMock, atLeast(0)).add(any(), any());
        verify(reporterMock, atLeast(0)).add(any(Artifact.class), any(), any());
    }

    @Test
    public void resolveDownloadConfigurationTest() {
        List<Artifact> artifacts = new ArrayList<>();
        Artifact artifact = new Artifact();
        artifact.addFact(new ArtifactFilename("director-ant.jar"));
        artifacts.add(artifact);
        Artifact artifact1 = new Artifact();
        artifact1.addFact(new ArtifactFilename("slf4j-api-1.6.6.jar"));
        artifacts.add(artifact1);

        ConfigurationHandlerOverride resolver = new ConfigurationHandlerOverride(antennaContextMock);
        resolver.process(artifacts);

        assertThat(artifact.askFor(ArtifactFilename.class)
                .flatMap(ArtifactFilename::getBestFilenameEntryGuess)
                .map(ArtifactFilename.ArtifactFilenameEntry::getFilename)
                .orElse(""))
                .isEqualTo("director-ant.jar");
        assertThat(artifact1.getFlag(Artifact.IS_IGNORE_FOR_DOWNLOAD_KEY)).isTrue();
    }

    @Test
    public void overrideValuesTest() {
        ConfigurationHandlerOverride resolver = new ConfigurationHandlerOverride(antennaContextMock);
        List<Artifact> artifacts = new ArrayList<>();
        Artifact artifact = new Artifact();
        artifact.addFact(new ArtifactFilename("overrideAll.jar"));
        artifacts.add(artifact);
        artifacts.add(artifact);

        resolver.process(artifacts);

        final MavenCoordinates mavenCoordinates = artifact.askFor(MavenCoordinates.class).get();
        assertThat(mavenCoordinates.getArtifactId()).isEqualTo("testID");
        assertThat(mavenCoordinates.getGroupId()).isEqualTo("testGroupId");
        assertThat(mavenCoordinates.getVersion()).isEqualTo("testVersion");

        final BundleCoordinates bundleCoordinates = artifact.askFor(BundleCoordinates.class).get();
        assertThat(bundleCoordinates.getSymbolicName()).isEqualTo("testName");
        assertThat(bundleCoordinates.getBundleVersion()).isEqualTo("testVersion");
    }

    @Test
    public void forbiddenLicensesText() {
        assertThat(config.getFinalLicenses().size()).isEqualTo(1);
    }
}
