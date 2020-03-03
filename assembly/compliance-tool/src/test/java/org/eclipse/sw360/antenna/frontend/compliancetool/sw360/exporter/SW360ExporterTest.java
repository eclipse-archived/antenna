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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class SW360ExporterTest {
    private final String clearingState;
    private final SW360ClearingState sw360ClearingState;
    private final int expectedNumOfReleases;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"INITIAL", SW360ClearingState.NEW_CLEARING, 2},
                {"INITIAL", SW360ClearingState.SENT_TO_CLEARING_TOOL, 2},
                {"INITIAL", SW360ClearingState.REPORT_AVAILABLE, 2},
                {"INITIAL", SW360ClearingState.APPROVED, 2},
                {"INITIAL", null, 2},
                {"EXTERNAL_SOURCE", SW360ClearingState.NEW_CLEARING, 2},
                {"EXTERNAL_SOURCE", SW360ClearingState.SENT_TO_CLEARING_TOOL, 2},
                {"EXTERNAL_SOURCE", SW360ClearingState.REPORT_AVAILABLE, 0},
                {"EXTERNAL_SOURCE", SW360ClearingState.APPROVED, 0},
                {"EXTERNAL_SOURCE", null, 2},
                {"AUTO_EXTRACT", SW360ClearingState.NEW_CLEARING, 2},
                {"AUTO_EXTRACT", SW360ClearingState.SENT_TO_CLEARING_TOOL, 2},
                {"AUTO_EXTRACT", SW360ClearingState.REPORT_AVAILABLE, 0},
                {"AUTO_EXTRACT", SW360ClearingState.APPROVED, 0},
                {"AUTO_EXTRACT", null, 2},
                {"PROJECT_APPROVED", SW360ClearingState.NEW_CLEARING, 2},
                {"PROJECT_APPROVED", SW360ClearingState.SENT_TO_CLEARING_TOOL, 2},
                {"PROJECT_APPROVED", SW360ClearingState.REPORT_AVAILABLE, 0},
                {"PROJECT_APPROVED", SW360ClearingState.APPROVED, 0},
                {"PROJECT_APPROVED", null, 2},
                {"OSM_APPROVED", SW360ClearingState.NEW_CLEARING, 2},
                {"OSM_APPROVED", SW360ClearingState.SENT_TO_CLEARING_TOOL, 2},
                {"OSM_APPROVED", SW360ClearingState.REPORT_AVAILABLE, 0},
                {"OSM_APPROVED", SW360ClearingState.APPROVED, 0},
                {"OSM_APPROVED", null, 2},
                {null, SW360ClearingState.NEW_CLEARING, 2},
                {null, SW360ClearingState.SENT_TO_CLEARING_TOOL, 2},
                {null, SW360ClearingState.REPORT_AVAILABLE, 2},
                {null, SW360ClearingState.APPROVED, 2},
                {null, null, 2}
        });
    }

    public SW360ExporterTest(String clearingState, SW360ClearingState sw360ClearingState, int expectedNumOfReleases) {
        this.clearingState = clearingState;
        this.sw360ClearingState = sw360ClearingState;
        this.expectedNumOfReleases = expectedNumOfReleases;
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File csvFile;
    private SW360Release release;

    @Mock
    SW360ComponentClientAdapter componentClientAdapterMock = mock(SW360ComponentClientAdapter.class);

    @Mock
    SW360ReleaseClientAdapter releaseClientAdapterMock = mock(SW360ReleaseClientAdapter.class);

    @Mock
    SW360Configuration configurationMock = mock(SW360Configuration.class);

    @Mock
    SW360ConnectionConfiguration connectionConfigurationMock = mock(SW360ConnectionConfiguration.class);

    HttpHeaders headers = new HttpHeaders();

    @Before
    public void setUp() throws IOException {
        SW360SparseComponent sparseComponent = SW360TestUtils.mkSW360SparseComponent("testComponent");
        when(componentClientAdapterMock.getComponents())
                .thenReturn(Collections.singletonList(sparseComponent));

        SW360Component component = SW360TestUtils.mkSW360Component("testComponent");
        when(componentClientAdapterMock.getComponentById(any()))
                .thenReturn(Optional.of(component));
        release = SW360TestUtils.mkSW360Release("testRelease");
        release.setClearingState(clearingState);
        release.setSw360ClearingState(sw360ClearingState);

        SW360Release release2 = SW360TestUtils.mkSW360Release("testRelease2");
        release2.setClearingState(clearingState);
        release2.setSw360ClearingState(sw360ClearingState);
        release2.setCopyrights("Higher Date");
        release2.setCreatedOn("zzzz-mm-dd");

        when(releaseClientAdapterMock.getReleaseById(any()))
                .thenReturn(Optional.of(release), Optional.of(release2));
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test-source.txt")).getPath());
        when(releaseClientAdapterMock.downloadAttachment(any(), any(), any()))
                .thenReturn(Optional.ofNullable(path));

        when(connectionConfigurationMock.getHttpHeaders())
                .thenReturn(headers);
        when(connectionConfigurationMock.getSW360ComponentClientAdapter())
                .thenReturn(componentClientAdapterMock);
        when(connectionConfigurationMock.getSW360ReleaseClientAdapter())
                .thenReturn(releaseClientAdapterMock);

        when(configurationMock.getConnectionConfiguration())
                .thenReturn(connectionConfigurationMock);
        csvFile = folder.newFile("sample.csv");
        when(configurationMock.getCsvFileName())
                .thenReturn(csvFile.getName());
        when(configurationMock.getTargetDir())
                .thenReturn(folder.getRoot().toPath());
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).getPath();
        Map<String, String> propertiesMap = ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));
        when(configurationMock.getProperties()).
                thenReturn(propertiesMap);
    }

    @Test
    public void testExporter() throws IOException {
        SW360Exporter sw360Exporter = new SW360Exporter();
        sw360Exporter.setConfiguration(configurationMock);
        sw360Exporter.execute();

        assertThat(csvFile).exists();
        CSVParser csvParser = SW360TestUtils.getCsvParser(csvFile);

        List<CSVRecord> records = csvParser.getRecords();

        assertThat(records.size()).isEqualTo(expectedNumOfReleases);
        verify(releaseClientAdapterMock, atLeast(expectedNumOfReleases)).downloadAttachment(any(), any(), any());

        if (expectedNumOfReleases == 2) {
            assertThat(records.get(1).get("Copyrights")).isEqualTo(release.getCopyrights());
        }
    }
}
