/*
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2020.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package com.eclipse.sw360.antenna.cyclonedx;

import org.cyclonedx.BomParser;
import org.cyclonedx.model.Bom;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.jsonreader.JsonReader;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CycloneDXGeneratorTest {
    @TempDir
    File tempDir;

    CycloneDXGenerator generator = new CycloneDXGenerator();

    Path antennaTargetDir;

    @BeforeEach
    public void setup() throws Exception {
        prepareAntennaContext();
    }

    private void prepareAntennaContext() {
        File antennaDir = new File(tempDir, "antenna");
        antennaDir.mkdir();
        antennaTargetDir = antennaDir.toPath();

        AntennaContext context = mock(AntennaContext.class);
        ToolConfiguration toolConfiguration = mock(ToolConfiguration.class);
        when(context.getToolConfiguration()).thenReturn(toolConfiguration);
        when(toolConfiguration.getAntennaTargetDirectory()).thenReturn(antennaTargetDir);
        generator.setAntennaContext(context);
    }

    private List<Artifact> readIQDataToArtifacts(String resourceName) throws IOException {
        File depDir = new File(tempDir, "depDir");
        depDir.mkdir();

        File recordingFile = new File(tempDir, "recording");

        JsonReader reader = new JsonReader(recordingFile.toPath(), depDir.toPath(), StandardCharsets.UTF_8);

        try (InputStream stream = CycloneDXGeneratorTest.class.getResourceAsStream("/" + resourceName)) {
            assertThat(stream).describedAs("Please check if resource exists in src/test/resources").isNotNull();
            return reader.createArtifactsList(stream);
        }
    }

    @Test
    void simpleReport() throws Exception {

        List<Artifact> artifacts = readIQDataToArtifacts("ReportWithTwoComponents.json");
        Map<String, IAttachable> map = generator.produce(artifacts);

        assertThat(map).hasSize(1);
        IAttachable attachable = map.get("cyclonedx-bom");
        assertThat(attachable.getClassifier()).isEqualTo("cyclonedx-bom");
        assertThat(attachable.getType()).isEqualTo("xml");

        File bomFile = attachable.getFile();
        Bom bom = new BomParser().parse(bomFile);
        assertThat(bom.getComponents()).hasSize(2);
    }

    @Test
    void reportWithMavenAndFiles() throws Exception {
        List<Artifact> artifacts = readIQDataToArtifacts("ReportWithMavenAndFiles.json");
        Map<String, IAttachable> map = generator.produce(artifacts);

        IAttachable attachable = map.get("cyclonedx-bom");
        File bomFile = attachable.getFile();
        Bom bom = new BomParser().parse(bomFile);

        assertThat(bom.getComponents()).hasSize(30);
    }

}
