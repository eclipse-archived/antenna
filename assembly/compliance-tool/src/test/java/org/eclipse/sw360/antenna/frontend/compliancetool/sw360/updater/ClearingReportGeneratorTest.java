/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ClearingReportGeneratorTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ClearingReportGenerator generator;

    @Before
    public void setUp() {
        generator = new ClearingReportGenerator();
    }

    @Test
    public void createClearingDocument() throws IOException {
        File targetFolder = folder.newFolder();
        SW360Release release = SW360TestUtils.mkSW360Release("testRelease");
        release.setOverriddenLicense("overridden");

        Path clearingDocument = generator.createClearingDocument(release, targetFolder.toPath());
        assertThat(Files.exists(clearingDocument)).isTrue();
        assertThat(clearingDocument.getFileName().toString()).isEqualTo(release.getId() + "_clearing.json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue(clearingDocument.toFile(), SW360Release.class);

        String content = String.join("", Files.readAllLines(clearingDocument));
        assertThat(content).contains(release.getDeclaredLicense());
        assertThat(content).contains(release.getObservedLicense());
        assertThat(content).contains(release.getOverriddenLicense());

        System.out.println(content);
    }

    @Test(expected = ExecutionException.class)
    public void testExceptionIsThrownOnFailedCreation() {
        Path nonExistingFolder = Paths.get("non", "existing", "folder");
        SW360Release release = SW360TestUtils.mkSW360Release("crash");
        generator.createClearingDocument(release, nonExistingFolder);
    }
}