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
package org.eclipse.sw360.antenna.frontend.stub.gradle;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIFrontend;

import java.io.File;
import java.nio.file.Path;

public class AntennaImpl {
    private final AbstractAntennaCLIFrontend antennaCLIFrontend;
    private final File pomFilePath;

    public AntennaImpl(String pluginDescendantArtifactIdName, Path pomFilePath) {
        this.pomFilePath = pomFilePath.toFile();
        antennaCLIFrontend = new AbstractAntennaCLIFrontend(this.pomFilePath) {
            @Override
            protected String getPluginDescendantArtifactIdName() {
                return pluginDescendantArtifactIdName;
            }

            @Override
            protected Path getBuildDirFromFolder(Path folder) {
                return folder.resolve("build");
            }
        };

    }

    public void execute() throws AntennaException {

        if (!pomFilePath.exists()) {
            throw new IllegalArgumentException("Cannot find " + pomFilePath.toString());
        }

        antennaCLIFrontend.execute();
    }
}
