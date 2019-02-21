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
package org.eclipse.sw360.antenna.workflow.analyzers;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.junit.Before;
import org.junit.Test;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class OrtResultAnalyzerTest {

    private OrtResultAnalyzer ortResultAnalyzer;

    @Before
    public void setUp() {
        ortResultAnalyzer = new OrtResultAnalyzer();
    }

    private List<Artifact> init(String filename) throws URISyntaxException, IOException {

        URI uri = this.getClass().getClassLoader().getResource(filename).toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));

        return new ArrayList<>(ortResultAnalyzer.createArtifactList(iStream));
    }

    @Test
    public void testMapLicenses() throws URISyntaxException, IOException {
        List<Artifact> artifacts = init("single_package.yml");

        assertThat(artifacts.get(0).askForGet(DeclaredLicenseInformation.class).get().evaluate()).isEqualTo("ISC");

        assertThat(artifacts.get(2).askForGet(DeclaredLicenseInformation.class).get().evaluate()).isEqualTo("( Apache License 2.0 AND ( LGPL 2.1 AND MPL 1.1 ) )");
    }

    @Test
    public void testParseOrtDataWithoutAnalyzerResultsToArtifacts() throws URISyntaxException, IOException {
        List<Artifact> artifacts = init("scan-result-nullAnalyzer.yml");

        assertThat(artifacts.stream()
        .map(artifact -> artifact.askForGet(ObservedLicenseInformation.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LicenseInformation::evaluate))
                .contains("CPL-1.0");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(CopyrightStatement.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(s -> Stream.of(s.split("/n"))).collect(Collectors.toList()).toArray())
                .contains("\"" + "Copyright (c) 2014-2017 Teist Peirson2 <teist.peirson@2.com>" + "\"");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(CopyrightStatement.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(s -> Stream.of(s.split("/n"))).collect(Collectors.toList()).toArray())
                .hasSize(1+1); //+1 since first element of array will be empty due to parsing

        assertThat(artifacts.stream().
                map(artifact -> artifact.askForGet(ArtifactSourceUrl.class)).
                filter(Optional::isPresent).
                map(Optional::get).
                filter(s -> s != "").
                collect(Collectors.toList())).hasSize(1);

        assertThat(artifacts.stream().
                map(artifact -> artifact.askForGet(ArtifactSourceUrl.class)).
                filter(Optional::isPresent).
                map(Optional::get).
                filter(s -> s != "").
                collect(Collectors.toList())).contains("https:/some.jar");
    }

    @Test
    public void testParseOrtDataToArtifacts() throws URISyntaxException, IOException {
        List<Artifact> artifacts = init("analyzer-result.yml");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(DeclaredLicenseInformation.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LicenseInformation::evaluate))
                .contains("MIT");
        assertThat(artifacts.stream()
                .map(Artifact::getAnalysisSource)).contains("OrtResult");
    }

    @Test
    public void testMapCoordinates() throws URISyntaxException, IOException {
        List<Artifact> artifacts = init("single_package.yml");

        Optional<JavaScriptCoordinates> optionalJavaScriptCoordinates = artifacts.stream()
                .map(artifact -> artifact.askFor(JavaScriptCoordinates.class))
                .filter(Optional::isPresent)
                .findAny()
                .map(Optional::get);

        assertThat(optionalJavaScriptCoordinates.isPresent()).isTrue();
        JavaScriptCoordinates javaScriptCoordinates = optionalJavaScriptCoordinates.get();
        assertThat(javaScriptCoordinates.getName()).isEqualTo("abbrev");
        assertThat(javaScriptCoordinates.getVersion()).isEqualTo("1.0.9");
        assertThat(javaScriptCoordinates.getArtifactId()).isEqualTo("abbrev-1.0.9");


        Optional<DotNetCoordinates> optionalDotNetCoordinates = artifacts.stream()
                .map(artifact -> artifact.askFor(DotNetCoordinates.class))
                .filter(Optional::isPresent)
                .findAny()
                .map(Optional::get);

        assertThat(optionalDotNetCoordinates.isPresent()).isTrue();
        DotNetCoordinates dotNetCoordinates = optionalDotNetCoordinates.get();
        assertThat(dotNetCoordinates.getPackageId()).isEqualTo("Newtonsoft.Json.Bson");
        assertThat(dotNetCoordinates.getVersion()).isEqualTo("1.0.1");


        Optional<MavenCoordinates> optionalMavenCoordinates = artifacts.stream()
                .map(artifact -> artifact.askFor(MavenCoordinates.class))
                .filter(Optional::isPresent)
                .findAny()
                .map(Optional::get);

        assertThat(optionalMavenCoordinates.isPresent()).isTrue();
        MavenCoordinates mavenCoordinates = optionalMavenCoordinates.get();
        assertThat(mavenCoordinates.getGroupId()).isEqualTo("org.javassist");
        assertThat(mavenCoordinates.getArtifactId()).isEqualTo("javassist");
        assertThat(mavenCoordinates.getVersion()).isEqualTo("3.21.0-GA");

    }

    @Test
    public void testParseOrtDataWithScanResultsToArtifacts() throws URISyntaxException, IOException {
        List<Artifact> artifacts = init("scan-result.yml");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(ObservedLicenseInformation.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LicenseInformation::evaluate))
                .contains("( Apache-2.0 AND ( BSD-2-Clause AND MIT ) )");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(DeclaredLicenseInformation.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LicenseInformation::evaluate))
                .doesNotContain("( Apache-2.0 AND ( BSD-2-Clause AND MIT ) )");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(CopyrightStatement.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(s -> Stream.of(s.split("/n"))).collect(Collectors.toList()).toArray())
                .contains("\"" + "Copyright (c) 2014-2017 Teist Peirson2 <teist.peirson@2.com>" + "\"");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(CopyrightStatement.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(s -> Stream.of(s.split("/n"))).collect(Collectors.toList()).toArray())
                .hasSize(4 + 1); //+1 since first element of array will be empty due to parsing

        assertThat(artifacts.stream().
                map(artifact -> artifact.askForGet(ArtifactSourceUrl.class)).
                filter(Optional::isPresent).
                map(Optional::get).
                filter(s -> s != "").
                collect(Collectors.toList())).hasSize(0);
    }
}
