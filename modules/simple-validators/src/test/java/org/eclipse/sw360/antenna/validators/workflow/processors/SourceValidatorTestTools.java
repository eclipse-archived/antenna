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
package org.eclipse.sw360.antenna.validators.workflow.processors;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SourceValidatorTestTools {

    private final static String jarName = "SourceValidatorTestTools.jar";
    private final static String sourceJarName = "SourceValidatorTestTools-sources.jar";
    private final static String classExtension = ".class";
    private final static String sourceExtension = ".java";
    private final static int size = 1000;
    private final Path tmpdir;

    public SourceValidatorTestTools(Path tmpdir) {
        this.tmpdir = tmpdir;
    }

    public File writeJar() throws IOException {
        File jar = tmpdir.resolve(jarName).toFile();
        write(jar, i -> true, classExtension);
        return jar;
    }

    public File writeSourceJar(int percentage) throws IOException {
        File jar = tmpdir.resolve(sourceJarName).toFile();
        write(jar, i -> (i % 100 < percentage), sourceExtension);
        return jar;
    }

    private void write(File filename, IntPredicate filter, String fileExtension) throws IOException {
        try ( FileOutputStream fos = new FileOutputStream(filename);
              ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos)) ) {
            IntStream.rangeClosed(1, size)
                    .filter(filter)
                    .forEach(i -> {
                                String entryname = "file-" + i + fileExtension;
                                ZipEntry ze = new ZipEntry(entryname);
                                try {
                                    zos.putNextEntry(ze);
                                    zos.closeEntry();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
        }
    }
}
