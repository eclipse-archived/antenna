/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.analyzers;

import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.util.ClassCodeSourceLocation;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

public class CsvAnalyzerTest extends AntennaTestWithMockedContext {

    private IProject project;
    private CsvAnalyzer analyzer;
    private License license1, license2;

    @Before
    public void setUp() {
        project = Mockito.mock(IProject.class);
        when(antennaContextMock.getProject()).thenReturn(project);
        when(project.getBasedir()).thenReturn(new File(""));
        when(toolConfigMock.getEncoding()).thenReturn(StandardCharsets.UTF_8);
        analyzer = new CsvAnalyzer();
        analyzer.setAntennaContext(antennaContextMock);

        license1 = new License();
        license1.setId("Apache-2.0");

        license2 = new License();
        license2.setId("Apache-2.0 OR MIT");
    }

    @Test
    public void testCsvAnalyzer() throws URISyntaxException {
        configureAnalyzer("dependencies.csv", ",");

        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        assertThat(artifacts).hasSize(7);

        Collection<String> typesToTest = new ArrayList<>();
        typesToTest.add(Coordinate.Types.MAVEN);
        typesToTest.add(Coordinate.Types.P2);
        typesToTest.add(Coordinate.Types.NPM);
        typesToTest.add(Coordinate.Types.NUGET);
        typesToTest.add(Coordinate.Types.GENERIC);

        assertThat(typesToTest.stream()
                .filter(type -> artifacts.stream().anyMatch(
                        artifact -> artifact.getCoordinateForType(type).isPresent()
                )).count())
                .isEqualTo(typesToTest.size());
    }

     @Test
     public void testThatDataIsFullyMapped() throws URISyntaxException {
         configureAnalyzer("dependencies.csv", ",");

         Set<Artifact> artifacts = analyzer.yield().getArtifacts();


         Artifact foundArtifact = artifacts.stream()
                 .filter(artifact -> "commons-csv".equals(artifact.getCoordinateForType(PackageURL.StandardTypes.MAVEN)
                         .map(Coordinate::getName)
                         .orElse("")))
                 .findFirst()
                 .get();

         commonsCsvFullDependencyCheck(foundArtifact);

         assertThat(foundArtifact.askFor(CopyrightStatement.class).get()).isEqualTo(new CopyrightStatement("Copyright 2005-2016 The Apache Software Foundation"));
     }

     @Test
     public void testThatSourcesIsMappedCorrectly() throws URISyntaxException {
        configureAnalyzer("dependencyWithSource.csv", ",");

        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        Artifact foundArtifact = artifacts.stream()
                .findFirst()
                .get();

         Optional<ArtifactSourceFile> artifactSourceFile = foundArtifact.askFor(ArtifactSourceFile.class);

         assertThat(artifactSourceFile.isPresent()).isTrue();
         assertThat(artifactSourceFile.get().get().toFile()).exists();
         assertThat(artifactSourceFile.get().get().toAbsolutePath()).
                 isEqualTo(Paths.get(this.getClass().getClassLoader().getResource("CsvAnalyzerTest/test_source.txt").toURI()));
     }

     @Test
     public void testThatMissingSourcesDontGetMapped() throws URISyntaxException {
        configureAnalyzer("dependencyWithMissingSource.csv", ",");

         Set<Artifact> artifacts = analyzer.yield().getArtifacts();

         Artifact foundArtifact = artifacts.stream()
                 .findFirst()
                 .get();

         assertThat(foundArtifact.askFor(ArtifactSourceFile.class)).isNotPresent();
     }

    @Test
    public void testExistenceOfClearingDocumentIsChecked() throws URISyntaxException {
        configureAnalyzer("dependencies.csv", ",");

        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        Artifact foundArtifact = artifacts.stream()
                .filter(artifact -> "commons-cli".equals(artifact.getCoordinateForType(PackageURL.StandardTypes.MAVEN)
                        .map(Coordinate::getName)
                        .orElse("")))
                .findFirst()
                .get();

        assertThat(foundArtifact.askFor(ArtifactClearingDocument.class)).isNotPresent();
    }

    @Test
    public void testCopyrightsIsParsedCorrectly() throws URISyntaxException {
        configureAnalyzer("dependencyWithMultipleCopyrights.csv", ",");
        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        assertThat(artifacts).hasSize(2);

        assertThat(artifacts.stream().map(artifact1 -> artifact1.askForGet(CopyrightStatement.class).get())
                .anyMatch(copyrightStatement ->
                        copyrightStatement.equals(new CopyrightStatement("Copyright 2005-2016 The Apache Software Foundation")
                                .mergeWith(new CopyrightStatement("Copyright 2020 Fake Company"))
                                .mergeWith(new CopyrightStatement("Copyright 2020 Fake the 2nd")).get()))).isTrue();
    }

    @Test
    public void testMergeOfDuplicateArtifact() throws URISyntaxException {
        configureAnalyzer("dependencyWithMultipleHashes.csv", ",");
        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        Artifact artifact0 = artifacts.iterator().next();
        assertThat(artifact0.askFor(ArtifactFilename.class).get().getArtifactFilenameEntries()).hasSize(2);
    }

    @Test
    public void testCsvAnalyzerWithSemicolonseparator() throws URISyntaxException {
        configureAnalyzer("dependenciesWithExcelFormat.csv", ";");
        Set<Artifact> artifacts = analyzer.yield().getArtifacts();

        assertThat(artifacts).hasSize(2);

        Artifact foundArtifact = artifacts.stream()
                .filter(artifact -> "commons-csv".equals(artifact.getCoordinateForType(PackageURL.StandardTypes.MAVEN)
                        .map(Coordinate::getName)
                        .orElse("")))
                .findFirst()
                .get();

        commonsCsvFullDependencyCheck(foundArtifact);

        assertThat(foundArtifact.askFor(CopyrightStatement.class).get())
                .isEqualTo(new CopyrightStatement("Copyright 2005-2016 The Apache Software Foundation")
                        .mergeWith(new CopyrightStatement("Copyright, 2020 the fake"))
                        .mergeWith(new CopyrightStatement("Copyright fake; the third")));

        List<String> hashes = foundArtifact.askFor(ArtifactFilename.class).get().
                getArtifactFilenameEntries().stream()
                .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                .collect(Collectors.toList());
        assertThat(hashes).hasSize(2);

        Artifact cliArtifact = artifacts.stream()
                .filter(artifact -> "commons-cli".equals(artifact.getCoordinateForType(PackageURL.StandardTypes.MAVEN)
                                .map(Coordinate::getName)
                                .orElse("")))
                .findFirst()
                .get();

        assertThat(cliArtifact.askFor(CopyrightStatement.class).get()).isEqualTo(
                new CopyrightStatement("Copyright 2005-2016 The Apache Software Foundation")
                        .mergeWith(new CopyrightStatement("copy & paste"))
                        .mergeWith(new CopyrightStatement("this is $ it"))
                        .mergeWith(new CopyrightStatement("!nevermind"))
                        .mergeWith(new CopyrightStatement("§copsright"))
                        .mergeWith(new CopyrightStatement("%gsp"))
                        .mergeWith(new CopyrightStatement("?"))
                        .mergeWith(new CopyrightStatement("`wer`"))
                        .mergeWith(new CopyrightStatement("#wtewp"))
                        .mergeWith(new CopyrightStatement("~fjd"))
        );
    }

    @Override
    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(antennaContextMock, atLeast(0)).getToolConfiguration();
        verify(antennaContextMock, atLeast(0)).getProject();
        verify(antennaContextMock, atLeast(0)).getProcessingReporter();
        verify(toolConfigMock, atLeast(0)).getEncoding();

        // assert that there were no unexpected interactions with the mocked objects
        verifyNoMoreInteractions(antennaContextMock);
        verifyNoMoreInteractions(toolConfigMock);
    }

    private void configureAnalyzer(String fileName, String delimiter) throws URISyntaxException {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("file.path", Paths.get("src", "test", "resources", "CsvAnalyzerTest", fileName).toString());
        configMap.put("base.dir", ClassCodeSourceLocation.getClassCodeSourceLocationAsString(this.getClass()));
        configMap.put("delimiter", delimiter);
        analyzer.configure(configMap);
    }

    private void commonsCsvFullDependencyCheck(Artifact foundArtifact) {
        assertThat(foundArtifact.getMatchState()).isEqualTo(MatchState.EXACT);
        assertThat(foundArtifact.askFor(ArtifactCoordinates.class).get().containsPurl("pkg:maven/org.apache.commons/commons-csv@1.4")).isTrue();

        assertThat(foundArtifact.askFor(OverriddenLicenseInformation.class).get())
                .isEqualTo(new OverriddenLicenseInformation(license1));

        assertThat(foundArtifact.askFor(DeclaredLicenseInformation.class).get())
                .isEqualTo(new DeclaredLicenseInformation(license1));

        assertThat(foundArtifact.askFor(ObservedLicenseInformation.class).get())
                .isEqualTo(new ObservedLicenseInformation(license2));


        assertThat(foundArtifact.askFor(ArtifactFilename.class).get().getArtifactFilenameEntries().iterator().next().getHash()).isEqualTo("620580a88953cbcf4528459e485054e7c27c0889");

        assertThat(foundArtifact.askFor(ArtifactSourceUrl.class).get().get()).contains("http://archive.apache.org/dist/commons/csv/source/commons-csv-1.4-src.zip");
        assertThat(foundArtifact.askFor(ArtifactReleaseTagURL.class).get().toString()).isEqualTo("https://github.com/apache/commons-csv/tree/csv-1.4");
        assertThat(foundArtifact.askFor(ArtifactSoftwareHeritageID.class).get().toString()).isEqualTo("swh:1:cnt:60dbac0aafd98c9ca461256a0cefd8a7aaea8bda");

        assertThat(foundArtifact.askFor(ArtifactClearingState.class).get()).isEqualTo(
                new ArtifactClearingState(ArtifactClearingState.ClearingState.OSM_APPROVED));
        assertThat(foundArtifact.askFor(ArtifactChangeStatus.class).get()).isEqualTo(
                new ArtifactChangeStatus(ArtifactChangeStatus.ChangeStatus.AS_IS));

        assertThat(foundArtifact.askFor(ArtifactCPE.class).get()).isEqualTo(new ArtifactCPE("cpe:2.3:a:apache:commons-csv:1.4"));

        assertThat(foundArtifact.askFor(ArtifactClearingDocument.class).get().get().getFileName().toString()).isEqualTo("clearing.json");
    }
}
