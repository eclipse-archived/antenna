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
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.license.EmptyLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
                .addFact(new MavenCoordinates("artifactId", "groupId", "version"))
                .addFact(new GenericArtifactCoordinates("name", "version"))
                .addFact(new ArtifactFile(jar))
                .addFact(new ArtifactSourceFile(mavenSourcesJar))
                .setProprietary(true);


        license = new License.Builder().setLicenseId("testLicense").build();
        artifact.setFlag(Artifact.IS_IGNORE_FOR_DOWNLOAD_KEY);
    }

    @Test
    public void artifactTest() {
        System.out.println(artifact.prettyPrint());
        assertThat(artifact.getArtifactIdentifiers().size()).isEqualTo(3);

        Optional<GenericArtifactCoordinates> oArtifactCoordinates = artifact.askFor(GenericArtifactCoordinates.class);
        assertThat(oArtifactCoordinates.isPresent()).isTrue();
        GenericArtifactCoordinates artifactCoordinates = oArtifactCoordinates.get();
        assertThat(artifactCoordinates.getName()).isEqualTo("name");
        assertThat(artifactCoordinates.getVersion()).isEqualTo("version");

        final MavenCoordinates mavenCoordinates = artifact.askFor(MavenCoordinates.class).get();
        assertThat(mavenCoordinates.getName()).isEqualTo("groupId:artifactId");
        assertThat(mavenCoordinates.getVersion()).isEqualTo("version");
        assertThat(mavenCoordinates.getGroupId()).isEqualTo("groupId");
        assertThat(mavenCoordinates.getArtifactId()).isEqualTo("artifactId");

        File found = artifact.askForGet(ArtifactFile.class).get().toFile();
        assertThat(found).isEqualTo(jar.toFile());
    }

    @Test
    public void artifactIdentifierTest() {
        assertThat(artifact.askFor(MavenCoordinates.class).get().getArtifactId()).isEqualTo("artifactId");
        assertThat(artifact.askFor(MavenCoordinates.class).get().getGroupId()).isEqualTo("groupId");
        assertThat(artifact.askFor(MavenCoordinates.class).get().getVersion()).isEqualTo("version");
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
        License declaredLicenses = new License.Builder()
                .setLicenseId("license1")
                .setName("licenseNumber1")
                .build();

        artifact.addFact(new DeclaredLicenseInformation(declaredLicenses));
        artifact.addFact(new ConfiguredLicenseInformation(new EmptyLicenseInformation()));
        artifact.addFact(new OverriddenLicenseInformation(new EmptyLicenseInformation()));

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact).toSpdxExpression().equals(declaredLicenses.getLicenseId()));

        License license2 = new License.Builder()
                .setLicenseId("license2")
                .setName("licenseNumber2")
                .build();
        LicenseInformation observedLicenses = license2.and(declaredLicenses);
        artifact.addFact(new ObservedLicenseInformation(observedLicenses));
        Set<License> licenses = new HashSet<>();
        licenses.add(license2);
        licenses.add(declaredLicenses);

        assertThat(observedLicenses.toSpdxExpression()).isEqualTo("license2 AND license1");
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact).toSpdxExpression()).isEqualTo("license2 AND license1");
        assertThat(observedLicenses.getLicenses().equals(licenses)).isTrue();

        License configuredLicense = new License.Builder().setLicenseId("license3").build();
        artifact.addFact(new ConfiguredLicenseInformation(configuredLicense));

        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact)).isEqualTo(configuredLicense);

        License overriddenLicense = new License.Builder().setLicenseId("license4").build();
        artifact.addFact(new OverriddenLicenseInformation(overriddenLicense));
        artifact.addFact(new ConfiguredLicenseInformation(new EmptyLicenseInformation()));
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifact)).isEqualTo(overriddenLicense);
    }
}
