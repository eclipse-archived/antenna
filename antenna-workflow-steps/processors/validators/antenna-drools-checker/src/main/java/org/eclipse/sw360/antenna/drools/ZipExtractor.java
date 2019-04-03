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

package org.eclipse.sw360.antenna.drools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {
    public static void extractAll(Path zipFile, File destination) throws IOException {
        try (FileInputStream fs = new FileInputStream(zipFile.toString());
             ZipInputStream zipInputStream = new ZipInputStream(fs)) {

            byte[] buffer = new byte[1024];
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                extractEntry(destination.toPath().normalize().toFile(), zipInputStream, buffer, zipEntry);
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private static void extractEntry(File destination, ZipInputStream zipInputStream, byte[] buffer, ZipEntry zipEntry) throws IOException {
        File newFile = newFile(destination, zipEntry);
        if (newFile.isDirectory()) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    private static File newFile(File destinationDirectory, ZipEntry zipEntry) throws IOException {
        File newFile = new File(destinationDirectory, zipEntry.getName());
        createFile(zipEntry, newFile);

        if (!newFile.toPath().normalize().startsWith(destinationDirectory.toPath())) {
            throw new IOException("Entry " + newFile.getPath() + " lies outside of temporary target directory: " + destinationDirectory.getPath() + ". It would not be properly deleted.");
        }

        return newFile;
    }

    private static void createFile(ZipEntry zipEntry, File newFile) throws IOException {
        if (zipEntry.isDirectory()) {
            newFile.mkdirs();
        } else {
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();
        }
    }
}
