/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.utils;

import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSoftwareHeritageID;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactToReleaseUtilsTest {
    @Test
    public void testRoundTripReleaseArtifactRelease () {
        SW360Release release = TestUtils.mkSW360Release("release");
        Artifact artifact = ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(release, new Artifact());
        SW360Release roundTripRelease = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);

        Assertions.assertThat(roundTripRelease).isEqualTo(release);
    }

    @Test
    public void testRoundTripArtifactReleaseArtifact() {
        Artifact artifact = TestUtils.mkArtifact("artifact", false);
        SW360Release releaseFromArtifact = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);
        Artifact artifactFromRelease = ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(releaseFromArtifact, new Artifact("SW360"));

        assertThat(artifactFromRelease.askForAll(ArtifactFact.class)).containsAll(artifact.askForAll(ArtifactFact.class));
        assertThat(artifactFromRelease).isEqualTo(artifact);
    }

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    @Test
    public void artifactIsMappedToSw360ReleaseCorrectlyWithOverwritten() {
        Artifact artifact = TestUtils.mkArtifact("Test1", true);
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdtest1");
        SW360Release release = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);
        release.setComponentId(sw360Component.getComponentId());
        release.setMainLicenseIds(Collections.EMPTY_SET);

        assertThat(release.getClearingState()).isEqualTo(TestUtils.RELEASE_CLEARING_STATE);
        assertThat(release.getChangeStatus()).isEqualTo(TestUtils.RELEASE_CHANGESTATUS);

        assertThat(release.getDownloadurl()).isEqualTo(TestUtils.RELEASE_DOWNLOAD_URL);
        assertThat(release.getReleaseTagUrl()).isEqualTo(TestUtils.RELEASE_RELEASE_TAG_URL);
        assertThat(release.getSoftwareHeritageId()).isEqualTo(TestUtils.RELEASE_SOFTWAREHERITGAE_ID);
        assertThat(new ArtifactSoftwareHeritageID.Builder(release.getSoftwareHeritageId()).build().toString()).isEqualTo(TestUtils.RELEASE_SOFTWAREHERITGAE_ID);

        assertThat(release.getCoordinates()).hasSize(1);
        assertThat(release.getCoordinates().containsKey("maven")).isTrue();
        assertThat(release.getCoordinates().get("maven")).isEqualTo("pkg:maven/org.group.id/artifactIdTest1@" + TestUtils.RELEASE_VERSION1);

        assertThat(release.getHashes()).hasSize(1);
        assertThat(release.getHashes()).
                isEqualTo(artifact.askForAll(ArtifactFilename.class)
                        .stream()
                        .map(ArtifactFilename::getArtifactFilenameEntries)
                        .flatMap(Collection::stream)
                        .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                        .collect(Collectors.toSet()));

        assertThat(release.getOverriddenLicense()).isEqualTo(TestUtils.RELEASE_OVERRIDEN_LICENSE);
        assertThat(release.getDeclaredLicense()).isEqualTo(TestUtils.RELEASE_DECLEARED_LICENSE);
        assertThat(release.getObservedLicense()).isEqualTo(TestUtils.RELEASE_OBSERVED_LICENSE);

        assertThat(release.getCopyrights()).isEqualTo(TestUtils.RELEASE_COPYRIGHT);
    }

    @Test
    public void artifactIsMappedToSw360ReleaseCorrectlyWithoutOverwritten() {
        Artifact artifact = TestUtils.mkArtifact("Test1", false);
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdtest1");
        SW360Release release = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);

        assertThat(release.getClearingState()).isEqualTo(TestUtils.RELEASE_CLEARING_STATE);
        assertThat(release.getChangeStatus()).isEqualTo(TestUtils.RELEASE_CHANGESTATUS);

        assertThat(release.getDownloadurl()).isEqualTo(TestUtils.RELEASE_DOWNLOAD_URL);
        assertThat(release.getReleaseTagUrl()).isEqualTo(TestUtils.RELEASE_RELEASE_TAG_URL);
        assertThat(release.getSoftwareHeritageId()).isEqualTo(TestUtils.RELEASE_SOFTWAREHERITGAE_ID);
        new ArtifactSoftwareHeritageID.Builder(release.getSoftwareHeritageId()).build();

        assertThat(release.getCoordinates()).containsKeys("maven");
        assertThat(release.getCoordinates()).hasSize(1);
        assertThat(release.getCoordinates()).containsValue("pkg:maven/org.group.id/artifactIdTest1@" + TestUtils.RELEASE_VERSION1);

        assertThat(release.getHashes()).hasSize(1);
        assertThat(release.getHashes()).
                isEqualTo(artifact.askForAll(ArtifactFilename.class)
                        .stream()
                        .map(ArtifactFilename::getArtifactFilenameEntries)
                        .flatMap(Collection::stream)
                        .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                        .collect(Collectors.toSet()));

        assertThat(release.getOverriddenLicense()).isNull();
        assertThat(release.getObservedLicense()).isEqualTo(TestUtils.RELEASE_OBSERVED_LICENSE);
        assertThat(release.getDeclaredLicense()).isEqualTo(TestUtils.RELEASE_DECLEARED_LICENSE);

        assertThat(release.getCopyrights()).isEqualTo(TestUtils.RELEASE_COPYRIGHT);
    }

    @Test
    public void testMainLicenseIsEmptyAndDetectedLicenseNull() {
        Artifact artifact = new Artifact("SW360");
        artifact.setProprietary(false);
        artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, "org.group.id", "artifactIdTest", "1.2.3"));
        artifact.addFact(new DeclaredLicenseInformation(new LicenseStatement()));

        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdTest");
        SW360Release release = ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);
        release.setComponentId(sw360Component.getComponentId());
        release.setMainLicenseIds(Collections.EMPTY_SET);

        assertThat(release.getOverriddenLicense()).isNull();
        assertThat(release.getMainLicenseIds().isEmpty()).isTrue();
    }

    @Test
    public void testArtifactWithoutFacts() {
        Artifact artifact = new Artifact()
                .setProprietary(false);

        thrownException.expect(ExecutionException.class);
        thrownException.expectMessage("does not have enough facts to create a component name.");

        ArtifactToReleaseUtils.convertToReleaseWithoutAttachments(artifact);
    }
}
