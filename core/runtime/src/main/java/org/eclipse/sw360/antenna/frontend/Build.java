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
package org.eclipse.sw360.antenna.frontend;

public class Build {
    private final String directory;
    private final String outputDirectory;
    private final String sourceDirectory;
    private final String testSourceDirectory;

    public Build(String buildDirectory, String outputDirectory, String sourceDirectory, String testSourceDirectory) {
        this.directory = buildDirectory;
        this.outputDirectory = outputDirectory;
        this.sourceDirectory = sourceDirectory;
        this.testSourceDirectory = testSourceDirectory;
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
