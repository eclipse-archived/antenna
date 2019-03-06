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

package org.eclipse.sw360.antenna.p2;

import org.eclipse.equinox.p2.metadata.Version;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class P2Artifact {
    private final String bundleCoordinates;
    private final Version version;
    private Optional<Path> jarPath = Optional.empty();
    private Optional<Path> sourcePath = Optional.empty();

    public P2Artifact(String bundleCoordinates, Version version) {
        this.bundleCoordinates = bundleCoordinates;
        this.version = version;
    }

    public String getBundleSymbolicName() {
        return bundleCoordinates;
    }

    public Version getVersion() {
        return version;
    }

    public Optional<Path> getJarPath() {
        return jarPath;
    }

    public void setJarPath(Path jarPath) {
        this.jarPath = Optional.of(jarPath);
    }

    public Optional<Path> getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(Path sourcePath) {
        this.sourcePath = Optional.of(sourcePath);
    }

    @Override
    public String toString() {
        return bundleCoordinates + "," + version.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        P2Artifact p2Artifact = (P2Artifact) o;
        return Objects.equals(bundleCoordinates, p2Artifact.bundleCoordinates) &&
                Objects.equals(version, p2Artifact.version) &&
                Objects.equals(jarPath, p2Artifact.jarPath) &&
                Objects.equals(sourcePath, p2Artifact.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bundleCoordinates, version, jarPath, sourcePath);
    }

}
