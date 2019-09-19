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

package org.eclipse.sw360.antenna.model.artifact.facts.javaScript;

import com.github.packageurl.PackageURLBuilder;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFactBuilder;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Objects;
import java.util.Optional;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public class JavaScriptCoordinates extends ArtifactCoordinates<JavaScriptCoordinates> {
    private final String artifactId;
    private final String name;
    private final String version;

    public JavaScriptCoordinates(String artifactId, String name, String version) {
        this.artifactId = artifactId;
        this.name = name;
        this.version = version;
    }

    @Override
    public String getFactContentName() {
        return "JavaScriptCoordinates";
    }

    @Override
    public JavaScriptCoordinates mergeWith(JavaScriptCoordinates resultWithPrecedence) {
        return new JavaScriptCoordinates(
                Optional.ofNullable(resultWithPrecedence.getArtifactId())
                        .orElse(Optional.ofNullable(getArtifactId())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getName())
                        .orElse(Optional.ofNullable(getName())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getVersion())
                        .orElse(Optional.ofNullable(getVersion())
                                .orElse(null)));
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
    public String getType() {
        return "npm";
    }

    @Override
    protected PackageURLBuilder addPurlFacts(PackageURLBuilder builder) {
        return builder.withNamespace(getName()).withName(getArtifactId());
    }

    @Override
    public String toString() {
        return name + ":" + version + " (" + artifactId + ")";
    }

    @Override
    public boolean isEmpty() {
        return (name == null || "".equals(name)) &&
                (version == null || "".equals(version)) &&
                (artifactId == null || "".equals(artifactId));
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof JavaScriptCoordinates) {
            final JavaScriptCoordinates javaScriptCoordinates = (JavaScriptCoordinates) artifactIdentifier;
            return compareStringsAsWildcard(artifactId, javaScriptCoordinates.getArtifactId()) ||
                    compareStringsAsWildcard(name, javaScriptCoordinates.getName()) ||
                    compareStringsAsWildcard(version, javaScriptCoordinates.getVersion());
        }
        return false;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaScriptCoordinates that = (JavaScriptCoordinates) o;
        return Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, name, version);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JavaScriptCoordinatesBuilder
            implements ArtifactFactBuilder {
        protected String artifactId;
        protected String name;
        protected String version;

        public JavaScriptCoordinatesBuilder setArtifactId(String value) {
            this.artifactId = value;
            return this;
        }

        public JavaScriptCoordinatesBuilder setName(String value) {
            this.name = value;
            return this;
        }

        public JavaScriptCoordinatesBuilder setVersion(String value) {
            this.version = value;
            return this;
        }

        @Override
        public JavaScriptCoordinates build() {
            if(artifactId != null) {
                artifactId = artifactId.trim();
            }
            if(name != null) {
                name = name.trim();
            }
            if(version != null) {
                version = version.trim();
            }
            return new JavaScriptCoordinates(artifactId, name, version);
        }
    }
}

