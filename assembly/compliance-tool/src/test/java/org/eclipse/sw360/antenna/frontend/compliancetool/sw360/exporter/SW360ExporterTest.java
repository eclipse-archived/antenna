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
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SW360ExporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File csvFile;

    @Mock
    SW360ComponentClientAdapter componentClientAdapterMock = mock(SW360ComponentClientAdapter.class);

    @Mock
    SW360ReleaseClientAdapter releaseClientAdapterMock = mock(SW360ReleaseClientAdapter.class);

    @Mock
    SW360ConnectionConfiguration connectionConfigurationMock = mock(SW360ConnectionConfiguration.class);

    @Mock
    HttpHeaders headers = mock(HttpHeaders.class);

    @Before
    public void setUp() throws IOException {
        SW360SparseComponent sparseComponent = SW360TestUtils.mkSW360SparseComponent("testComponent");
        when(componentClientAdapterMock.getComponents(any()))
                .thenReturn(Collections.singletonList(sparseComponent));

        SW360Component component = SW360TestUtils.mkSW360Component("testComponent");
        when(componentClientAdapterMock.getComponentById(any(), any()))
                .thenReturn(Optional.of(component));
        SW360Release release = SW360TestUtils.mkSW360Release("testRelease");

        when(releaseClientAdapterMock.getReleaseById(any(), any()))
                .thenReturn(Optional.of(release));

        when(connectionConfigurationMock.getHttpHeaders())
                .thenReturn(headers);
        when(connectionConfigurationMock.getSW360ComponentClientAdapter())
                .thenReturn(componentClientAdapterMock);
        when(connectionConfigurationMock.getSW360ReleaseClientAdapter())
                .thenReturn(releaseClientAdapterMock);

        csvFile = folder.newFile("sample.csv");
    }

    @Test
    public void testExporter() throws IOException {
        SW360Exporter sw360Exporter = new SW360Exporter();
        sw360Exporter.setConnectionConfiguration(connectionConfigurationMock);
        sw360Exporter.setCsvFile(csvFile);
        sw360Exporter.execute();

        assertThat(csvFile).exists();
        CSVParser csvParser = SW360TestUtils.getCsvParser(csvFile);
        assertThat(csvParser.getRecords().size()).isEqualTo(1);
    }
}
