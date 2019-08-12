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

public class MavenCoordinates extends JavaCoordinates<MavenCoordinates> {
    protected final String artifactId;
    protected final String groupId;
    protected final String version;

    public MavenCoordinates(String artifactId, String groupId, String version) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;
    }

    @Override
    public String getFactContentName() {
        return "MavenCoordinates";
    }

    @Override
    public MavenCoordinates mergeWith(MavenCoordinates resultWithPrecedence) {
        return new MavenCoordinates(
                Optional.ofNullable(resultWithPrecedence.getArtifactId())
                        .orElse(Optional.ofNullable(getArtifactId())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getGroupId())
                        .orElse(Optional.ofNullable(getGroupId())
                                .orElse(null)),
                Optional.ofNullable(resultWithPrecedence.getVersion())
                        .orElse(Optional.ofNullable(getVersion())
                                .orElse(null)));
    }

    @Override
    public String getName() {
        return getGroupId() + ":" + getArtifactId();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isEmpty() {
        return (groupId == null || "".equals(groupId)) &&
                (version == null || "".equals(version)) &&
                (artifactId == null || "".equals(artifactId));
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof MavenCoordinates) {
            final MavenCoordinates mavenCoordinates = (MavenCoordinates) artifactIdentifier;
            return compareStringsAsWildcard(artifactId, mavenCoordinates.getArtifactId()) &&
                    compareStringsAsWildcard(groupId, mavenCoordinates.getGroupId()) &&
                    compareStringsAsWildcard(version, mavenCoordinates.getVersion());
        }
        return false;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenCoordinates that = (MavenCoordinates) o;
        return Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, groupId, version);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MavenCoordinatesBuilder
            implements ArtifactFactBuilder {

        private String artifactId;
        private String groupId;
        private String version;

        public MavenCoordinatesBuilder setArtifactId(String value) {
            this.artifactId = value;
            return this;
        }

        public MavenCoordinatesBuilder setGroupId(String value) {
            this.groupId = value;
            return this;
        }

        public MavenCoordinatesBuilder setVersion(String value) {
            this.version = value;
            return this;
        }

        @Override
        public MavenCoordinates build() {
            if(artifactId != null) {
                artifactId = artifactId.trim();
            }
            if(groupId != null) {
                groupId = groupId.trim();
            }
            if(version != null) {
                version = version.trim();
            }
            return new MavenCoordinates(artifactId, groupId, version);
        }
    }

}
