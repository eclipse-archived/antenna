/*
 * Copyright (c) Bosch Software Innovations GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.main;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AntennaComplianceToolTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Ignore
    //is ignore because static method calls can neither be mocked nor verified and powermock is not desired in code.
    @Test
    public void testMainSucceedsWithExporter() {
        exit.expectSystemExitWithStatus(0);
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).getPath();
        String[] args = new String[]{"exporter", propertiesFilePath};
        AntennaComplianceTool.main(args);
    }

    @Test
    public void testMainFailsWithEmptyArgs() {
        exit.expectSystemExitWithStatus(1);
        AntennaComplianceTool.main(new String[]{});
    }

    @Test
    public void testMainFailsWithNonExistentComplianceToolArgs() throws IOException {
        exit.expectSystemExitWithStatus(1);
        File testFile = folder.newFile("test");
        AntennaComplianceTool.main(new String[]{"non-existent-option", testFile.getAbsolutePath()});
    }

    @Test
    public void testMainFailsWithNonExistentFilenameArgs() {
        exit.expectSystemExitWithStatus(1);
        AntennaComplianceTool.main(new String[]{"non-existent-option", "non-existent-file"});
    }
}
