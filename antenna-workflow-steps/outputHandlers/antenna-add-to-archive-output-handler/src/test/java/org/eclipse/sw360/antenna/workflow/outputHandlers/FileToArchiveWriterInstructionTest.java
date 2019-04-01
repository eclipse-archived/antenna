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
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FileToArchiveWriterInstructionTest {

    @Test
    public void testConfiguringWithLinuxPaths() throws Exception {
        FileToArchiveWriterInstruction instruction = new FileToArchiveWriterInstruction(
                "disclosure-sw360-doc-txt:/my/path/target/example-project-1.0-SNAPSHOT.jar:/legalnotice/DisclosureDoc.txt",
                Mockito.mock(IProcessingReporter.class));

        assertThat(instruction.outputType).isEqualTo("disclosure-sw360-doc-txt");
        assertThat(instruction.pathInArchive).endsWithRaw(Paths.get("/legalnotice/DisclosureDoc.txt"));
        assertThat(instruction.zipFile).endsWithRaw(Paths.get("/my/path/target/example-project-1.0-SNAPSHOT.jar"));
    }

    @Test
    public void testConfiguringWithWindowsPaths() throws Exception {
        FileToArchiveWriterInstruction instruction = new FileToArchiveWriterInstruction(
                "disclosure-sw360-doc-txt:C:\\Users\\target\\example-project-1.0-SNAPSHOT.jar:/legalnotice/DisclosureDoc.txt",
                Mockito.mock(IProcessingReporter.class));

        assertThat(instruction.outputType).isEqualTo("disclosure-sw360-doc-txt");
        assertThat(instruction.pathInArchive).endsWithRaw(Paths.get("/legalnotice/DisclosureDoc.txt"));
        assertThat(instruction.zipFile).endsWithRaw(Paths.get("C:\\Users\\target\\example-project-1.0-SNAPSHOT.jar"));
    }
}
