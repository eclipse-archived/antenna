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
package org.eclipse.sw360.antenna.frontend.cli;

import org.eclipse.sw360.antenna.api.IProject;

import java.io.File;

public class DefaultProject implements IProject {
    private final File configFile;
    private final String buildDir;
    private final Build build;

    private String projectId = "project";
    private String projectVersion = "1.0";
    private String artifactId = projectId + ":" + projectVersion;

    public DefaultProject(File configFile, String buildDir) {
        this.configFile = configFile;
        this.buildDir = buildDir;
        this.build = new Build(buildDir, buildDir + "/classes");
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getVersion() {
        return projectVersion;
    }

    public void setVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public String getBuildDirectory() {
        return buildDir;
    }

    public Build getBuild() {
        return build;
    }

    public class Build {
        private final String directory;
        private final String outputDirectory;

        public Build(String directory, String outputDirectory) {
            this.directory = directory;
            this.outputDirectory = outputDirectory;
        }

        public String getDirectory() {
            return directory;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

    }
}
