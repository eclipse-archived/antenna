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

package org.eclipse.sw360.antenna.model.artifact.facts.java;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFactBuilder;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Objects;
import java.util.Optional;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public class BundleCoordinates extends JavaCoordinates<BundleCoordinates> {
    protected final String symbolicName;
    protected final String bundleVersion;

    public BundleCoordinates(String symbolicName, String bundleVersion) {
        this.symbolicName = symbolicName;
        this.bundleVersion = bundleVersion;
    }

    @Override
    public String getFactContentName() {
        return "BundleCoordinates";
    }

    @Override
    public boolean isEmpty() {
        return (symbolicName == null || "".equals(symbolicName)) &&
                (bundleVersion == null || "".equals(bundleVersion));
    }

    @Override
    public BundleCoordinates mergeWith(BundleCoordinates resultWithPrecedence) {
        return new BundleCoordinates(
                Optional.ofNullable(resultWithPrecedence.getSymbolicName())
                        .orElse(Optional.ofNullable(getSymbolicName())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getBundleVersion())
                        .orElse(Optional.ofNullable(getBundleVersion())
                                .orElse(null)));
    }

    @Override
    public String getName() {
        return getSymbolicName();
    }

    @Override
    public String getVersion() {
        return getBundleVersion();
    }

    @Override
    public String getType() {
        return "p2";
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof BundleCoordinates) {
            final BundleCoordinates bundleCoordinates = (BundleCoordinates) artifactIdentifier;
            return compareStringsAsWildcard(symbolicName, bundleCoordinates.getSymbolicName()) &&
                    compareStringsAsWildcard(bundleVersion, bundleCoordinates.getBundleVersion());
        }
        return false;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BundleCoordinates that = (BundleCoordinates) o;
        return Objects.equals(symbolicName, that.symbolicName) &&
                Objects.equals(bundleVersion, that.bundleVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolicName, bundleVersion);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BundleCoordinatesBuilder
            implements ArtifactFactBuilder {
        protected String symbolicName;
        protected String bundleVersion;

        public void setSymbolicName(String value) {
            this.symbolicName = value;
        }

        public void setBundleVersion(String value) {
            this.bundleVersion = value;
        }

        @Override
        public BundleCoordinates build() {
            if(symbolicName != null) {
                symbolicName = symbolicName.trim();
            }
            if(bundleVersion != null) {
                bundleVersion = bundleVersion.trim();
            }
            return new BundleCoordinates(symbolicName, bundleVersion);
        }
    }
}
