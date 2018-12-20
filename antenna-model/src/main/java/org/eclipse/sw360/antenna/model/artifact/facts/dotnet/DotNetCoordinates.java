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

package org.eclipse.sw360.antenna.model.artifact.facts.dotnet;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFactBuilder;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Objects;
import java.util.Optional;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public class DotNetCoordinates extends ArtifactCoordinates<DotNetCoordinates> {
    private final String packageId;
    private final String version;

    public DotNetCoordinates(String packageId, String version) {
        this.packageId = packageId;
        this.version = version;
    }

    @Override
    public String getName() { return getPackageId(); }

    @Override
    public String getVersion() { return version; }

    public String getPackageId() { return packageId; }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof DotNetCoordinates) {
            final DotNetCoordinates javaScriptCoordinates = (DotNetCoordinates) artifactIdentifier;
            return compareStringsAsWildcard(packageId, javaScriptCoordinates.getPackageId()) ||
                    compareStringsAsWildcard(version, javaScriptCoordinates.getVersion());
        }
        return false;
    }

    @Override
    public String getFactContentName() {
        return "DotNetCoordinates";
    }

    @Override
    public boolean isEmpty() {
        return (packageId == null || "".equals(packageId)) &&
                (version == null || "".equals(version));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DotNetCoordinates that = (DotNetCoordinates) o;
        return Objects.equals(packageId, that.packageId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() { return Objects.hash(packageId, version); }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DotNetCoordinatesBuilder
            implements ArtifactFactBuilder {
        protected String packageId;
        protected String version;

        public void setPackageId(String value) {
            this.packageId = value;
        }

        public void setVersion(String value) {
            this.version = value;
        }

        @Override
        public DotNetCoordinates build() {
            if(packageId != null) {
                packageId = packageId.trim();
            }
            if(version != null) {
                version = version.trim();
            }
            return new DotNetCoordinates(packageId, version);
        }
    }

    @Override
    public String toString() {
        return packageId + ":" + version;
    }

    @Override
    public DotNetCoordinates mergeWith(DotNetCoordinates resultWithPrecedence) {
        return new DotNetCoordinates(
                Optional.ofNullable(resultWithPrecedence.getPackageId())
                        .orElse(Optional.ofNullable(getPackageId())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getVersion())
                        .orElse(Optional.ofNullable(getVersion())
                                .orElse(null)));
    }
}
