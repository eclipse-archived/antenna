/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
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
import org.eclipse.sw360.antenna.http.config.ProxySettings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.setVariableValueInObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HttpHelperTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
        httpHelper = new HttpHelper(ProxySettings.noProxy());
        setVariableValueInObject(httpHelper, "httpClient", httpClientMock);
    }

    @Test
    public void downloadFileWritesTheFileToDisk() throws Exception {
        File sourceDirectory = temporaryFolder.newFolder("source");
        File targetDirectory = temporaryFolder.newFolder("target");
        File expectedJarFile = new File(sourceDirectory, "archive.zip");

        when(httpResponseMock.getStatusLine())
                .thenReturn(statusLine);
        when(httpResponseMock.getStatusLine().getStatusCode())
                .thenReturn(HttpStatus.SC_OK);
        when(httpResponseMock.getEntity())
                .thenReturn(httpEntityMock);
        when(httpClientMock.execute(any(HttpGet.class)))
                .then((Answer<CloseableHttpResponse>) httpGetMock -> {
                    Files.write(expectedJarFile.toPath(), "dummy content".getBytes(StandardCharsets.UTF_8));
                    return httpResponseMock;
                });
        when(httpEntityMock.getContent())
                .then((Answer<InputStream>) result -> new FileInputStream(expectedJarFile));

        File resultFile = httpHelper.downloadFile("https://example.com/folder/archive.zip", targetDirectory.toPath());

        assertThat(resultFile).hasSameContentAs(expectedJarFile);
    }

    @Test(expected = IOException.class)
    public void downloadFileThrowsExceptionOn404StatusCode() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

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