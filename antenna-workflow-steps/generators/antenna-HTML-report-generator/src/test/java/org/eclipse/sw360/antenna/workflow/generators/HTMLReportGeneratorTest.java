/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HTMLReportGeneratorTest extends AntennaTestWithMockedContext {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private HTMLReportGenerator htmlReportGenerator;
    private Charset charset = Charset.forName("UTF-8");

    public final String licenseName1 = "LicenseName1";
    public final String licenseText1 = "Some Super Long LicenseText1";
    public final String licenseName2 = "LicenseName2";
    public final String licenseText2 = "Some Super Long LicenseText2";
    public final String licenseName3 = "LicenseName3";
    public final String licenseText3 = "Some Super Long LicenseText3";
    public final String licenseFullName2 = "LicenseFullName2";
    public final String artifactId1 = "ArtifactId1";
    public final String artifactId2 = "ArtifactId2";
    public final String artifactId3 = "ArtifactId3";
    public final String artifactId4 = "ArtifactId4";

    public Set<ArtifactForHTMLReport> artifacts;

    @Before
    public void setUp() throws Exception {
        when(toolConfigMock.getEncoding()).thenReturn(charset);

        htmlReportGenerator = new HTMLReportGenerator();
        htmlReportGenerator.setAntennaContext(antennaContextMock);
        htmlReportGenerator.configure(Collections.emptyMap());

        final License license1 = new License();
        license1.setName(licenseName1);
        license1.setText(licenseText1);
        final ArtifactForHTMLReport artifact1 = new ArtifactForHTMLReport(artifactId1, license1);

        final ArtifactForHTMLReport artifact2 = new ArtifactForHTMLReport(artifactId2, null);

        final License license2 = new License();
        license2.setName(licenseName2);
        license2.setText(licenseText2);
        license2.setLongName(licenseFullName2);
        final ArtifactForHTMLReport artifact3 = new ArtifactForHTMLReport(artifactId3, license2);

        final License license3 = new License();
        license3.setName(licenseName3);
        license3.setText(licenseText3);
        final LicenseStatement license4 = new LicenseStatement();
        license4.setLeftStatement(license1);
        license4.setRightStatement(license3);
        license4.setOp(LicenseOperator.AND);
        final ArtifactForHTMLReport artifact4 = new ArtifactForHTMLReport(artifactId4, license4);

        artifacts = Stream.of(artifact1, artifact2, artifact3, artifact4).collect(Collectors.toSet());
    }

    @After
    public void tearDown() throws Exception {
        temporaryFolder.delete();
        verify(toolConfigMock).getEncoding();
    }

    @Test
    public void testThatArtifactsAreIncluded() throws Exception {
        final File outputFile = temporaryFolder.newFile();

        htmlReportGenerator.writeReportToFile(artifacts, outputFile);

        byte[] encoded = Files.readAllBytes(outputFile.toPath());
        final String contentOfFile = new String(encoded, charset);

        assertThat(contentOfFile).contains(licenseName1);
        assertThat(contentOfFile).contains(licenseText1);
        assertThat(contentOfFile).contains(artifactId1);
        assertThat(contentOfFile).contains(licenseName2);
        assertThat(contentOfFile).contains(licenseText2);
        assertThat(contentOfFile).contains(licenseFullName2);
        assertThat(contentOfFile).contains(licenseName3);
        assertThat(contentOfFile).contains(licenseText3);
        assertThat(contentOfFile).contains(artifactId2);
        assertThat(contentOfFile).contains(artifactId3);
        assertThat(contentOfFile).contains(artifactId4);

    }

}
