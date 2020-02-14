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
package org.eclipse.sw360.antenna.ort.workflow.analyzers;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

        return new ArrayList<>(ortResultAnalyzer.createArtifactList(new File(uri)));
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
    public void testParseOrtDataWithScanResultsToArtifacts() throws URISyntaxException, IOException {
        List<Artifact> artifacts = init("scan-result.yml");
        List<String> sourceUrls = makeListOfSourceUrlStrings(artifacts);

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
                .flatMap(s -> Stream.of(s.split("\n"))).toArray())
                .contains("Copyright (c) 2014-2017 Teist Peirson2 <teist.peirson@2.com>");

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(CopyrightStatement.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(s -> Stream.of(s.split("\n"))).toArray())
                .hasSize(4);

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askFor(ArtifactVcsInfo.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(o -> "https://github.com/babel/babel.git"
                        .equals(o.getVcsInfo().getUrl())))
                .isTrue();

        assertThat(sourceUrls).hasSize(1);

        assertThat(sourceUrls).contains("https://registry.npmjs.org/babel-generator/-/babel-generator-6.26.0.tgz");
    }

    private List<String> makeListOfSourceUrlStrings(List<Artifact> artifacts) {
        return artifacts.stream()
                .map(artifact -> artifact.askForGet(ArtifactSourceUrl.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(s -> !"".equals(s))
                .collect(Collectors.toList());
    }
}
