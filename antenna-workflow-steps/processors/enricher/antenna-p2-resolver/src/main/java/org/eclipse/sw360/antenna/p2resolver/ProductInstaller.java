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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;

import java.io.IOException;
import java.util.jar.JarFile;

/**
 * This class abstracts installation of the product needed to run the artifact resolver.
 * <p>
 * Currently, the product is stored in a zip within the antenna-p2-resolver jar. This class extracts
 * the zip-File to a temporary folder or from the folder if we are not using a jar (e.g. for testing.
 * <p>
 * It must be a real class because it obtains the location of the jar from the location in class path
 */
public class ProductInstaller {

    public static ProductInstaller create() {
        return new ProductInstaller();
    }

    public void installEclipseProductForP2Resolution(String extractionLocation) throws AntennaException {
        String location = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try (JarFile jar = new JarFile(location)) {
            P2RepositoryExtractor.extractProductFromJar(extractionLocation, location);
        } catch (IOException e) {
            P2RepositoryExtractor.extractProductFromFilesystem(extractionLocation, location);
        }
    }
}
