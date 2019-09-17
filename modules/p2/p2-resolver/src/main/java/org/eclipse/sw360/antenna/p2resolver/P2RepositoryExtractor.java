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

package org.eclipse.sw360.antenna.p2resolver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.util.ClassCodeSourceLocation;
import org.eclipse.sw360.antenna.util.ZipExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import static org.eclipse.sw360.antenna.p2resolver.OperatingSystemSpecifics.getProductNameForOS;

public final class P2RepositoryExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(P2RepositoryExtractor.class);

    private P2RepositoryExtractor() {
        // Utility class
    }

    public static void installEclipseProductForP2Resolution(String extractionLocation) throws ExecutionException {
        String location = "";
        try {
            location = ClassCodeSourceLocation.getClassCodeSourceLocationAsString(P2RepositoryExtractor.class);
        } catch (URISyntaxException e) {
            throw new ExecutionException("There was a problem parsing the class  code source location of " + P2RepositoryExtractor.class);
        }
        try (JarFile jar = new JarFile(location)) {
            P2RepositoryExtractor.extractProductFromJar(extractionLocation, location);
        } catch (IOException e) {
            LOGGER.warn("Jar could not be extracted. " +
                    "Trying to extract from filesystem, but this will only work in tests." +
                    "If this message pops up during normal execution, the installation of eclipse failed.");
            P2RepositoryExtractor.extractProductFromFilesystem(extractionLocation, location);
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public static void extractProductFromJar(String extractionLocation, String jarPath) throws ExecutionException {
        String extractedFolder = extractionLocation + File.separator + "extracted";
        new File(extractedFolder).mkdir();
        LOGGER.info("Extracting jar file containing products.");
        unzipFile(new File(jarPath), extractedFolder);
        LOGGER.info("Extracting jar file complete.");
        extractProductFromFilesystem(extractionLocation, extractedFolder);
    }

    public static void extractProductFromFilesystem(String extractionLocation, String location) throws ExecutionException {
        File zipFile = new File(location + File.separator + getProductNameForOS());
        LOGGER.info("Extracting zip file of product.");
        unzipFile(zipFile, extractionLocation);
        LOGGER.info("Extracted product at: " + extractionLocation);
    }

    private static void unzipFile(File zipFile, String extractionLocation) {
        try {
            ZipExtractor.extractAll(zipFile, new File(extractionLocation));
        } catch (IOException e) {
            throw new ExecutionException("Unzipping file " + zipFile.toString() + " failed. Reason: ", e);
        }
    }
}
