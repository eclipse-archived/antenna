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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public class JavaScriptCoordinates extends ArtifactCoordinates<JavaScriptCoordinates> {
    private final String namespace;
    private final String packageName;
    private final String version;

    public JavaScriptCoordinates(String namespace, String packageName, String version) {
        this.namespace = namespace;
        this.packageName = packageName;
        this.version = version;
    }

    @Override
    public String getFactContentName() {
        return "JavaScriptCoordinates";
    }

    @Override
    public JavaScriptCoordinates mergeWith(JavaScriptCoordinates resultWithPrecedence) {
        return new JavaScriptCoordinates(
                Optional.ofNullable(resultWithPrecedence.getNamespace())
                        .orElse(Optional.ofNullable(getNamespace())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getPackageName())
                        .orElse(Optional.ofNullable(getName())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getVersion())
                        .orElse(Optional.ofNullable(getVersion())
                                .orElse(null)));
    }

    @Override
    public String getName() {
        return Stream.of(namespace, packageName)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("/"));
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
        return builder.withNamespace(namespace).withName(packageName);
    }

    @Override
    public String toString() {
        return Stream.of(namespace, packageName, version)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(":"));
    }

    @Override
    public boolean isEmpty() {
        return (packageName == null || "".equals(packageName)) &&
                (version == null || "".equals(version)) &&
                (namespace == null || "".equals(namespace));
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof JavaScriptCoordinates) {
            final JavaScriptCoordinates javaScriptCoordinates = (JavaScriptCoordinates) artifactIdentifier;
            return compareStringsAsWildcard(namespace, javaScriptCoordinates.getNamespace()) ||
                    compareStringsAsWildcard(packageName, javaScriptCoordinates.getName()) ||
                    compareStringsAsWildcard(version, javaScriptCoordinates.getVersion());
        }
        return false;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaScriptCoordinates that = (JavaScriptCoordinates) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(packageName, that.packageName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, packageName, version);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JavaScriptCoordinatesBuilder
            implements ArtifactFactBuilder {
        protected String namespace;
        protected String packageName;
        protected String version;

        public JavaScriptCoordinatesBuilder setNamespace(String value) {
            this.namespace = value;
            return this;
        }

        public JavaScriptCoordinatesBuilder setPackageName(String value) {
            this.packageName = value;
            return this;
        }

        public JavaScriptCoordinatesBuilder setVersion(String value) {
            this.version = value;
            return this;
        }

        @Override
        public JavaScriptCoordinates build() {
            if(namespace != null) {
                namespace = namespace.trim();
            }
            if(packageName != null) {
                packageName = packageName.trim();
            }
            if(version != null) {
                version = version.trim();
            }
            return new JavaScriptCoordinates(namespace, packageName, version);
        }
    }
}

