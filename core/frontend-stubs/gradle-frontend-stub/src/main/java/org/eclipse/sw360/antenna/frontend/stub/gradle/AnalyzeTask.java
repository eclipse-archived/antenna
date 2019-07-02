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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AnalyzeTask extends DefaultTask {

    protected abstract String getPluginDescendantArtifactIdName();

    @TaskAction
    public void analyze() throws Exception {

        AntennaExtension extension = getProject().getExtensions().findByType(AntennaExtension.class);
        if (extension == null) {
            extension = new AntennaExtension();
        }

        Path pomPath = Paths.get(System.getProperty("user.dir")).resolve(extension.getPomPath()).toAbsolutePath();

        AntennaImpl osmRunner;

        if(extension.getPropertiesFilePath() != null) {
            Path propertiesFilePath = Paths.get(System.getProperty("user.dir")).resolve(extension.getPropertiesFilePath()).toAbsolutePath();
            osmRunner = new AntennaImpl(getPluginDescendantArtifactIdName(), pomPath, propertiesFilePath);
        } else {
            osmRunner = new AntennaImpl(getPluginDescendantArtifactIdName(), pomPath);
        }

        osmRunner.execute();
    }

}
