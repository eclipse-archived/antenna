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

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class SW360UpdaterTest {
    private static final String CLEARING_DOC = "clearing.doc";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private SW360Configuration configurationMock = mock(SW360Configuration.class);
    private SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);

    private final String clearingState;
    private final boolean clearingDocAvailable;
    private final boolean expectUpload;

    public SW360UpdaterTest(String clearingState, boolean clearingDocAvailable, boolean expectUpload) {
        this.clearingState = clearingState;
        this.clearingDocAvailable = clearingDocAvailable;
        this.expectUpload = expectUpload;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"OSM_APPROVED", true, true},
                {"EXTERNAL_SOURCE", true, true},
                {"AUTO_EXTRACT", true, true},
                {"PROJECT_APPROVED", true, true},
                {"INITIAL", true, false},
                {"", true, false},
                {"OSM_APPROVED", false, true},
                {"EXTERNAL_SOURCE", false, true},
                {"AUTO_EXTRACT", false, true},
                {"PROJECT_APPROVED", false, true},
                {"INITIAL", false, false},
                {"", false, false}

        });
    }

    private void initConnectionConfiguration() {
        SW360Connection connectionConfiguration = mock(SW360Connection.class);

        when(connectionConfiguration.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);
        when(configurationMock.getConnection())
                .thenReturn(connectionConfiguration);
    }

    private void initBasicConfiguration() throws IOException {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getPath();
        Map<String, String> propertiesMap = ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));

        Path csvFile = writeCsvFile();
        propertiesMap.put("csvFilePath", csvFile.toString());

        when(configurationMock.getProperties()).
                thenReturn(propertiesMap);
        when(configurationMock.getBaseDir())
                .thenReturn(Paths.get(propertiesMap.get("csvFilePath")).getParent());
        when(configurationMock.getTargetDir())
                .thenReturn(getTargetDir());
    }

    @NotNull
    private Path getTargetDir() {
        return folder.getRoot().toPath();
    }

    @Test(expected = NullPointerException.class)
    public void testUpdaterImplMustNotBeNull() {
        new SW360Updater(null, configurationMock, mock(ClearingReportGenerator.class));
    }

    @Test(expected = NullPointerException.class)
    public void testConfigurationMustNotBeNull() {
        new SW360Updater(mock(SW360UpdaterImpl.class), null, mock(ClearingReportGenerator.class));
    }

    @Test(expected = NullPointerException.class)
    public void testClearingReportGeneratorMustNotBeNull() {
        new SW360Updater(mock(SW360UpdaterImpl.class), configurationMock, null);
    }

    @Test
    public void testExecute() throws IOException {
        initBasicConfiguration();

        initConnectionConfiguration();

        SW360UpdaterImpl updater = mock(SW360UpdaterImpl.class);
        when(updater.artifactToReleaseInSW360(any()))
                .thenReturn(new SW360Release());
        SW360Release release = mock(SW360Release.class);
        when(release.getClearingState()).thenReturn(clearingState);
        when(updater.artifactToReleaseInSW360(any()))
                .thenReturn(release);

        ClearingReportGenerator generator = mock(ClearingReportGenerator.class);
        when(generator.createClearingDocument(any(), any()))
                .thenReturn(getTargetDir().resolve(CLEARING_DOC));

        SW360Updater sw360Updater = new SW360Updater(updater, configurationMock, generator);

        sw360Updater.execute();

        Map<Path, SW360AttachmentType> testAttachmentMap = createExpectedAttachmentMap();

        if (expectUpload && !clearingDocAvailable) {
            verify(generator).createClearingDocument(release, getTargetDir());
        }
        verify(updater).artifactToReleaseInSW360(any());
        verify(releaseClientAdapter, times(expectUpload ? 1 : 0)).uploadAttachments(any(), eq(testAttachmentMap));
    }

    private Map<Path, SW360AttachmentType> createExpectedAttachmentMap() {
        if (clearingDocAvailable) {
            return Collections
                    .singletonMap(configurationMock.getBaseDir().resolve(CLEARING_DOC), SW360AttachmentType.CLEARING_REPORT);
        } else if (expectUpload) {
            return Collections
                    .singletonMap(getTargetDir().resolve(CLEARING_DOC), SW360AttachmentType.CLEARING_REPORT);
        }

        return Collections.emptyMap();
    }

    private Path writeCsvFile() throws IOException {
        final File tempCsvFile = folder.newFile("test.csv");
        String clearingDocPath = "";
        if (clearingDocAvailable) {
            File clearingDoc = folder.newFile(CLEARING_DOC);
            clearingDocPath = clearingDoc.getPath();
        }
        String csvContent = String.format("Artifact Id,Group Id,Version,Coordinate Type,Clearing State,Clearing Document%s" +
                "test,test,x.x.x,mvn,%s,%s%s", System.lineSeparator(), clearingState, clearingDocPath, System.lineSeparator());
        Files.write(tempCsvFile.toPath(), csvContent.getBytes(StandardCharsets.UTF_8));
        return tempCsvFile.toPath();
    }
}
