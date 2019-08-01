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
import org.eclipse.sw360.antenna.frontend.MetaDataStoringProject;
import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIFrontend;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class AntennaImpl {
    private final AbstractAntennaCLIFrontend antennaCLIFrontend;
    private final File pomFilePath;

    public AntennaImpl(String pluginDescendantArtifactIdName, Path pomFilePath, Project gradleProject) {
        this(pluginDescendantArtifactIdName, pomFilePath, gradleProject, null);
    }

    public AntennaImpl(String pluginDescendantArtifactIdName, Path pomFilePath, Project gradleProject, Path propertiesFilePath) {
        this.pomFilePath = pomFilePath.toFile();
        final WrappedGradleProject wrappedGradleProject = getWrappedGradleProject(gradleProject, propertiesFilePath);

        antennaCLIFrontend = new AbstractAntennaCLIFrontend(this.pomFilePath) {
            @Override
            protected String getPluginDescendantArtifactIdName() {
                return pluginDescendantArtifactIdName;
            }

            @Override
            protected Path getBuildDirFromFolder(Path folder) {
                return folder.resolve("build");
            }

            @Override
            public MetaDataStoringProject getProject() {
                return wrappedGradleProject;
            }
        };
    }

    public void execute() throws AntennaException {
        antennaCLIFrontend.execute();
    }

    private WrappedGradleProject getWrappedGradleProject(Project gradleProject, Path propertiesFilePath) {
        if (!this.pomFilePath.exists()) {
            throw new IllegalArgumentException("Cannot find " + this.pomFilePath.toString());
        }

        if (propertiesFilePath != null) {
            if(!Files.exists(propertiesFilePath)) {
                throw new IllegalArgumentException("Cannot find " + propertiesFilePath.toString());
            } else {
                return new WrappedGradleProject(gradleProject, pomFilePath, propertiesFilePath.toFile());
            }
        } else {
            return new WrappedGradleProject(gradleProject, pomFilePath);
        }
    }
}
