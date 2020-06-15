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
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapterAsync;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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

    private final SW360ComponentClientAdapter componentClientAdapterMock = mock(SW360ComponentClientAdapter.class);

    private final SW360ReleaseClientAdapter releaseClientAdapterMock = mock(SW360ReleaseClientAdapter.class);

    private SW360ReleaseClientAdapterAsync releaseAdapterAsyncMock;

    private SW360Connection connectionMock;

    @Before
    public void setUp() throws URISyntaxException {
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
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test-source.txt")).toURI());
        when(releaseClientAdapterMock.downloadAttachment(any(), any(), any()))
                .thenReturn(Optional.of(path));

        connectionMock = createConnectionMock();
    }

    private SW360Connection createConnectionMock() {
        SW360Connection connection = mock(SW360Connection.class);
        when(connection.getComponentAdapter())
                .thenReturn(componentClientAdapterMock);
        when(connection.getReleaseAdapter())
                .thenReturn(releaseClientAdapterMock);
        releaseAdapterAsyncMock = createReleaseAdapterForDownloads();
        when(connection.getReleaseAdapterAsync())
                .thenReturn(releaseAdapterAsyncMock);
        return connection;
    }

    private SW360Configuration createConfigurationMock(Map<String, String> properties) throws IOException {
        SW360Configuration config = mock(SW360Configuration.class);
        when(config.getConnection())
                .thenReturn(connectionMock);
        csvFile = folder.newFile("sample.csv");
        when(config.getCsvFilePath())
                .thenReturn(csvFile.toPath());
        Path basePath = Paths.get(csvFile.getParent());
        when(config.getBaseDir())
                .thenReturn(basePath);
        when(config.getTargetDir())
                .thenReturn(folder.getRoot().toPath());
        when(config.getProperties()).thenReturn(properties);
        Path sourcesPath = basePath.resolve(properties.get("sourcesDirectory"));
        when(config.getSourcesPath()).thenReturn(sourcesPath);
        return config;
    }

    private Map<String, String> prepareConfigProperties() {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).getPath();
        return ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));
    }

    private SW360ReleaseClientAdapterAsync createReleaseAdapterForDownloads() {
        SW360ReleaseClientAdapterAsync adapter = mock(SW360ReleaseClientAdapterAsync.class);
        when(adapter.downloadAttachment(any(), any(), any()))
                .thenAnswer((Answer<CompletableFuture<Optional<Path>>>) invocationOnMock -> {
                    SW360SparseAttachment attachment = invocationOnMock.getArgument(1);
                    Path downloadPath = Paths.get("download" + attachment.getFilename());
                    return CompletableFuture.completedFuture(Optional.of(downloadPath));
                });
        return adapter;
    }

    @Test(expected = NullPointerException.class)
    public void testConfigurationMustNotBeNull() {
        new SW360Exporter(null);
    }

    private void runExporterTest(SW360Exporter sw360Exporter) throws IOException {
        sw360Exporter.execute();

        assertThat(csvFile).exists();
        CSVParser csvParser = SW360TestUtils.getCsvParser(csvFile, ',');

        List<CSVRecord> records = csvParser.getRecords();

        assertThat(records.size()).isEqualTo(expectedNumOfReleases);
        verify(releaseAdapterAsyncMock, atLeast(expectedNumOfReleases)).downloadAttachment(any(), any(), any());

        if (expectedNumOfReleases == 2) {
            assertThat(records.get(1).get("Copyrights")).isEqualTo(release.getCopyrights());
        }
    }

    @Test
    public void testExporter() throws IOException {
        SW360Configuration configurationMock = createConfigurationMock(prepareConfigProperties());
        SW360Exporter sw360Exporter = new SW360Exporter(configurationMock);

        runExporterTest(sw360Exporter);
    }

    @Test
    public void testExporterWithCleanupOfUnreferencedSources() throws IOException {
        Map<String, String> configProperties = prepareConfigProperties();
        configProperties.put(SW360Exporter.PROP_REMOVE_SOURCES, "true");
        SW360Configuration configurationMock = createConfigurationMock(configProperties);
        SourcesExporter sourcesExporter = spy(new SourcesExporter(configurationMock.getSourcesPath()));
        SW360Exporter exporter = new SW360Exporter(configurationMock, sourcesExporter);

        runExporterTest(exporter);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SourcesExporter.ReleaseWithSources>> captor =
                ArgumentCaptor.forClass(Collection.class);
        verify(sourcesExporter).removeUnreferencedFiles(captor.capture());
        assertThat(captor.getValue()).hasSize(expectedNumOfReleases);
    }
}
