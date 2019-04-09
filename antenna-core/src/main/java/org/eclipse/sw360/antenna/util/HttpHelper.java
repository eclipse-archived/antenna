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
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.exceptions.FailedToDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class HttpHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);
    private CloseableHttpClient httpClient;

    public HttpHelper(AntennaContext context) {
        httpClient = getConfiguredHttpClient(context);
    }

    public File downloadFile(String url, Path targetDirectory) throws IOException, FailedToDownloadException {
        String filename = url.substring(url.lastIndexOf("/") + 1);  // We don't want to have the last slash in the name
        return downloadFile(url, targetDirectory, filename);
    }

    public File downloadFile(String url, Path targetDirectory, String filename) throws IOException, FailedToDownloadException {
        File targetFile = targetDirectory.resolve(filename).toFile();

        try (CloseableHttpResponse response = getFromUrl(url)) {
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new FailedToDownloadException("File not found on URL=["+url+"]");
            } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Reason: " + response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream is = entity.getContent()) {
                    FileUtils.copyInputStreamToFile(is, targetFile);
                }
            }
        } catch (IOException e) {
            throw new IOException("Encountered a problem while downloading the file " + url + ": " + e.getMessage());
        }

        if (!targetFile.exists()) {
            throw new FailedToDownloadException("File does not exist after downloading.");
        }
        return targetFile;
    }

    private CloseableHttpClient getConfiguredHttpClient(AntennaContext context) {
        ToolConfiguration tc = context.getToolConfiguration();

        if (tc.useProxy()) {
            LOGGER.info("Using proxy on host {} and port {}", tc.getProxyHost(), tc.getProxyPort());
            return HttpClients.custom()
                    .useSystemProperties()
                    .setProxy(new HttpHost(tc.getProxyHost(), tc.getProxyPort()))
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
