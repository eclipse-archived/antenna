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

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class SW360UpdaterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private SW360Configuration configurationMock = mock(SW360Configuration.class);
    private SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);
    private SW360UpdaterImpl updater = mock(SW360UpdaterImpl.class);
    private HttpHeaders header = new HttpHeaders();
    private String clearingDocName = "clearing.doc";

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
                {"INITAL", true, false},
                {"", true, false},
                {"OSM_APPROVED", false, false},
                {"EXTERNAL_SOURCE", false, false},
                {"AUTO_EXTRACT", false, false},
                {"PROJECT_APPROVED", false, false},
                {"INITAL", false, false},
                {"", false, false}

        });
    }

    @Before
    public void setUp() throws IOException {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getPath();
        Map<String, String> propertiesMap = ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));

        Path csvFile = writeCsvFile();
        propertiesMap.put("csvFilePath", csvFile.toString());

        when(configurationMock.getProperties()).
                thenReturn(propertiesMap);

        SW360ConnectionConfiguration connectionConfiguration = mock(SW360ConnectionConfiguration.class);

        when(connectionConfiguration.getSW360ReleaseClientAdapter())
                .thenReturn(releaseClientAdapter);
        when(connectionConfiguration.getHttpHeaders())
                .thenReturn(header);
        when(configurationMock.getConnectionConfiguration())
                .thenReturn(connectionConfiguration);
        when(configurationMock.getBaseDir())
        .thenReturn(Paths.get(propertiesMap.get("csvFilePath")).getParent());

        when(updater.artifactToReleaseInSW360(any()))
                .thenReturn(new SW360Release());
    }

    @Test
    public void testExecute() {
        SW360Release release = mock(SW360Release.class);
        when(release.getClearingState()).thenReturn(clearingState);
        when(updater.artifactToReleaseInSW360(any()))
                .thenReturn(release);

        SW360Updater sw360Updater = new SW360Updater();
        sw360Updater.setUpdater(updater);
        sw360Updater.setConfiguration(configurationMock);

        sw360Updater.execute();

        Map<Path, SW360AttachmentType> testAttachmentMap;
        if(clearingDocAvailable) {
        testAttachmentMap = Collections
                .singletonMap(configurationMock.getBaseDir().resolve(clearingDocName),SW360AttachmentType.CLEARING_REPORT);
        } else {
            testAttachmentMap = Collections.emptyMap();
        }

        verify(updater).artifactToReleaseInSW360(any());
        verify(releaseClientAdapter, times(expectUpload ? 1 : 0)).uploadAttachments(any(), eq(testAttachmentMap), eq(header));
    }

    private Path writeCsvFile() throws IOException {
        final File tempCsvFile = folder.newFile("test.csv");
        String clearingDocPath = "";
        if (clearingDocAvailable) {
            File clearingDoc = folder.newFile(clearingDocName);
            clearingDocPath = clearingDoc.getPath();
        }
        String csvContent = String.format("Artifact Id,Group Id,Version,Coordinate Type,Clearing State,Clearing Document%s" +
                "test,test,x.x.x,mvn,%s,%s%s", System.lineSeparator(), clearingState, clearingDocPath, System.lineSeparator());
        FileUtils.writeStringToFile(tempCsvFile, csvContent);
        return tempCsvFile.toPath();
    }
}
