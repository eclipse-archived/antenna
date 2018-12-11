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
package org.eclipse.sw360.antenna.util;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIssues;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.JavaCoordinates;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonReaderTest {
    private List<Artifact> artifacts;

    @Test
    public void testMapLicenses() throws URISyntaxException, IOException{
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), Charset.forName("UTF-8"));
        URI uri = this.getClass().getClassLoader().getResource("data.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));
        artifacts = jsonReader.createArtifactsList(iStream);
        assertThat(artifacts.get(0).askForGet(DeclaredLicenseInformation.class).get().evaluate()).isEqualTo("( license1 AND ( license2 AND license3 ) )");
    }

    @Test
    public void testParseData2() throws URISyntaxException, IOException{
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), Charset.forName("UTF-8"));
        URI uri = this.getClass().getClassLoader().getResource("data2.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));

        artifacts = jsonReader.createArtifactsList(iStream);

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(DeclaredLicenseInformation.class))
                .map(Optional::get)
                .map(LicenseInformation::evaluate)
                .anyMatch("PUBLIC-DOMAIN"::equals));
        assertThat(artifacts.stream()
                .map(Artifact::getAnalysisSource)
                .allMatch("CSV"::equals));
        assertThat(artifacts.stream()
                .map(artifact -> artifact.askFor(ArtifactFilename.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ArtifactFilename::getFilename)
                .anyMatch("aopalliance-1.0.jar"::equals));

        artifacts.stream()
                .map(artifact -> artifact.askForGet(ArtifactIssues.class))
                .map(Optional::get)
                .findAny()
                .ifPresent(issues -> assertThat(issues.stream()
                        .map(Issue::getStatus)
                        .anyMatch(SecurityIssueStatus.OPEN::equals)));

        artifacts.stream()
                .map(artifact -> artifact.askForGet(ArtifactIssues.class))
                .map(Optional::get)
                .findAny()
                .ifPresent(issues -> assertThat(issues.stream()
                        .map(Issue::getReference)
                        .anyMatch("CVE-2009-1523"::equals)));
    }

    @Test
    public void testMapCoordinates() throws URISyntaxException, IOException {
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), Charset.forName("UTF-8"));
        URI uri = this.getClass().getClassLoader().getResource("data3.json").toURI();
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
