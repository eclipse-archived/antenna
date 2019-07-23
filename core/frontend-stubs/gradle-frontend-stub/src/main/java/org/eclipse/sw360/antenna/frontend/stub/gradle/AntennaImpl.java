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

    private File propertiesFilePath;

    public AntennaImpl(String pluginDescendantArtifactIdName, Path pomFilePath) {
        this(pluginDescendantArtifactIdName, pomFilePath, null);
    }

    public AntennaImpl(String pluginDescendantArtifactIdName, Path pomFilePath, Path propertiesFilePath) {
        this.pomFilePath = pomFilePath.toFile();

        if (!this.pomFilePath.exists()) {
            throw new IllegalArgumentException("Cannot find " + pomFilePath.toString());
        }

        if (propertiesFilePath != null) {
            this.propertiesFilePath = propertiesFilePath.toFile();
            if(!this.propertiesFilePath.exists()) {
                throw new IllegalArgumentException("Cannot find " + propertiesFilePath.toString());
            }
        } else {
            this.propertiesFilePath = null;
        }

        antennaCLIFrontend = new AbstractAntennaCLIFrontend(this.pomFilePath, this.propertiesFilePath) {
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
        antennaCLIFrontend.execute();
    }
}
