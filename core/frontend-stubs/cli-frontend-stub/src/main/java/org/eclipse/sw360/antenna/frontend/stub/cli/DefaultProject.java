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
package org.eclipse.sw360.antenna.frontend.stub.cli;

import org.eclipse.sw360.antenna.api.IProject;

import java.io.File;
import java.nio.file.Paths;

public class DefaultProject implements IProject {
    private final File configFile;
    private final String baseUri;
    private final String buildDir;
    private final Build build;

    private String projectId = "project";
    private String projectVersion = "1.0";
    private String artifactId = projectId + ":" + projectVersion;

    private File propertiesFile;

    public DefaultProject(File configFile, String buildDir, String sourceDir) {
        this.configFile = configFile;
        this.buildDir = buildDir;
        this.baseUri = configFile.toPath().getParent().toUri().toString();
        this.build = new Build(buildDir, sourceDir);
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

    @Override
    public File getBasedir() {
        return configFile.getParentFile();
    }

    public String getBaseUri() {
        return baseUri;
    }

    public File getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public class Build {
        private final String directory;
        private final String outputDirectory;
        private final String sourceDirectory;
        private final String testSourceDirectory;

        public Build(String buildDirectory, String sourceDirectory) {
            this.directory = buildDirectory;
            this.outputDirectory = buildDirectory + File.separator + "classes";
            this.sourceDirectory = Paths.get(sourceDirectory, "main", "java").toString();
            this.testSourceDirectory = Paths.get(sourceDirectory, "test", "java").toString();
        }

        public String getDirectory() {
            return directory;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public String getSourceDirectory() {
            return sourceDirectory;
        }

        public String getTestSourceDirectory() {
            return testSourceDirectory;
        }
    }
}
