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
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactTest {
    private Artifact artifact;
    private Path jar, mavenSourcesJar;
    private TemporaryFolder temp;
    private License license;

    @Before
    public void init() throws IOException {
        temp = new TemporaryFolder();
        temp.create();
        jar = temp.newFile("jar.jar").toPath();
        mavenSourcesJar = temp.newFile("sourcesJar.jar").toPath();

        artifact = new Artifact("Test")
                .addCoordinate(new Coordinate(Coordinate.Types.MAVEN, "groupId", "artifactId", "version"))
                .addCoordinate(new Coordinate("name", "version"))
                .addFact(new ArtifactFile(jar))
                .addFact(new ArtifactSourceFile(mavenSourcesJar))
                .addFact(new ArtifactMatchingMetadata(MatchState.EXACT))
                .setProprietary(true);


        license = new License();
        license.setName("testLicense");
        artifact.setFlag(Artifact.IS_IGNORE_FOR_DOWNLOAD_KEY);
    }

    @Test
    public void artifactTest() {
        System.out.println(artifact.prettyPrint());
        assertThat(artifact.getArtifactIdentifiers().size()).isEqualTo(2);

        assertThat(new ArtifactCoordinates(new Coordinate("name", "version")).matches(artifact)).isTrue();
        assertThat(new ArtifactCoordinates(new Coordinate("name", "otherVersion")).matches(artifact)).isFalse();
        assertThat(new ArtifactCoordinates(new Coordinate("name", "")).matches(artifact)).isFalse();
        assertThat(new ArtifactCoordinates(new Coordinate("name", null)).matches(artifact)).isTrue();
        assertThat(new ArtifactCoordinates(new Coordinate("name", "*")).matches(artifact)).isTrue();
        assertThat(new ArtifactCoordinates(new Coordinate("name", "v*n")).matches(artifact)).isTrue();
        assertThat(new ArtifactCoordinates(new Coordinate(Coordinate.Types.P2, "name", "version")).matches(artifact)).isFalse();
        assertThat(new ArtifactCoordinates(new Coordinate(Coordinate.Types.MAVEN, "groupId", "artifactId", "version")).matches(artifact)).isTrue();

        File found = artifact.askForGet(ArtifactFile.class).get().toFile();
        assertThat(found).isEqualTo(jar.toFile());
    }

    @Test
    public void artifactIdentifierTest() {
        assertThat(artifact.askFor(ArtifactFile.class).isPresent()).isTrue();
        assertThat(artifact.getFlag(Artifact.IS_IGNORE_FOR_DOWNLOAD_KEY)).isTrue();
    }

    @Test
    public void artifactFilenameTest() {
        artifact.addFact(new ArtifactFilename("test1", "12345Test1"));
        artifact.addFact(new ArtifactFilename("test2", "12345Test2"));
        artifact.addFact(new ArtifactFilename("test1", "12345Test1"));

        assertThat(artifact.askFor(ArtifactFilename.class).get().getArtifactFilenameEntries()).hasSize(2);
        assertThat(artifact.askFor(ArtifactFilename.class).get().getFilenames()).isEqualTo(Arrays.asList("test1", "test2"));
    }

    @Test
    public void testEquals() {
        assertThat(artifact).isEqualTo(artifact);
        assertThat(artifact).isNotNull();
        assertThat(artifact).isNotEqualTo(new Artifact());
        assertThat(artifact).isNotEqualTo(new File(""));
        Artifact artifact1 = new Artifact();
        artifact1.addFact(new DeclaredLicenseInformation(license));
        assertThat(artifact.equals(artifact1)).isFalse();

    }

    @Test
    public void matchStateTest() {
        assertThat(artifact.getMatchState()).isEqualTo(MatchState.EXACT);
    }

    @Test
    public void proprietaryTest() {
        assertThat(artifact.isProprietary()).isTrue();
    }

    @Test
    public void licenseTest() {
        Artifact artifact = new Artifact();
        artifact.addFact(new DeclaredLicenseInformation(license));
        artifact.addFact(new ObservedLicenseInformation(license));
        assertThat(artifact.askForGet(DeclaredLicenseInformation.class).get()).isEqualTo(license);
        assertThat(artifact.askForGet(ObservedLicenseInformation.class).get()).isEqualTo(license);
    }

    @Test
    public void fileTest() {
        assertThat(artifact.askForGet(ArtifactFile.class).orElseThrow(null).toFile())
                .isEqualTo(jar.toFile());
        assertThat(artifact.askForGet(ArtifactSourceFile.class).orElseThrow(null).toFile())
                .isEqualTo(mavenSourcesJar.toFile());
        temp.delete();
    }

    @Test
    public void hashTest() {
        artifact.hashCode();
    }

    @Test
    public void testGetFinalLicense() {
        Artifact artifact = new Artifact();
        License declaredLicenses = new License();
        declaredLicenses.setName("license1");
        declaredLicenses.setLongName("licenseNumber1");

        artifact.addFact(new DeclaredLicenseInformation(declaredLicenses));
        artifact.addFact(new ConfiguredLicenseInformation(new LicenseStatement()));
        artifact.addFact(new OverriddenLicenseInformation(new LicenseStatement()));

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact).evaluate().equals(declaredLicenses.getName()));

        LicenseStatement observedLicenses = new LicenseStatement();
        License license2 = new License();
        license2.setName("license2");
        license2.setLongName("licenseNumber2");
        observedLicenses.setLeftStatement(license2);
        observedLicenses.setRightStatement(declaredLicenses);
        observedLicenses.setOp(LicenseOperator.AND);
        artifact.addFact(new ObservedLicenseInformation(observedLicenses));
        List<License> licenses = new ArrayList<>();
        licenses.add(license2);
        licenses.add(declaredLicenses);

        assertThat(observedLicenses.evaluate()).isEqualTo("( license2 AND license1 )");
        assertThat(observedLicenses.evaluateLong()).isEqualTo("( licenseNumber2 AND licenseNumber1 )");
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact).evaluate()).isEqualTo("( license2 AND license1 )");
        assertThat(observedLicenses.getLicenses().equals(licenses)).isTrue();

        License configuredLicense = new License();
        configuredLicense.setName("license3");
        artifact.addFact(new ConfiguredLicenseInformation(configuredLicense));

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact)).isEqualTo(configuredLicense);

        License overriddenLicense = new License();
        overriddenLicense.setName("license4");
        artifact.addFact(new OverriddenLicenseInformation(overriddenLicense));
        artifact.addFact(new ConfiguredLicenseInformation(new LicenseStatement()));
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact)).isEqualTo(overriddenLicense);
    }
}
