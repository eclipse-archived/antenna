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

package org.eclipse.sw360.antenna.jsonreader;

import org.apache.commons.io.output.NullOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the inputStream an returns the bytes which are read, and writes the
 * bytes to the file at the recordingFilePath.
 */
public class RecordingInputStream extends InputStream {

    private InputStream target;
    private OutputStream output;

    /**
     *
     * @param target
     *            InputStream which will be read.
     * @param recordingFilePath
     *            Path to the file to which the content of the InputStream is
     *            written.
     * @throws IOException
     */
    public RecordingInputStream(InputStream target, Path recordingFilePath) throws IOException {
        this.target = target;
        if (recordingFilePath == null) {
            this.output = new NullOutputStream();
        } else {
            File recordingFile = recordingFilePath.toFile();
            Path parent = recordingFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            this.output = new FileOutputStream(recordingFile);
        }
    }

    /**
     * Reads the InputStream and writes the content to the specified file.
     *
     * @return Byte that are return from the read Method of the InputStream.
     */
    @Override
    public int read() throws IOException {
        int readByte = -1;
        try {
            readByte = target.read();

            if (readByte != -1) {
                output.write(readByte);
            }
        } finally {
            if (readByte == -1) {
                output.close();
            }
        }
        return readByte;
    }

    @Override
    public int available() throws IOException {
        return target.available();
    }

    @Override
    public void close() throws IOException {
        target.close();
    }

    @SuppressWarnings("sync-override")
    @Override
    public void mark(int arg0) {
        target.mark(arg0);
    }

    @Override
    public boolean markSupported() {
        return target.markSupported();
    }

    @SuppressWarnings("sync-override")
    @Override
    public void reset() throws IOException {
        target.reset();
    }

    @Override
    public long skip(long arg0) throws IOException {
        return target.skip(arg0);
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
