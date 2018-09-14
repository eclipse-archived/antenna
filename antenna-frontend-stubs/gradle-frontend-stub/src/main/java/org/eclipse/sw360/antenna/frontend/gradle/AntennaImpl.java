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
package org.eclipse.sw360.antenna.frontend.gradle;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.frontend.cli.AbstractAntennaCLIFrontend;

import java.io.File;
import java.nio.file.Path;

public class AntennaImpl {

    private static class GradleAntennaFrontend extends AbstractAntennaCLIFrontend {
        public GradleAntennaFrontend(File pomFile) {
            super(pomFile);
        }

        @Override
        protected Path getBuildDirFromFolder(Path folder) {
            return folder.resolve("build");
        }
    }

    private final Path pomFilePath;

    public AntennaImpl(Path pomFilePath) {
        this.pomFilePath = pomFilePath;
    }

    public void execute() throws AntennaException {

        if (!pomFilePath.toFile().exists()) {
            throw new IllegalArgumentException("Cannot find " + pomFilePath.toString());
        }

        GradleAntennaFrontend antennaCLIFrontend = new GradleAntennaFrontend(pomFilePath.toFile());
        antennaCLIFrontend.execute();
    }
}
