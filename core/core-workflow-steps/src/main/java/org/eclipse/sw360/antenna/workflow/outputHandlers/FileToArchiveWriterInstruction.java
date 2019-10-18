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

package org.eclipse.sw360.antenna.workflow.outputHandlers;


import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.reporting.MessageType;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileToArchiveWriterInstruction {
    public final Path zipFile;
    public final String outputType;
    public final Path pathInArchive;

    public FileToArchiveWriterInstruction(String instruction, IProcessingReporter reporter) {
        if (!instruction.contains(":")) {
            throwConfigurationError(instruction, "invalid due to missing \":\"", reporter);
        }

        if (!instruction.contains("jar:") && !instruction.contains("ear:") && !instruction.contains("war:") && !instruction.contains("zip:")) {
            throwConfigurationError(instruction, "does not contain a path-in-jar part", reporter);
        }

        String[] newParts = instruction.substring(0, instruction.lastIndexOf(':')).split(":", 2);
        if (newParts.length != 2) {
            throwConfigurationError(instruction, "does not contain a type or path", reporter);
        }

        pathInArchive = Paths.get(instruction.substring(instruction.lastIndexOf(':') + 1));
        zipFile = Paths.get(newParts[1]);
        outputType = newParts[0];
    }

    private void throwConfigurationError(String instruction, String s, IProcessingReporter reporter) {
        String msg = "Unable to attach File related to instruction=[" + instruction + "], " + s;
        reporter.add(MessageType.PROCESSING_FAILURE, msg);
        throw new ConfigurationException(msg);
    }
}
