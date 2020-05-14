/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.OverriddenLicenseInformation;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.license.LicenseOperator;
import org.eclipse.sw360.antenna.model.license.LicenseStatement;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HTMLReportGeneratorTest extends AntennaTestWithMockedContext {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private HTMLReportGenerator htmlReportGenerator;
    private Charset charset = StandardCharsets.UTF_8;

    private static final String LICENSEID1 = "LicenseName1";
    private static final String LICENSETEXT1 = "Some Super Long LicenseText1";
    private static final String LICENSEID2 = "LicenseName2";
    private static final String LICENSETEXT2 = "Some Super Long LicenseText2";
    private static final String LICENSEID3 = "LicenseName3";
    private static final String LICENSETEXT3 = "Some Super Long LicenseText3";
    private static final String LICENSECOMMONNAME = "License Common Name";
    private static final String ARTIFACTID1 = "ArtifactId1";
    private static final String ARTIFACTID2 = "ArtifactId2";
    private static final String ARTIFACTID3 = "ArtifactId3";
    private static final String ARTIFACTID4 = "ArtifactId4";
    private static final String ARTIFACTVERSION1 = "1.0.0";
    private static final String ARTIFACTVERSION2 = "1.1.0";
    private static final String ARTIFACTVERSION3 = "1.0.1";
    private static final String ARTIFACTVERSION4 = "2.3.4";
    private static final List<String> COPYRIGHTLIST = Arrays.asList("Copyright (c) A Name 2020", "Copyright (c) Another Name 2020");
    private static final String COPYRIGHTSTRING = COPYRIGHTLIST.stream().collect(Collectors.joining("\n"));
    private static final String ARTIFACTFILENAME1 = "ArtifactName1";
    private static final String ARTIFACTFILENAME2 = "ArtifactName2";

    public Collection<Artifact> artifacts;

    @Before
    public void setUp() throws Exception {
        when(toolConfigMock.getEncoding()).thenReturn(charset);
        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(temporaryFolder.getRoot().toPath());

        htmlReportGenerator = new HTMLReportGenerator();
        htmlReportGenerator.setAntennaContext(antennaContextMock);
        htmlReportGenerator.configure(Collections.emptyMap());

        final License license1 = new License();
        license1.setId(LICENSEID1);
        license1.setText(LICENSETEXT1);
        final Artifact artifact1 = createArtifact(ARTIFACTID1, ARTIFACTVERSION1, ARTIFACTFILENAME1, license1);

        final Artifact artifact2 = createArtifact(ARTIFACTID2, ARTIFACTVERSION2, ARTIFACTFILENAME2, new License());

        final License license2 = new License();
        license2.setId(LICENSEID2);
        license2.setText(LICENSETEXT2);
        license2.setCommonName(LICENSECOMMONNAME);
        final Artifact artifact3 = createArtifact(ARTIFACTID3, ARTIFACTVERSION3, "", license2);

        final License license3 = new License();
        license3.setId(LICENSEID3);
        license3.setText(LICENSETEXT3);
        final LicenseStatement license4 = new LicenseStatement();
        license4.setLicenses(Stream.of(license1, license3).collect(Collectors.toList()));
        license4.setOp(LicenseOperator.AND);
        final Artifact artifact4 = createArtifact(ARTIFACTID4, ARTIFACTVERSION4, "", license4);

        artifacts = Arrays.asList(artifact1, artifact2, artifact3, artifact4);
    }

    private Artifact createArtifact(String id, String version, String filename, LicenseInformation license) {
        return new Artifact()
                .addCoordinate(Coordinate.builder().withType(Coordinate.Types.GENERIC).withName(id).withVersion(version).build())
                .addFact(new CopyrightStatement(COPYRIGHTSTRING))
                .addFact(new ArtifactFilename(filename))
                .addFact(new OverriddenLicenseInformation(license));
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
        verify(toolConfigMock).getEncoding();
        verify(toolConfigMock, times(2)).getAntennaTargetDirectory();
    }

    @Test
    public void testThatArtifactsAreIncluded() throws Exception {
        htmlReportGenerator.produce(artifacts);

        byte[] encoded = Files.readAllBytes(temporaryFolder.getRoot().toPath().resolve("3rdparty-licenses.html"));
        final String contentOfFile = new String(encoded, charset);

        assertThat(contentOfFile).contains(LICENSEID1);
        assertThat(contentOfFile).contains(LICENSETEXT1);
        assertThat(contentOfFile).contains(ARTIFACTID1);
        assertThat(contentOfFile).contains(ARTIFACTVERSION1);
        assertThat(contentOfFile).contains(LICENSEID2);
        assertThat(contentOfFile).contains(LICENSETEXT2);
        assertThat(contentOfFile).contains(LICENSECOMMONNAME);
        assertThat(contentOfFile).contains(LICENSEID3);
        assertThat(contentOfFile).contains(LICENSETEXT3);
        assertThat(contentOfFile).contains(ARTIFACTID2);
        assertThat(contentOfFile).contains(ARTIFACTVERSION2);
        assertThat(contentOfFile).contains(ARTIFACTID3);
        assertThat(contentOfFile).contains(ARTIFACTVERSION3);
        assertThat(contentOfFile).contains(ARTIFACTID4);
        assertThat(contentOfFile).contains(ARTIFACTVERSION4);
        assertThat(contentOfFile).contains(COPYRIGHTLIST.get(0));
        assertThat(contentOfFile).contains(COPYRIGHTLIST.get(1));
        assertThat(contentOfFile).contains(ARTIFACTFILENAME1);
        assertThat(contentOfFile).contains(ARTIFACTFILENAME2);
        assertThat(temporaryFolder.getRoot().toPath().resolve("styles.css")).exists();
    }
}
