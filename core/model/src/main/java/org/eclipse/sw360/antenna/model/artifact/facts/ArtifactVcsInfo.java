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
package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;

import java.util.Objects;
import java.util.Optional;

public class ArtifactVcsInfo implements ArtifactFact<ArtifactVcsInfo> {
    private final VcsInfo vcsInfo;

    public static class VcsInfo {
        private final String type;
        private final String url;
        private final String revision;

        public VcsInfo(String type, String url, String revision) {
            this.type = type;
            this.url = url;
            this.revision = revision;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public String getRevision() {
            return revision;
        }

        public boolean isEmpty() {
            return type == null && url == null && revision == null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VcsInfo vcsInfo = (VcsInfo) o;
            return Objects.equals(type, vcsInfo.type) &&
                    Objects.equals(url, vcsInfo.url) &&
                    Objects.equals(revision, vcsInfo.revision);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, url, revision);
        }

        @Override
        public String toString() {
            return "VcsInfo{" +
                    "type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", revision='" + revision + '\'' +
                    '}';
        }
    }

    public ArtifactVcsInfo(String type, String url, String revision) {
        this(new VcsInfo(type, url, revision));
    }

    public ArtifactVcsInfo(VcsInfo vcsInfo) {
        this.vcsInfo = vcsInfo;
    }

    public VcsInfo getVcsInfo() {
        return vcsInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactVcsInfo that = (ArtifactVcsInfo) o;
        return Objects.equals(vcsInfo, that.vcsInfo);
    }

    @Override
    public String toString() {
        return Optional.ofNullable(vcsInfo)
                .map(Objects::toString)
                .orElse("EMPTY");
    }

    @Override
    public int hashCode() {
        return Objects.hash(vcsInfo);
    }

    @Override
    public String getFactContentName() {
        return "VCS Information";
    }

    @Override
    public boolean isEmpty() {
        return vcsInfo == null;
    }

    @Override
    public String prettyPrint() {
        return "Set ArtifactVcsInfo to " + toString();
    }
}
