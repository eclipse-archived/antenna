/*
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2020.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package com.eclipse.sw360.antenna.cyclonedx;

import org.cyclonedx.model.Component;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.junit.jupiter.api.Test;

import static com.eclipse.sw360.antenna.cyclonedx.ArtifactToComponentConverter.toComponent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArtifactToComponentConverterTest {

    @Test
    void disallowsNull() {
        assertThatThrownBy(() -> toComponent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Artifact must not be null");
    }

    @Test
    void convertingMavenArtifact() {
        Artifact myArtifact = new ArtifactTestHarness.Builder()
                .setMaven("com.example", "myArtifact", "42")
                .build();
        Component component = toComponent(myArtifact);

        assertThat(component.getPurl()).isEqualTo("pkg:maven/com.example/myArtifact@42");
        assertThat(component.getName()).isEqualTo("myArtifact");
        assertThat(component.getGroup()).isEqualTo("com.example");
        assertThat(component.getVersion()).isEqualTo("42");
        assertThat(component.getHashes()).isEmpty();
        assertThat(component.getLicenseChoice().getLicenses()).isNull();
    }

    @Test
    void convertingFileHasChecksumAndPackageUrl() {
        Artifact myArtifact = new ArtifactTestHarness.Builder()
                .addFilename("model-1.0.0-SNAPSHOT.jar", "9bb1b31831f849f34a274f608640f5da6e867571", "sha-1")
                .build();
        Component component = toComponent(myArtifact);

        assertThat(component.getHashes()).hasSize(1).extracting("value").containsExactly("9bb1b31831f849f34a274f608640f5da6e867571");
        assertThat(component.getPurl()).isEqualTo("pkg:generic/model-1.0.0-SNAPSHOT.jar?checksum=sha-1%3A9bb1b31831f849f34a274f608640f5da6e867571&download_url=file%3A%2F%2Fmodel-1.0.0-SNAPSHOT.jar");
    }

    @Test
    void convertingFileWithUnknownChecksumHasAtLeastPackageUrl() {
        Artifact myArtifact = new ArtifactTestHarness.Builder()
                .addFilename("model-1.0.0-SNAPSHOT.jar", "abcdef")
                .build();

        Component component = toComponent(myArtifact);
        assertThat(component.getPurl()).isEqualTo("pkg:generic/model-1.0.0-SNAPSHOT.jar?checksum=UNKNOWN%3Aabcdef&download_url=file%3A%2F%2Fmodel-1.0.0-SNAPSHOT.jar");
    }

    @Test
    void licenseInformationIsAdded() {
        Artifact myArtifact = new ArtifactTestHarness.Builder()
                .setNPM("@angular/isTrue", "23_0_3")
                .setDeclared("MIT")
                .build();
        Component component = toComponent(myArtifact);
        assertThat(component.getPurl()).isEqualTo("pkg:npm/%40angular%2Fistrue@23_0_3");
        assertThat(component.getName()).isEqualTo("@angular/istrue");
        assertThat(component.getVersion()).isEqualTo("23_0_3");
        assertThat(component.getLicenseChoice().getLicenses())
                .hasSize(1)
                .extracting("id").containsExactly("MIT");
    }

    @Test
    void convertingEmptyArtifactFailsWithException() {
        assertThatThrownBy(() -> toComponent(ArtifactTestHarness.EMPTY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No artifactFilename available for artifact: Artifact=[no identifier]");
    }


}
