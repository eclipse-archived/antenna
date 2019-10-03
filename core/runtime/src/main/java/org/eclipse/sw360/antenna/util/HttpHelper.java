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

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class HttpHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);
    private CloseableHttpClient httpClient;

    public HttpHelper(ProxySettings proxySettings) {
        httpClient = getConfiguredHttpClient(proxySettings);
    }

    public File downloadFile(String url, Path targetDirectory) throws IOException {
        String filename = url.substring(url.lastIndexOf("/") + 1);  // We don't want to have the last slash in the name
        return downloadFile(url, targetDirectory, filename);
    }

    public File downloadFile(String url, Path targetDirectory, String filename) throws IOException {
        File targetFile = targetDirectory.resolve(filename).toFile();

        try (CloseableHttpResponse response = getFromUrl(url)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new IOException("File not found on URL=[" + url + "]");
            } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Reason: " + response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream is = entity.getContent()) {
                    FileUtils.copyInputStreamToFile(is, targetFile);
                }
            }
        }

        if (!targetFile.exists()) {
            throw new IOException("File " + targetFile.getAbsolutePath() + "does not exist after downloading.");
        }
        return targetFile;
    }

    private CloseableHttpClient getConfiguredHttpClient(ProxySettings proxySettings) {
        if (proxySettings.isProxyUse()) {
            LOGGER.info("Using proxy on host {} and port {}", proxySettings.getProxyHost(), proxySettings.getProxyPort());
            return HttpClients.custom()
                    .useSystemProperties()
                    .setProxy(new HttpHost(proxySettings.getProxyHost(), proxySettings.getProxyPort()))
                    .build();
        } else {
            return HttpClients.createDefault();
        }
    }

    private CloseableHttpResponse getFromUrl(String url) throws IOException {
        try {
            HttpGet getRequest = new HttpGet(url);
            LOGGER.info("Downloading {}", url);
            return httpClient.execute(getRequest);
        } catch (IOException e) {
            throw new IOException("Error while downloading " + url + ": " + e.getMessage());
        }
    }
}
