/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.coordinates;

import com.github.packageurl.PackageURL;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public final class Coordinate extends PackageURLFacade {
    public Coordinate(PackageURL packageURL) {
        super(packageURL);
    }

    public Coordinate(String packageURLString) {
        super(packageURLString);
    }

    public Coordinate(String name, String version) {
        super(Types.GENERIC, null, name, version, null, null);
    }

    public Coordinate(String type, String namespace, String name, String version) {
        super(type, namespace, name, version, null, null);
    }

    public Coordinate(String type, String name, String version) {
        super(type, null, name, version, null, null);
    }

    public Coordinate(String type, String namespace, String name, String version, TreeMap<String, String> qualifiers, String subpath) {
        super(type, namespace, name, version, qualifiers, subpath);
    }

    public static CoordinateBuilder builder() {
        return new CoordinateBuilder();
    }

    public boolean matches(String packageUrlString) {
        return matches(new Coordinate(packageUrlString));
    }

    public boolean matches(Coordinate coordinate) {
        if(coordinate == null) {
            return false;
        }
        return compareStringsAsWildcard(getScheme(), coordinate.getScheme()) &&
                compareStringsAsWildcard(getType(), coordinate.getType()) &&
                compareStringsAsWildcard(getNamespace(), coordinate.getNamespace()) &&
                compareStringsAsWildcard(getName(), coordinate.getName()) &&
                compareStringsAsWildcard(getVersion(), coordinate.getVersion()) &&
                compareStringsAsWildcard(getSubpath(), coordinate.getSubpath());
    }

    public static class Types extends PackageURL.StandardTypes {
        public static final String P2 = "p2";

        public static final Set<String> all = Collections.unmodifiableSet(Stream.of(
                // Standard Types:
                BITBUCKET, COMPOSER, DEBIAN, DOCKER, GEM, GENERIC, GITHUB, GOLANG, MAVEN, NPM, NUGET, PYPI, RPM,
                // Custom Types:
                P2)
                .collect(Collectors.toSet()));
    }
}
