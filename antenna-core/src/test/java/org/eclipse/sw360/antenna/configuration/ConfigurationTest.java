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
package org.eclipse.sw360.antenna.configuration;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.ArtifactSelector;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;
import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.eclipse.sw360.antenna.xml.XMLResolverJaxB;

public class ConfigurationTest {

    private Configuration configuration;

    @Before
    public void resolveConfiguration() throws URISyntaxException, AntennaConfigurationException {
        URL xmlUrl = ConfigurationTest.class.getResource("/antennaconf.xml");
        XMLResolverJaxB resolver = new XMLResolverJaxB(Charset.forName("UTF-8"));
        AntennaConfig config = resolver.resolveXML(new File(xmlUrl.toURI()));
        this.configuration = new Configuration(config);
    }

    private ArtifactSelector mkArtifactSelectorFromFileName(String fileName) {
        ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier();
        artifactIdentifier.setFilename(fileName);
        return new ArtifactSelector(artifactIdentifier);
    }

    @Test
    public void test() {
        final ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier();
        artifactIdentifier.setFilename("slf4j-api-1.6.6.jar");
        assertThat(configuration.getIgnoreForSourceResolving().get(0).matches(artifactIdentifier));
        assertThat(configuration.getIgnoreForSourceResolving().size()).isEqualTo(1);

        License license = new License();
        license.setName("EPL-2.0");

        ArtifactSelector selector = mkArtifactSelectorFromFileName("overrideName.jar");
        List<License> list = configuration.getFinalLicenses().get(selector).getLicenses();
        assertThat(list.contains(license)).isTrue();
        assertThat(configuration.getFinalLicenses().containsKey(selector)).isTrue();
        assertThat(configuration.isFailOnMissingSources()).isTrue();
        assertThat(configuration.isFailOnIncompleteSources()).isFalse();
        assertThat(configuration.getValidForIncompleteSources().size()).isEqualTo(2);
    }

    @Test
    public void testConfigIsNull() {
        Configuration configuration = new Configuration(null);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getFinalLicenses()).isEmpty();
        assertThat(configuration.getRemoveArtifact()).isEmpty();
        assertThat(configuration.getValidForIncompleteSources()).isEmpty();
        assertThat(configuration.getValidForMissingSources()).isEmpty();
    }

    @Test
    public void overridesTest() {
        assertThat(configuration.getOverride().size()).isEqualTo(2);
        ArtifactSelector artifactSelector = mkArtifactSelectorFromFileName("overrideAll.jar");
        Artifact generatedArtifact = configuration.getOverride().get(artifactSelector);
        assertThat(generatedArtifact.getDeclaredLicenses().getLicenses().get(0).getName()).isEqualTo("testLicense");
        ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier();
        artifactIdentifier.setFilename("overrideName.jar");
        MavenCoordinates mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setArtifactId("testID");
        mavenCoordinates.setGroupId("testGroupId");
        mavenCoordinates.setVersion("testVersion");
        artifactIdentifier.setMavenCoordinates(mavenCoordinates);
        BundleCoordinates bundleCoordinates = new BundleCoordinates();
        bundleCoordinates.setSymbolicName("testName");
        bundleCoordinates.setBundleVersion("testVersion");
        artifactIdentifier.setBundleCoordinates(bundleCoordinates);
        assertThat(generatedArtifact.getArtifactIdentifier()).isEqualTo(artifactIdentifier);
    }

    @Test
    public void addArtifactTest() {
        Artifact compareArtifact = new Artifact();
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
        compareArtifact.setArtifactIdentifier(identifier);
        Artifact artifact = configuration.getAddArtifact().get(0);
        assertThat(artifact.getDeclaredLicenses().getLicenses().get(0).getName()).isEqualTo("Apache");
        assertThat(artifact.getArtifactIdentifier()).isEqualTo(compareArtifact.getArtifactIdentifier());
        assertThat(artifact.isProprietary()).isFalse();
        assertThat(artifact.getMatchState()).isEqualTo(MatchState.EXACT);
    }
}
