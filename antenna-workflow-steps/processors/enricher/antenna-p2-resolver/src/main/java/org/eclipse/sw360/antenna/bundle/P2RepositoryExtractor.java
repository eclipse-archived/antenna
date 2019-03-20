/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.bundle;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.eclipse.sw360.antenna.bundle.OperatingSystemSpecifics.getProductNameForOS;

public final class P2RepositoryExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(P2RepositoryExtractor.class);

    private P2RepositoryExtractor() {
        // Utility class
    }

    public static void extractProductFromJar(String extractionLocation, String jarPath) throws IOException, AntennaException {
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().equals(getProductNameForOS())) {
                    extractZip(extractionLocation, jar, file);
                }
            }
        }
    }

    public static void extractProductFromFilesystem(String extractionLocation, String location) throws AntennaException {
        try {
            File zipFile = new File(location + File.separator + getProductNameForOS());
            unzipFile(zipFile, extractionLocation);
        } catch (IOException ex) {
            throw new AntennaException("Could not extract product from file system at " + location, ex);
        }
    }
    private static void extractZip(String extractionLocation, JarFile jar, JarEntry file) throws IOException {
        LOGGER.info("Extracting product to interact with p2 repositories: Starting");
        File zipFile = new File(extractionLocation + File.separator + file.getName());
        try (InputStream is = jar.getInputStream(file); FileOutputStream fos = new FileOutputStream(zipFile)) {
            while (is.available() > 0) {
                fos.write(is.read());
            }
        }
        unzipFile(zipFile, extractionLocation);
    }

    private static void unzipFile(File zipFile, String extractionLocation) throws IOException {
        try {
            LOGGER.info("Extracting zip file of product.");
            ZipFile zip = new ZipFile(zipFile);
            zip.extractAll(extractionLocation);
            LOGGER.info("Extracted product at: " + extractionLocation);
        } catch (ZipException e) {
            throw new IOException("Unzipping file failed: ", e);
        }
    }
}
