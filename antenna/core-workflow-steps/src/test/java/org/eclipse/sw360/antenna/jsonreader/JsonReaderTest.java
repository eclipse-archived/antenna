/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.jsonreader;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonReaderTest {
    private List<Artifact> artifacts;

    @Test
    public void testMapLicenses() throws URISyntaxException, IOException {
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), StandardCharsets.UTF_8);
        URI uri = this.getClass().getClassLoader().getResource("JsonReaderTest/data.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));
        artifacts = jsonReader.createArtifactsList(iStream);
        assertThat(artifacts.get(0).askForGet(DeclaredLicenseInformation.class).get().evaluate()).isEqualTo("( license1 AND ( license2 AND license3 ) )");
    }

    @Test
    public void testMappingAdditionalInformationForLicenses() throws URISyntaxException, IOException {
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), StandardCharsets.UTF_8);
        URI uri = this.getClass().getClassLoader().getResource("JsonReaderTest/data.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));
        artifacts = jsonReader.createArtifactsList(iStream);

        assertThat(artifacts.get(0).askForGet(ObservedLicenseInformation.class)).isEmpty();
        assertThat(artifacts.get(0).askFor(MissingLicenseInformation.class).get().getMissingLicenseReasons())
                .containsExactlyInAnyOrder(MissingLicenseReasons.NO_LICENSE_IN_SOURCES);
    }

    @Test
    public void testParseData2() throws URISyntaxException, IOException {
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), StandardCharsets.UTF_8);
        URI uri = this.getClass().getClassLoader().getResource("JsonReaderTest/data2.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));

        artifacts = jsonReader.createArtifactsList(iStream);

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(DeclaredLicenseInformation.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LicenseInformation::evaluate))
                .contains("PUBLIC-DOMAIN");
        assertThat(artifacts.stream()
                .map(Artifact::getAnalysisSource)).contains("JSON");

        Optional<List<Issue>> issues = artifacts.stream()
                .map(artifact -> artifact.askForGet(ArtifactIssues.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        assertThat(issues.isPresent()).isTrue();
        issues.ifPresent(i -> {
            assertThat(i.stream().map(Issue::getStatus)).contains(SecurityIssueStatus.OPEN);
            assertThat(i.stream().map(Issue::getReference)).contains("CVE-2018-8014");
            assertThat(i.stream().map(Issue::getSeverity)).contains(5.9);
        });
    }

    @Test
    public void testMapCoordinates() throws URISyntaxException, IOException {
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), StandardCharsets.UTF_8);
        URI uri = this.getClass().getClassLoader().getResource("JsonReaderTest/data3.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));
        artifacts = jsonReader.createArtifactsList(iStream);

        artifacts.stream()
                .map(artifact -> artifact.askFor(MavenCoordinates.class))
                .filter(Optional::isPresent)
                .findAny()
                .map(Optional::get)
                .ifPresent(mC -> {
                    assertThat(mC.getGroupId()).isEqualTo("org.apache.commons");
                    assertThat(mC.getArtifactId()).isEqualTo("commons-lang3");
                    assertThat(mC.getVersion()).isEqualTo("3.5");
                });

        artifacts.stream()
                .map(artifact -> artifact.askFor(JavaScriptCoordinates.class))
                .filter(Optional::isPresent)
                .findAny()
                .map(Optional::get)
                .ifPresent(jsC -> {
                    assertThat(jsC.getName()).isEqualTo("process");
                    assertThat(jsC.getVersion()).isEqualTo("0.5.1");
                    assertThat(jsC.getArtifactId()).isEqualTo("process-0.5.1");
                });

        artifacts.stream()
                .map(artifact -> artifact.askFor(DotNetCoordinates.class))
                .filter(Optional::isPresent)
                .findAny()
                .map(Optional::get)
                .ifPresent(mC -> {
                    assertThat(mC.getPackageId()).isEqualTo("Microsoft.AspNetCore.SignalR");
                    assertThat(mC.getVersion()).isEqualTo("1.0.4");
                });
    }
}
