/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Copys all files from the given path to the specified target directory in the
 * folder filesToScan.
 */
public final class FileAssembler {

    /**
     * Copies files identified through the list filePaths to the directory
     * clmFilePath.
     *
     * @param clmFilePath
     *            Directory, where all files for the clm scan are stored.
     * @param buildDirectory
     *            Current build directory.
     * @param filePaths
     *            Paths to files that will be copied.
     */
    public static void copyFiles(Path clmFilePath, Path buildDirectory, Collection<File> filePaths) throws IOException {
        Files.createDirectories(clmFilePath);

        for (File path : filePaths) {
            copyFile(clmFilePath.toString(), buildDirectory.toString(), path);
        }

    }

    private static void copyFile(String clmFiles, String buildDirectory, File path) {
        File fileToCopy = path;
        if (!fileToCopy.isDirectory()) {
            File targetFile = prepareFilesForWriting(clmFiles, buildDirectory, fileToCopy);
            try {
                writeToFile(fileToCopy, targetFile);
            } catch (IOException e) {
                throw new RuntimeException(
                        "An Exception occurred while copying the files to the directory of antenna: " + e.getMessage(), e);
            }
        } else {
            File[] listFiles = fileToCopy.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    copyFile(clmFiles, buildDirectory, file);
                }
            }
        }
    }

    /**
     * Sets the correct paths for the files.
     *
     * @param targetDirectory
     * @param buildDirectory
     *            Parent directory, where the files are written to.
     * @param fileToCopy
     * @return
     */
    private static File prepareFilesForWriting(String targetDirectory, String buildDirectory, File fileToCopy) {
        String newFile = fileToCopy.getPath().replace(buildDirectory, "");
        File targetFile = new File(targetDirectory, newFile);
        File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        return targetFile;
    }

    private static void writeToFile(File fileToCopy, File targetFile) throws FileNotFoundException, IOException {
        InputStream in;
        BufferedOutputStream bOut;
        in = new FileInputStream(fileToCopy);
        OutputStream out = new FileOutputStream(targetFile);
        bOut = new BufferedOutputStream(out);
        byte[] buffer = new byte[1024 * 4];
        for (int read = in.read(buffer); -1 != read; read = in.read(buffer)) {
            bOut.write(buffer, 0, read);
        }
        bOut.flush();
        bOut.close();
        in.close();
    }

    private FileAssembler() {
        // util
    }
}
