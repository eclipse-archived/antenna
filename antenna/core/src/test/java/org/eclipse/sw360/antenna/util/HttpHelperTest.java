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
package org.eclipse.sw360.antenna.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.exceptions.FailedToDownloadException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.setVariableValueInObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HttpHelperTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private ToolConfiguration toolConfigMock;
    @Mock
    private AntennaContext antennaContextMock;
    @Mock
    private CloseableHttpClient httpClientMock;
    @Mock
    private CloseableHttpResponse httpResponseMock;
    @Mock
    private HttpEntity httpEntityMock;
    @Mock
    private StatusLine statusLine;

    private HttpHelper httpHelper;

    @Before
    public void setUp() throws Exception {
        when(toolConfigMock.getAntennaTargetDirectory())
                .thenReturn(temporaryFolder.newFolder("target").toPath());
        when(antennaContextMock.getToolConfiguration())
                .thenReturn(toolConfigMock);

        httpHelper = new HttpHelper(antennaContextMock);
        setVariableValueInObject(httpHelper, "httpClient", httpClientMock);
    }

    @Test
    public void downloadFileWritesTheFileToDisk() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();
        File expectedJarFile = new File(targetDirectory.toFile(), "archive.zip");

        when(httpResponseMock.getStatusLine())
                .thenReturn(statusLine);
        when(httpResponseMock.getStatusLine().getStatusCode())
                .thenReturn(HttpStatus.SC_OK);
        when(httpResponseMock.getEntity())
                .thenReturn(httpEntityMock);
        when(httpClientMock.execute(any(HttpGet.class)))
                .then((Answer<CloseableHttpResponse>) httpGetMock -> {
                    new FileOutputStream(expectedJarFile).close();
                    return httpResponseMock;
                });
        when(httpEntityMock.getContent())
                .then((Answer<InputStream>) result -> new FileInputStream(expectedJarFile));

        File resultFile = httpHelper.downloadFile("https://example.com/folder/archive.zip", targetDirectory);

        assertThat(resultFile).isEqualTo(expectedJarFile);
    }

    @Test(expected = FailedToDownloadException.class)
    public void downloadFileThrowsExceptionOn404StatusCode() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();

        when(httpResponseMock.getStatusLine())
                .thenReturn(statusLine);
        when(httpResponseMock.getStatusLine().getStatusCode())
                .thenReturn(HttpStatus.SC_NOT_FOUND);
        when(httpClientMock.execute(any(HttpGet.class)))
                .thenReturn(httpResponseMock);

        httpHelper.downloadFile("https://example.com/archive.zip", targetDirectory);

        verify(httpResponseMock, never()).getEntity();
    }
}