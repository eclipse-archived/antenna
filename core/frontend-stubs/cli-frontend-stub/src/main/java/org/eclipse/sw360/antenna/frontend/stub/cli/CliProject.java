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

import org.eclipse.sw360.antenna.frontend.Build;
import org.eclipse.sw360.antenna.frontend.MetaDataStoringProject;

import java.io.File;
import java.nio.file.Paths;

public class CliProject extends MetaDataStoringProject {
    private final File configFile;
    private final String buildDir;

    private String projectId = "project";
    private String projectVersion = "1.0";

    public CliProject(File configFile, String buildDir, String sourceDir) {
        this.configFile = configFile;
        this.buildDir = buildDir;
        this.build = new Build(buildDir,
                buildDir + File.separator + "classes",
                Paths.get(sourceDir, "main", "java").toString(),
                Paths.get(sourceDir, "test", "java").toString());
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

    @Override
    public void setVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public String getBuildDirectory() {
        return buildDir;
    }

    @Override
    public File getBasedir() {
        return configFile.getParentFile();
    }
}
