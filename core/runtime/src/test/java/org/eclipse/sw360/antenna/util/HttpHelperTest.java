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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.eclipse.sw360.antenna.http.HttpClientFactory;
import org.eclipse.sw360.antenna.http.HttpClientFactoryImpl;
import org.eclipse.sw360.antenna.http.config.HttpClientConfig;
import org.eclipse.sw360.antenna.http.utils.HttpConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HttpHelperTest {
    private static final String TEST_CONTENT = "Content of the test file to be downloaded.";
    private static final String FILE_NAME = "archive.zip";
    private static final String FILE_PATH = "/test/downloads";
    private static final String FILE_REQUEST = FILE_PATH + "/" + FILE_NAME;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private HttpHelper httpHelper;

    @Before
    public void setUp() {
        HttpClientFactory clientFactory = new HttpClientFactoryImpl();
        httpHelper = new HttpHelper(clientFactory.newHttpClient(HttpClientConfig.basicConfig()));
    }

    /**
     * Checks whether the correct file has been downloaded.
     *
     * @param targetDirectory the target directory for downloads
     * @param resultFile      the file returned by the helper
     */
    private static void checkDownloadedFile(Path targetDirectory, Path resultFile) throws IOException {
        assertThat(resultFile.getParent()).isEqualTo(targetDirectory);
        assertThat(resultFile.getFileName().toString()).isEqualTo(FILE_NAME);
        byte[] bytes = Files.readAllBytes(resultFile);
        assertThat(bytes).isEqualTo(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void downloadFileWritesTheFileToDisk() throws Exception {
        wireMockRule.stubFor(get(urlPathEqualTo(FILE_REQUEST))
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_OK)
                        .withBody(TEST_CONTENT)));

        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

        Path resultFile = httpHelper.downloadFile(wireMockRule.url(FILE_REQUEST), targetDirectory).toPath();

        checkDownloadedFile(targetDirectory, resultFile);
    }

    @Test
    public void downloadFileNameCanBeOverwritten() throws IOException {
        final String downloadUri = "/foo/bar.txt";
        wireMockRule.stubFor(get(urlPathEqualTo(downloadUri))
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_OK)
                        .withBody(TEST_CONTENT)));

        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

        Path resultFile =
                httpHelper.downloadFile(wireMockRule.url(downloadUri), targetDirectory, FILE_NAME).toPath();

        checkDownloadedFile(targetDirectory, resultFile);
    }

    @Test(expected = IOException.class)
    public void downloadFileThrowsExceptionOn404StatusCode() throws Exception {
        wireMockRule.stubFor(get(anyUrl())
                .willReturn(aResponse().withStatus(HttpConstants.STATUS_ERR_NOT_FOUND)));
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

        httpHelper.downloadFile(wireMockRule.url("/test"), targetDirectory);
    }
}
