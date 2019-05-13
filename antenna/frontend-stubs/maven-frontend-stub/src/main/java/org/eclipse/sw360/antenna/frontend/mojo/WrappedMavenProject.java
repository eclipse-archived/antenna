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
package org.eclipse.sw360.antenna.frontend.mojo;

import org.apache.maven.project.MavenProject;
import org.eclipse.sw360.antenna.api.IProject;

import java.io.File;

public class WrappedMavenProject implements IProject {

    private final MavenProject innerProject;

    public WrappedMavenProject(MavenProject innerProject) {
        this.innerProject = innerProject;
    }

    @Override
    public Object getRawProject(){
        return innerProject;
    }

    @Override
    public String getProjectId() {
        return innerProject.getArtifactId();
    }

    @Override
    public String getVersion() {
        return innerProject.getVersion();
    }

    @Override
    public String getBuildDirectory() {
        return innerProject.getBuild().getOutputDirectory();
    }

    @Override
    public File getConfigFile() {
        return innerProject.getFile();
    }
}
