/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.configuration;

import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCore;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.license.LicenseOperator;
import org.eclipse.sw360.antenna.model.license.LicenseStatement;
import org.eclipse.sw360.antenna.model.xml.generated.*;
import org.eclipse.sw360.antenna.xml.XMLResolverJaxB;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

    private Configuration configuration;

    @Before
    public void resolveConfiguration() throws URISyntaxException {
        URL xmlUrl = ConfigurationTest.class.getResource("/antennaconf.xml");
        XMLResolverJaxB resolver = new XMLResolverJaxB(StandardCharsets.UTF_8);
        AntennaConfig config = resolver.resolveXML(new File(xmlUrl.toURI()));
        configuration = new Configuration(config);
    }

    @Test
    public void test() {
        final ArtifactIdentifier artifactIdentifier = new ArtifactFilename("slf4j-api-1.6.6.jar");
        assertThat(configuration.getIgnoreForSourceResolving().get(0).matches(artifactIdentifier)).isTrue();
        assertThat(configuration.getIgnoreForSourceResolving().size()).isEqualTo(1);

        License license = new License();
        license.setId("EPL-2.0");
        license.setCommonName("Eclipse Public License 2.0");

        ArtifactIdentifier identifier = new ArtifactFilename("overrideName.jar");
        assertThat(configuration.getFinalLicenses().keySet().stream().anyMatch(k -> k.matches(identifier))).isTrue();
        List<LicenseInformation> list = configuration.getFinalLicenses().entrySet().stream()
                .filter(e -> e.getKey().matches(identifier))
                .map(Map.Entry::getValue)
                .map(LicenseInformation::getLicenses)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        assertThat(list.contains(license)).isTrue();
        assertThat(configuration.isFailOnMissingSources()).isTrue();
        assertThat(configuration.isFailOnIncompleteSources()).isFalse();
        assertThat(configuration.getValidForIncompleteSources().size()).isEqualTo(2);
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
        final LicenseStatement declaredLicenseInformation = (LicenseStatement) generatedArtifact.askForGet(DeclaredLicenseInformation.class).get();
        assertThat(declaredLicenseInformation
                .getLicenses()
                .size()
        ).isEqualTo(3);
        assertThat(declaredLicenseInformation
                .getOp()
        ).isEqualTo(LicenseOperator.AND);
        assertThat(declaredLicenseInformation
                .getLicenses()
                .get(0)
                .evaluate()
        ).isEqualTo("testLicense");
        assertThat(declaredLicenseInformation
                .evaluate())
                .isEqualTo("( testLicense AND ( otherLicense OR thirdLicense ) )");

        assertThat(generatedArtifact.askFor(ArtifactFilename.class).get().getFilenames())
                .contains("overrideName.jar");
        assertThat(generatedArtifact.askFor(ArtifactCoordinates.class).get().containsPurl("pkg:maven/testGroupId/testID@testVersion")).isTrue();
        assertThat(generatedArtifact.askFor(ArtifactCoordinates.class).get().containsPurl("pkg:p2/testName@testVersion")).isTrue();
    }

    @Test
    public void addArtifactTest() throws Exception {
        Artifact artifact = configuration.getAddArtifact().get(0);
        assertThat(artifact.askForGet(DeclaredLicenseInformation.class).get().getLicenses().get(0).evaluate())
                .isEqualTo("Apache");
        assertThat(artifact.askFor(ArtifactFilename.class).get().getFilenames())
                .contains("addArtifact.jar");
        assertThat(artifact.askFor(ArtifactCoordinates.class).get().containsPurl("pkg:maven/addGroupId/addArtifactId@addVersion")).isTrue();
        assertThat(artifact.askFor(ArtifactCoordinates.class).get().containsPurl("pkg:p2/addSymbolicName@addBundleVersion")).isTrue();
        assertThat(artifact.isProprietary())
                .isFalse();
        assertThat(artifact.getMatchState())
                .isEqualTo(MatchState.EXACT);
    }

    @Test
    public void testSetFinalLicensesWithMultipleXmlElements() {
        ArtifactIdentifier artifactIdentifier = new ArtifactFilename("overrideName.jar");
        License licenseWithOldSyntax = (License) configuration.getFinalLicenses().entrySet().stream()
                .filter(e -> e.getKey().matches(artifactIdentifier))
                .map(Map.Entry::getValue)
                .findAny()
                .get();
        assertThat(licenseWithOldSyntax.getId()).isEqualTo("EPL-2.0");
        assertThat(licenseWithOldSyntax.getCommonName()).isEqualTo("Eclipse Public License 2.0");

        ArtifactIdentifier artifactIdentifier2 = new ArtifactFilename("setFinalLicenses.jar");
        License licenseWithNewSyntax = (License) configuration.getFinalLicenses().entrySet()
                .stream()
                .filter(e -> e.getKey().matches(artifactIdentifier2))
                .map(Map.Entry::getValue)
                .findAny()
                .get();
        assertThat(licenseWithNewSyntax.getId()).isEqualTo("Apache-2.0");
        assertThat(licenseWithNewSyntax.getCommonName()).isEqualTo("Apache License 2.0");
    }
}
