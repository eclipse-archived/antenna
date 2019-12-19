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
package org.eclipse.sw360.antenna.sw360.workflow.generator;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.utils.SW360ReleaseAdapterUtils;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360UpdaterTest extends AntennaTestWithMockedContext {

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    private String sourceUrl = "https://thrift.apache.org/";
    private String releaseTagUrl = "https://github.com/apache/thrift/releases/tag/0.10.0";
    private String swhID = "swh:1:rel:ae93ff0b4bdbd6749f75c23ad23311b512230894";
    private String copyrights = "Copyright 2006-2010 The Apache Software Foundation.";

    @Test
    public void artifactIsMappedToSw360ReleaseCorrectlyWithOverwritten() {
        Artifact artifact = mkArtifact("Test1", true);
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdtest1");
        SW360Release release = SW360ReleaseAdapterUtils.convertToRelease(artifact);
        release.setComponentId(sw360Component.getComponentId());
        release.setMainLicenseIds(Collections.EMPTY_SET);

        assertThat(release.getClearingState()).isEqualTo(ArtifactClearingState.ClearingState.PROJECT_APPROVED.toString());
        assertThat(release.getChangeStatus()).isEqualTo(ArtifactChangeStatus.ChangeStatus.CHANGED.toString());

        assertThat(release.getDownloadurl()).isEqualTo(sourceUrl);
        assertThat(release.getReleaseTagUrl()).isEqualTo(releaseTagUrl);
        assertThat(release.getSoftwareHeritageId()).isEqualTo(swhID);
        assertThat(new ArtifactSoftwareHeritageID.Builder(release.getSoftwareHeritageId()).build().toString()).isEqualTo(swhID);

        assertThat(release.getCoordinates()).hasSize(1);
        assertThat(release.getCoordinates().containsKey("maven")).isTrue();
        assertThat(release.getCoordinates().get("maven")).isEqualTo("pkg:maven/org.group.id/artifactIdTest1@1.2.3");

        assertThat(release.getHashes()).hasSize(1);
        assertThat(release.getHashes()).
                isEqualTo(artifact.askForAll(ArtifactFilename.class)
                        .stream()
                        .map(ArtifactFilename::getArtifactFilenameEntries)
                        .flatMap(Collection::stream)
                        .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                        .collect(Collectors.toSet()));

        assertThat(release.getOverriddenLicense()).isEqualTo("Overridden");
        assertThat(release.getDeclaredLicense()).isEqualTo("Declared");
        assertThat(release.getObservedLicense()).isEqualTo("Observed");

        assertThat(release.getCopyrights()).isEqualTo(copyrights);
    }

    @Test
    public void artifactIsMappedToSw360ReleaseCorrectlyWithoutOverwritten() {
        Artifact artifact = mkArtifact("Test1", false);
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdtest1");
        SW360Release release = SW360ReleaseAdapterUtils.convertToRelease(artifact);

        assertThat(release.getClearingState()).isEqualTo(ArtifactClearingState.ClearingState.PROJECT_APPROVED.toString());
        assertThat(release.getChangeStatus()).isEqualTo(ArtifactChangeStatus.ChangeStatus.CHANGED.toString());

        assertThat(release.getDownloadurl()).isEqualTo(sourceUrl);
        assertThat(release.getReleaseTagUrl()).isEqualTo(releaseTagUrl);
        assertThat(release.getSoftwareHeritageId()).isEqualTo(swhID);
        new ArtifactSoftwareHeritageID.Builder(release.getSoftwareHeritageId()).build();

        assertThat(release.getCoordinates()).containsKeys("maven");
        assertThat(release.getCoordinates()).hasSize(1);
        assertThat(release.getCoordinates()).containsValue("pkg:maven/org.group.id/artifactIdTest1@1.2.3");

        assertThat(release.getHashes()).hasSize(1);
        assertThat(release.getHashes()).
                isEqualTo(artifact.askForAll(ArtifactFilename.class)
                        .stream()
                        .map(ArtifactFilename::getArtifactFilenameEntries)
                        .flatMap(Collection::stream)
                        .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                        .collect(Collectors.toSet()));

        assertThat(release.getOverriddenLicense()).isNull();
        assertThat(release.getDeclaredLicense()).isEqualTo("Declared");
        assertThat(release.getObservedLicense()).isEqualTo("Observed");

        assertThat(release.getCopyrights()).isEqualTo(copyrights);
    }

    private Artifact mkArtifact(String name, boolean withOverridden) {
        // License information
        Function<String,LicenseInformation> mkLicenseInformation = licname -> new LicenseInformation() {
            @Override
            public String evaluate() {
                return licname;
            }

            @Override
            public String evaluateLong() {
                return "long " + licname;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public List<License> getLicenses() {
                License license = new License();
                license.setLongName(evaluateLong());
                license.setText("license text for: " + evaluate());
                license.setName(evaluate());
                return Collections.singletonList(license);
            }

            @Override
            public String getLinkStr() {
                return "https://link.to.license" + name + ".invalid";
            }
        };
        Artifact artifact = new Artifact("JSON");
        artifact.setProprietary(false);
        artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, "org.group.id", "artifactId" + name + "", "1.2.3"));
        artifact.addFact(new DeclaredLicenseInformation(mkLicenseInformation.apply("Declared")));
        artifact.addFact(new ObservedLicenseInformation(mkLicenseInformation.apply("Observed")));
        if (withOverridden) {
            artifact.addFact(new OverriddenLicenseInformation(mkLicenseInformation.apply("Overridden")));
        }
        artifact.addFact(new ArtifactSourceUrl(sourceUrl));
        artifact.addFact(new ArtifactReleaseTagURL(releaseTagUrl));
        artifact.addFact(new ArtifactSoftwareHeritageID.Builder(swhID).build());
        artifact.addFact(new ArtifactFilename("test1-file.jar", ("12345678" + name)));
        artifact.addFact(new ArtifactFilename("test2-file.jar", ("12345678" + name)));
        artifact.addFact(new ArtifactClearingState(ArtifactClearingState.ClearingState.valueOf("PROJECT_APPROVED")));
        artifact.addFact(new ArtifactChangeStatus(ArtifactChangeStatus.ChangeStatus.valueOf("CHANGED")));
        artifact.addFact(new CopyrightStatement(copyrights));

        return artifact;
    }

    @Test
    public void testMainLicenseIsEmptyAndDetectedLicenseNull() {
        Artifact artifact = new Artifact("JSON");
        artifact.setProprietary(false);
        artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, "org.group.id", "artifactIdTest", "1.2.3"));
        artifact.addFact(new DeclaredLicenseInformation(new LicenseStatement()));

        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdTest");
        SW360Release release = SW360ReleaseAdapterUtils.convertToRelease(artifact);
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

        SW360ReleaseAdapterUtils.convertToRelease(artifact);
    }
}
