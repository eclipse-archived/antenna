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
import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/*
 * A thin wrapper around com.github.packageurl.PackageURL
 * It just delegates
 */
public abstract class PackageURLFacade {
    private final PackageURL packageURL;

    PackageURLFacade(PackageURL packageURL) {
        this.packageURL = packageURL;
    }

    PackageURLFacade(String packageURLString) {
        try {
            packageURL = new PackageURL(packageURLString);
        } catch (MalformedPackageURLException e) {
            throw new ExecutionException("Failed to create PackageURL in Coordinate", e);
        }
    }

    PackageURLFacade(String type, String namespace, String name, String version, TreeMap<String, String> qualifiers, String subpath) {
        try {
            packageURL = new PackageURL(type, namespace, name, version, qualifiers, subpath);
        } catch (MalformedPackageURLException e) {
            throw new ExecutionException("Failed to create PackageURL in Coordinate", e);
        }
    }

    public PackageURL getPackageURL() {
        return packageURL;
    }

    public String getScheme() {
        return packageURL.getScheme();
    }

    public final String getType() {
        return packageURL.getType();
    }

    public String getNamespace() {
        return packageURL.getNamespace();
    }

    public boolean hasNamespace() {
        return getNamespace() != null && !"".equals(getNamespace());
    }

    public String getName() {
        return packageURL.getName();
    }

    public String getVersion() {
        return packageURL.getVersion();
    }

    public Map<String, String> getQualifiers() {
        return packageURL.getQualifiers();
    }

    public String getSubpath() {
        return packageURL.getSubpath();
    }

    public String toString() {
        return packageURL.toString();
    }

    public String canonicalize() {
        return packageURL.canonicalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageURLFacade coordinate = (PackageURLFacade) o;
        return Objects.equals(packageURL.canonicalize(), coordinate.packageURL.canonicalize()); // TODO: PackageURL has no valid equals method
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageURL.canonicalize()); // TODO: PackageURL has no valid hashCode method
    }

}
