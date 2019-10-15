/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven;

import org.eclipse.sw360.antenna.model.coordinates.Coordinate;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;


public abstract class IArtifactRequester {

    public static final String JAR_EXTENSION = ".jar";

    public IArtifactRequester() {
    }

    /**
     * Requests a jar file from a repository.
     *
     * @param mavenCoordinate       Identifies the artifact for which the jar is requested.
     * @param targetDirectory       Where the jar file will be stored.
     * @param classifierInformation Information on the classifier (source or not and name).
     * @return The jar file, or null if the file couldn't be obtained.
     */
    public abstract Optional<File> requestFile(Coordinate mavenCoordinate, Path targetDirectory, ClassifierInformation classifierInformation);

    String getExpectedJarBaseName(Coordinate mavenCoordinate, ClassifierInformation classifierInformation) {
        return mavenCoordinate.getName() + "-" + mavenCoordinate.getVersion()
                + (classifierInformation.classifier.isEmpty() ? JAR_EXTENSION : "-" + classifierInformation.classifier + JAR_EXTENSION);
    }

}
