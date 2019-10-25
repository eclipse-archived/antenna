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
package org.eclipse.sw360.antenna.model.coordinates;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURLBuilder;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;

/*
 * A thin wrapper around com.github.packageurl.PackageURLBuilder
 * It just delegates
 */
public final class CoordinateBuilder {
    private final PackageURLBuilder packageURLBuilder = PackageURLBuilder.aPackageURL();

    public CoordinateBuilder withType(String type) {
        packageURLBuilder.withType(type);
        return this;
    }

    public CoordinateBuilder withNamespace(String namespace) {
        packageURLBuilder.withNamespace(namespace);
        return this;
    }

    public CoordinateBuilder withName(String name) {
        packageURLBuilder.withName(name);
        return this;
    }

    public CoordinateBuilder withVersion(String version) {
        packageURLBuilder.withVersion(version);
        return this;
    }

    public CoordinateBuilder withSubpath(String subpath) {
        packageURLBuilder.withSubpath(subpath);
        return this;
    }

    public CoordinateBuilder withQualifier(String key, String value) {
        packageURLBuilder.withQualifier(key, value);
        return this;
    }

    public Coordinate build() {
        try {
            return new Coordinate(packageURLBuilder.build());
        } catch (MalformedPackageURLException e) {
            throw new ExecutionException("Failed to build PackageURL in Builder for Coordinate", e);
        }
    }
}
