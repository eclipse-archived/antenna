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

    public static void extractProductFromJar(String extractionLocation, String jarPath) throws AntennaException {
        String extractedFolder = extractionLocation + File.separator + "extracted";
        new File(extractedFolder).mkdir();
        LOGGER.info("Extracting jar file containing products.");
        unzipFile(new File(jarPath), extractedFolder);
        LOGGER.info("Extracting jar file complete.");
        extractProductFromFilesystem(extractionLocation, extractedFolder);
    }

    public static void extractProductFromFilesystem(String extractionLocation, String location) throws AntennaException {
        File zipFile = new File(location + File.separator + getProductNameForOS());
        LOGGER.info("Extracting zip file of product.");
        unzipFile(zipFile, extractionLocation);
        LOGGER.info("Extracted product at: " + extractionLocation);
    }

    private static void unzipFile(File zipFile, String extractionLocation) throws AntennaException {
        try {
            ZipFile zip = new ZipFile(zipFile);
            zip.extractAll(extractionLocation);
        } catch (ZipException e) {
            throw new AntennaException("Unzipping file " + zipFile.toString() + " failed. Reason: ", e);
        }
    }
}
