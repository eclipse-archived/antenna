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

import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public class ArtifactFilename implements ArtifactFact, ArtifactIdentifier {
    private final String filename;
    private final String hash;
    private final String hashAlgorithm;

    public ArtifactFilename(String filename) {
        this.filename = Optional.ofNullable(filename).map(String::trim).orElse(null);
        this.hash = null;
        this.hashAlgorithm = "UNKNOWN";
    }

    public ArtifactFilename(String filename, String hash) {
        this.filename = Optional.ofNullable(filename).map(String::trim).orElse(null);
        this.hash = Optional.ofNullable(hash).map(String::trim).orElse(null);
        this.hashAlgorithm = "UNKNOWN";
    }

    public ArtifactFilename(String filename, String hash, String hashAlgorithm) {
        this.filename = Optional.ofNullable(filename).map(String::trim).orElse(null);
        this.hash = Optional.ofNullable(hash).map(String::trim).orElse(null);
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(artifactIdentifier instanceof ArtifactFilename) {
            ArtifactFilename artifactFilename = (ArtifactFilename) artifactIdentifier;
            return compareStringsAsWildcard(filename, artifactFilename.filename) &&
                    compareStringsAsWildcard(hash, artifactFilename.hash) &&
                    (hash == null || Objects.equals(hashAlgorithm, artifactFilename.hashAlgorithm));
        }
        if(artifactIdentifier instanceof ArtifactPathnames && filename != null) {
            List<String> artifactPathnames = ((ArtifactPathnames) artifactIdentifier).get();
            return artifactPathnames.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(pn -> pn.equals(filename) || pn.endsWith("/" + filename) || pn.endsWith("\\" + filename));
        }
        return false;
    }

    public String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    @Override
    public String getFactContentName() {
        return "Filename";
    }

    @Override
    public boolean isEmpty() {
        return filename == null && hash == null;
    }

    @Override
    public String prettyPrint() {
        return "Set " + getFactContentName() + " to " + toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactFilename that = (ArtifactFilename) o;
        return Objects.equals(filename, that.filename) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(hashAlgorithm, that.hashAlgorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, hash, hashAlgorithm);
    }

    @Override
    public String toString() {
        return  filename + " (hash='" + hash + '\'' + ", hashAlgorithm='" + hashAlgorithm + '\'' + ')';
    }
}
