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

import java.io.File;

public class OperatingSystemSpecifics {
    private static final String LAUNCHER_NAME = "eclipse";
    private static final String ANTENNA = "org.eclipse.sw360.antenna.p2.app.product";
    private static final String LINUX_X86_64 = ANTENNA + "-linux.gtk.x86_64.zip";
    private static final String WIN32_X86_64 = ANTENNA + "-win32.win32.x86_64.zip";
    private static final String MAC_X86_64 = ANTENNA + "-macosx.cocoa.x86_64.zip";
    private static final String MAC_PATH = "Eclipse.app" + File.separator + "Contents" + File.separator + "MacOS" + File.separator;

    public static String getProductNameForOS() throws AntennaException {
        if (isWindows()) {
            return WIN32_X86_64;
        } else if (isLinux()) {
            return LINUX_X86_64;
        } else if (isMac()) {
            return MAC_X86_64;
        }
        throw new AntennaException("Operating system not supported for workflow step P2 Resolver");
    }

    public static File prepareEclipseExecutable(File productInstallationArea) throws AntennaException {
        File eclipse_executable = getEclipseExecutable(productInstallationArea);
        if (isLinux() || isMac()) {
            eclipse_executable.setExecutable(true);
            return eclipse_executable;
        }
        return eclipse_executable;
    }

    public static File getEclipseExecutable(File productInstallationArea) throws AntennaException {
        if (isWindows()) {
            return productInstallationArea.toPath().resolve(LAUNCHER_NAME + ".exe").normalize().toFile();
        } else if (isLinux()) {
            return productInstallationArea.toPath().resolve(LAUNCHER_NAME).normalize().toFile();
        } else if (isMac()) {
            return productInstallationArea.toPath().resolve(MAC_PATH + File.separator + LAUNCHER_NAME).normalize().toFile();
        }
        throw new AntennaException("Operating system not supported for workflow step P2 Resolver");
    }

    public static boolean isWindows() {
        String operatingSystem = getOperatingSystem();
        return operatingSystem.contains("win");
    }

    public static boolean isMac() {
        String operatingSystem = getOperatingSystem();
        return operatingSystem.contains("mac");
    }

    public static boolean isLinux() {
        String operatingSystem = getOperatingSystem();
        return operatingSystem.contains("nix") || operatingSystem.contains("nux");
    }

    public static String getOperatingSystem() {
        return System.getProperty("os.name").toLowerCase();
    }
}
