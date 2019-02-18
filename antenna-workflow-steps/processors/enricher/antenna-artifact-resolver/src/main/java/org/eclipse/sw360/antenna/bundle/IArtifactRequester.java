/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.bundle;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public abstract class IArtifactRequester {

    public static final String JAR_EXTENSION = ".jar";
    public static final String SOURCES_JAR_EXTENSION = "-sources"+JAR_EXTENSION;
    protected AntennaContext context;

    public IArtifactRequester(AntennaContext context) {
        this.context = context;
    }

    /**
     * Requests a jar file from a repository.
     *
     * @param coordinates
     *            Identifies the artifact for which the jar is requested.
     * @param targetDirectory
     *            Where the jar file will be stored.
     * @param isSource
     *            Whether the request should retrieve the sources.
     * @return The jar file, or null if the file couldn't be obtained.
     * @throws IOException
     *             If a problem occurs putting the file in place
     * @throws MavenArtifactDoesNotExistException
     *             If the artifact cannot be found in the repository.
     */
    public abstract File requestFile(MavenCoordinates coordinates, Path targetDirectory, boolean isSource)
            throws IOException, MavenArtifactDoesNotExistException;

    String getExpectedJarBaseName(MavenCoordinates coordinates, boolean isSource){
        String jarBaseName = coordinates.getArtifactId() + "-" + coordinates.getVersion();
        if (isSource) {
            jarBaseName = jarBaseName.concat(SOURCES_JAR_EXTENSION);
        } else {
            jarBaseName = jarBaseName.concat(JAR_EXTENSION);
        }
        return jarBaseName;
    }

}
