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
package org.eclipse.sw360.antenna.frontend.stub.gradle;

import org.eclipse.sw360.antenna.frontend.MetaDataStoringProject;
import org.eclipse.sw360.antenna.frontend.Build;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class WrappedGradleProject extends MetaDataStoringProject {

    private final Project innerProject;
    private final Build build;
    private String projectId;
    private final File pomFile;

    public WrappedGradleProject(Project innerProject, File pomFile) {
        this(innerProject, pomFile, null);
    }

    public WrappedGradleProject(Project innerProject, File pomFile, File propertiesFile) {
        this.innerProject = innerProject;
        this.pomFile = pomFile;
        this.propertiesFile = propertiesFile;

        Path srcDir = innerProject.getRootDir()
                .toPath()
                .resolve("src")
                .toAbsolutePath();
        Path buildDir = innerProject.getBuildDir().toPath().toAbsolutePath();

        this.build = new Build(buildDir.toString(),
                buildDir.resolve("classes").toString(),
                srcDir.resolve("main").resolve("java").toString(),
                srcDir.resolve("test").resolve("java").toString());
        this.projectId = innerProject.getName();
    }

    @Override
    public Object getRawProject() {
        return innerProject;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public String getVersion() {
        return Optional.ofNullable(innerProject.getVersion())
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(v -> !v.isEmpty() && !"unspecified".equals(v))
                .orElse("1.0-SNAPSHOT");
    }

    @Override
    public File getConfigFile() {
        return pomFile;
    }

    @Override
    public String getBuildDirectory() {
        return innerProject.getBuildDir().getAbsolutePath();
    }

    @Override
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setVersion(String projectVersion) {
        innerProject.setVersion(projectVersion);
    }

    @Override
    public Build getBuild() {
        return build;
    }
}
