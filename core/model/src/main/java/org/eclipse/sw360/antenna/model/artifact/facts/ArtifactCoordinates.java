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

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;

public abstract class ArtifactCoordinates<T extends ArtifactCoordinates> implements ArtifactIdentifier<T> {
    public abstract String getName();

    public abstract String getVersion();

    public abstract String getType();

    protected PackageURLBuilder addPurlFacts(PackageURLBuilder builder) {
        return builder;
    }

    public PackageURL getPurl() {
        PackageURLBuilder builder =  PackageURLBuilder.aPackageURL()
                .withType(getType())
                .withName(getName())
                .withVersion(getVersion());

        builder = addPurlFacts(builder);
        
        try {
            return builder.build();
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException("Could not build Purl", e);
        }
    }

    @Override
    public String prettyPrint() {
        return "Set " + getFactContentName() + " to " + toString();
    }

    @Override
    public String toString() {
        return getName() + ":" + getVersion();
    }
}
