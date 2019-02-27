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

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCore;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.xml.XMLResolverJaxB;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
        return new ArtifactFilename(fileName);
    }

    @Test
    public void test() {
        final ArtifactIdentifier artifactIdentifier = new ArtifactFilename("slf4j-api-1.6.6.jar");
        assertThat(configuration.getIgnoreForSourceResolving().get(0).matches(artifactIdentifier)).isTrue();
        assertThat(configuration.getIgnoreForSourceResolving().size()).isEqualTo(1);

        License license = new License();
        license.setName("EPL-2.0");

        ArtifactIdentifier identifier = new ArtifactFilename("overrideName.jar");
        assertThat(configuration.getFinalLicenses().keySet().stream().anyMatch(k -> k.matches(identifier))).isTrue();
        List<License> list = configuration.getFinalLicenses().entrySet().stream()
                .filter(e -> e.getKey().matches(identifier))
                .map(Map.Entry::getValue)
                .map(LicenseInformation::getLicenses)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        assertThat(list.contains(license)).isTrue();
        assertThat(configuration.isFailOnMissingSources()).isTrue();
        assertThat(configuration.isFailOnIncompleteSources()).isFalse();
        assertThat(configuration.getValidForIncompleteSources().size()).isEqualTo(2);
        assertThat(configuration.getConfiguredSW360Project().getName()).isEqualTo("anyProjectName");
        assertThat(configuration.getConfiguredSW360Project().getVersion()).isEqualTo("anyProjectVersion");

    }

    @Test
    public void testBuildArtifactFlags() {
        String isProprietaryList = configuration.getAddArtifact().stream()
                .map(ArtifactCore::prettyPrint)
                .collect(Collectors.joining(";"));

        assertThat(isProprietaryList).contains("Flags: [isProprietary: false]");
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

        ArtifactIdentifier artifactIdentifier = new ArtifactFilename("overrideAll.jar");
        Artifact generatedArtifact = configuration.getOverride().entrySet().stream()
                .filter(e -> e.getKey().matches(artifactIdentifier))
                .map(Map.Entry::getValue)
                .findAny()
                .get();
        assertThat(generatedArtifact.askForGet(DeclaredLicenseInformation.class).get()
                .getLicenses()
                .get(0)
                .getName()
        ).isEqualTo("testLicense");

        assertThat(generatedArtifact.askFor(ArtifactFilename.class).get().getFilename())
                .isEqualTo("overrideName.jar");
        assertThat(generatedArtifact.askFor(MavenCoordinates.class).get().getArtifactId())
                .isEqualTo("testID");
        assertThat(generatedArtifact.askFor(MavenCoordinates.class).get().getGroupId())
                .isEqualTo("testGroupId");
        assertThat(generatedArtifact.askFor(MavenCoordinates.class).get().getVersion())
                .isEqualTo("testVersion");
        assertThat(generatedArtifact.askFor(BundleCoordinates.class).get().getSymbolicName())
                .isEqualTo("testName");
        assertThat(generatedArtifact.askFor(BundleCoordinates.class).get().getBundleVersion())
                .isEqualTo("testVersion");
    }

    @Test
    public void addArtifactTest() {
        Artifact artifact = configuration.getAddArtifact().get(0);

        assertThat(artifact.askForGet(DeclaredLicenseInformation.class).get().getLicenses().get(0).getName())
                .isEqualTo("Apache");
        assertThat(artifact.askFor(ArtifactFilename.class).get().getFilename())
                .isEqualTo("addArtifact.jar");
        assertThat(artifact.askFor(MavenCoordinates.class).get().getArtifactId())
                .isEqualTo("addArtifactId");
        assertThat(artifact.askFor(MavenCoordinates.class).get().getGroupId())
                .isEqualTo("addGroupId");
        assertThat(artifact.askFor(MavenCoordinates.class).get().getVersion())
                .isEqualTo("addVersion");
        assertThat(artifact.askFor(BundleCoordinates.class).get().getSymbolicName())
                .isEqualTo("addSymbolicName");
        assertThat(artifact.askFor(BundleCoordinates.class).get().getBundleVersion())
                .isEqualTo("addBundleVersion");
        assertThat(artifact.isProprietary())
                .isFalse();
        assertThat(artifact.getMatchState())
                .isEqualTo(MatchState.EXACT);
    }
}
