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

package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;

public class GenericArtifactCoordinates extends ArtifactCoordinates {
    private final String name;
    private final String version;

    public GenericArtifactCoordinates(String name, String version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public String getFactContentName() {
        return "Coordinates";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isEmpty() {
        return (name == null || "".equals(name)) &&
                (version == null || "".equals(version));
    }

    @Override
    public ArtifactFact mergeWith(ArtifactFact resultWithPrecedence) {
        if(resultWithPrecedence instanceof GenericArtifactCoordinates) {
            final GenericArtifactCoordinates coordinatesPrecedence = (GenericArtifactCoordinates) resultWithPrecedence;
            return new GenericArtifactCoordinates(
                    coordinatesPrecedence.getName() == null ? name : coordinatesPrecedence.getName(),
                    coordinatesPrecedence.getVersion() == null ? version : coordinatesPrecedence.getVersion());
        }
        return this;
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof GenericArtifactCoordinates) {
            final GenericArtifactCoordinates coordinates = (GenericArtifactCoordinates) artifactIdentifier;
            return (name == null || name.equals(coordinates.getName())) &&
                    (version == null || version.equals(coordinates.getVersion()));
        }
        return false;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
