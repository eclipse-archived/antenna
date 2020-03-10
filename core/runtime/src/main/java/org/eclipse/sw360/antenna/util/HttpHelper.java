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

import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.http.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class HttpHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    private final HttpClient httpClient;

    /**
     * Creates a new instance of {@code HttpHelper} that uses the given client
     * for HTTP requests.
     *
     * @param httpClient the {@code HttpClient} to be used
     */
    public HttpHelper(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public File downloadFile(String url, Path targetDirectory) throws IOException {
        String filename = url.substring(url.lastIndexOf("/") + 1);  // We don't want to have the last slash in the name
        return downloadFile(url, targetDirectory, filename);
    }

    public File downloadFile(String url, Path targetDirectory, String filename) throws IOException {
        LOGGER.debug("Downloading from URL {} to file {} in {}.", url, filename, targetDirectory);
        Path targetFile = targetDirectory.resolve(filename);

        return HttpUtils.waitFor(httpClient.execute(HttpUtils.get(url),
                HttpUtils.checkResponse(response -> {
                    Files.createDirectories(targetDirectory);
                    Files.copy(response.bodyStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return targetFile.toFile();
                })));
    }
}
