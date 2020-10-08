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


import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360UpdaterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final SW360Configuration configurationMock = mock(SW360Configuration.class);

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
    public void testUpdateWithFailure() throws IOException {
        SW360Release testRelease = SW360TestUtils.mkSW360Release("test");

        SW360UpdaterImpl updaterImpl = mock(SW360UpdaterImpl.class);
        when(updaterImpl.artifactToReleaseInSW360(any(), any()))
                .thenReturn(testRelease);
        when(updaterImpl.artifactToReleaseWithUploads(any(), any(), any()))
                .thenReturn(new AttachmentUploadResult<>(testRelease));

        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getPath();
        final Map<String, String> properties = ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));
        initConfiguration(properties);

        SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);

        SW360Connection connection = mock(SW360Connection.class);
        when(connection.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);
        when(configurationMock.getConnection()).thenReturn(connection);

        // the creation of the clearing document will failed one time and be successful one time
        final ClearingReportGenerator clearingReportGenerator = mock(ClearingReportGenerator.class);
        when(clearingReportGenerator.createClearingDocument(any(), any()))
                .thenThrow(new SW360ClientException("Clearing doc generation error"))
                .thenReturn(folder.newFile("clearing_document.test").toPath());
        SW360Updater updater = new SW360Updater(updaterImpl, configurationMock, clearingReportGenerator);

        // and an exception is nevertheless thrown by SW360ReleaseClientAdapterAsyncImpl
        Assertions.assertThatThrownBy(updater::execute)
                .isInstanceOf(SW360ClientException.class);

        // but we expect that attachment upload worked one time exactly
        verify(releaseClientAdapter, times(1)).uploadAttachments(any());
    }

    private void initConfiguration(Map<String, String> propertiesMap) throws IOException {
        Path csvFile = SW360TestUtils.writeCsvFile(folder,
                "",
                ArtifactClearingState.ClearingState.OSM_APPROVED,
                "");

        SW360TestUtils.initConfigProperties(configurationMock, propertiesMap);
        when(configurationMock.getBaseDir())
                .thenReturn(csvFile.getParent());
        when(configurationMock.getCsvFilePath())
                .thenReturn(csvFile);
        when(configurationMock.getSourcesPath())
                .thenReturn(folder.getRoot().toPath());
    }
}
