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

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ArtifactTest {
    private Artifact artifact;
    private File jar, mavenSourcesJar;
    private TemporaryFolder temp;
    License license;

    @Before
    public void init() throws IOException {
        this.artifact = new Artifact();
        artifact.getArtifactIdentifier().getMavenCoordinates().setArtifactId("testId");
        artifact.getArtifactIdentifier().getMavenCoordinates().setGroupId("testGroupId");
        artifact.getArtifactIdentifier().getMavenCoordinates().setVersion("testVersion");
        artifact.setMatchState(MatchState.EXACT);
        artifact.setProprietary(true);
        temp = new TemporaryFolder();
        temp.create();
        jar = temp.newFile("jar");
        mavenSourcesJar = temp.newFile("sourcesJar");
        artifact.setJar(jar);
        artifact.setMavenSourceJar(mavenSourcesJar);
        license = new License();
        license.setName("testLicense");
        artifact.setIgnoreForDownload(true);
    }

    @Test
    public void artifactIdentifierTest() {
        assertThat(artifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId()).isEqualTo("testId");
        assertThat(artifact.getArtifactIdentifier().getMavenCoordinates().getGroupId()).isEqualTo("testGroupId");
        assertThat(artifact.getArtifactIdentifier().getMavenCoordinates().getVersion()).isEqualTo("testVersion");
        assertThat(artifact.hasSources()).isTrue();
        assertThat(artifact.isIgnoreForDownload()).isTrue();
        artifact.setIgnoreForDownload(false);
        assertThat(artifact.isIgnoreForDownload()).isFalse();
        artifact.setMavenSourceJar(null);
        artifact.setP2SourceJar(null);
        artifact.setJar(null);
        assertThat(artifact.hasSources()).isFalse();
        artifact.setP2SourceJar(new File(""));
        assertThat(artifact.hasSources()).isTrue();
        artifact.setP2SourceJar(null);
        artifact.setMavenSourceJar(new File(""));

        assertThat(artifact.hasSources()).isTrue();
    }

    @Test
    public void testEquals() {
        assertThat(artifact.equals(artifact)).isTrue();
        assertThat(artifact.equals(null)).isFalse();
        assertThat(artifact.equals(new Artifact())).isFalse();
        assertThat(artifact.equals(new File(""))).isFalse();
        Artifact artifact1 = new Artifact();
        artifact1.setDeclaredLicenses(license);
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
        artifact.setDeclaredLicenses(license);
        artifact.setObservedLicenses(license);
        assertThat(artifact.getDeclaredLicenses()).isEqualTo(license);
        assertThat(artifact.getObservedLicenses()).isEqualTo(license);
    }

    @Test
    public void fileTest() {
        assertThat(artifact.getJar()).isEqualTo(jar);
        assertThat(artifact.getMvnSourceJar()).isEqualTo(mavenSourcesJar);
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

        artifact.setDeclaredLicenses(declaredLicenses);
        artifact.setConfiguredLicense(new LicenseStatement());
        artifact.setOverriddenLicenses(new LicenseStatement());

        assertThat(artifact.getFinalLicenses().evaluate().equals(declaredLicenses.getName()));

        LicenseStatement observedLicenses = new LicenseStatement();
        License license2 = new License();
        license2.setName("license2");
        license2.setLongName("licenseNumber2");
        observedLicenses.setLeftStatement(license2);
        observedLicenses.setRightStatement(declaredLicenses);
        observedLicenses.setOp(LicenseOperator.AND);
        artifact.setObservedLicenses(observedLicenses);
        List<License> licenses = new ArrayList<>();
        licenses.add(license2);
        licenses.add(declaredLicenses);

        assertThat(observedLicenses.evaluate()).isEqualTo("( license2 AND license1 )");
        assertThat(observedLicenses.evaluateLong()).isEqualTo("( licenseNumber2 AND licenseNumber1 )");
        assertThat(artifact.getFinalLicenses()).isEqualTo(declaredLicenses);
        assertThat(observedLicenses.getLicenses().equals(licenses)).isTrue();

        License configuredLicense = new License();
        configuredLicense.setName("license3");
        artifact.setConfiguredLicense(configuredLicense);

        assertThat(artifact.getFinalLicenses()).isEqualTo(configuredLicense);

        License overriddenLicense = new License();
        overriddenLicense.setName("license4");
        artifact.setOverriddenLicenses(overriddenLicense);
        artifact.setConfiguredLicense(new LicenseStatement());
        assertThat(artifact.getFinalLicenses()).isEqualTo(overriddenLicense);
    }
}
