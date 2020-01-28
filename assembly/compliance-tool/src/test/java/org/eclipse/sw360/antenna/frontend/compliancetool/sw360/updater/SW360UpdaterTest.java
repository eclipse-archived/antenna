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
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SW360UpdaterTest {
    private SW360Configuration configurationMock = mock(SW360Configuration.class);
    private SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);
    private SW360UpdaterImpl updater = mock(SW360UpdaterImpl.class);
    HttpHeaders header = new HttpHeaders();

    @Before
    public void setUp() {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getPath();
        Map<String, String> propertiesMap = ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));
        when(configurationMock.getProperties()).
                thenReturn(propertiesMap);

        SW360ConnectionConfiguration connectionConfiguration = mock(SW360ConnectionConfiguration.class);

        when(releaseClientAdapter.uploadAttachments(any(), any(), eq(header)))
                .thenReturn(new SW360Release());
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
        SW360Updater sw360Updater = new SW360Updater();
        sw360Updater.setUpdater(updater);
        sw360Updater.setConfiguration(configurationMock);

        sw360Updater.execute();

        Map<Path, SW360AttachmentType> testAttachmentMap = Collections
                .singletonMap(configurationMock.getBaseDir().resolve("test-source.txt"),SW360AttachmentType.CLEARING_REPORT);

        verify(updater, atLeast(1)).artifactToReleaseInSW360(any());
        verify(releaseClientAdapter, atLeast(1)).uploadAttachments(any(), eq(testAttachmentMap), eq(header));
    }
}
