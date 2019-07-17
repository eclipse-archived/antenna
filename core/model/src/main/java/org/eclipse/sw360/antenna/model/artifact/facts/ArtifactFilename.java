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

import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorHelper.compareStringsAsWildcard;

public class ArtifactFilename implements ArtifactFact<ArtifactFilename>, ArtifactIdentifier<ArtifactFilename> {
    private final Set<ArtifactFilenameEntry> artifactFilenameEntries = new HashSet<>();

    public static class ArtifactFilenameEntry {
        private final String filename;
        private final String hash;
        private final String hashAlgorithm;

        public ArtifactFilenameEntry(String filename) {
            this.filename = sanitizeString(filename);
            this.hash = null;
            this.hashAlgorithm = "UNKNOWN";
        }

        public ArtifactFilenameEntry(String filename, String hash) {
            this.filename = sanitizeString(filename);
            this.hash = sanitizeString(hash);
            this.hashAlgorithm = "UNKNOWN";
        }

        public ArtifactFilenameEntry(String filename, String hash, String hashAlgorithm) {
            this.filename = sanitizeString(filename);
            this.hash = sanitizeString(hash);
            this.hashAlgorithm = hashAlgorithm;
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

        private String sanitizeString(String input) {
            return Optional.ofNullable(input)
                    .map(String::trim)
                    .filter(string -> !string.isEmpty())
                    .orElse(null);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArtifactFilenameEntry that = (ArtifactFilenameEntry) o;
            return Objects.equals(filename, that.filename) &&
                    Objects.equals(hash, that.hash) &&
                    Objects.equals(hashAlgorithm, that.hashAlgorithm);
        }

        public boolean matches(ArtifactFilenameEntry artifactFilenameEntry) {
            return compareStringsAsWildcard(filename, artifactFilenameEntry.filename) &&
                    compareStringsAsWildcard(hash, artifactFilenameEntry.hash) &&
                    (hash == null || Objects.equals(hashAlgorithm, artifactFilenameEntry.hashAlgorithm));

        }

        @Override
        public int hashCode() {
            return Objects.hash(filename, hash, hashAlgorithm);
        }

        public boolean isEmpty() {
            return filename == null && hash == null;
        }

        @Override
        public String toString() {
            return filename + " (hash='" + hash + '\'' + ", hashAlgorithm='" + hashAlgorithm + '\'' + ')';
        }
    }

    public ArtifactFilename(String filename) {
        artifactFilenameEntries.add(new ArtifactFilenameEntry(filename));
    }

    public ArtifactFilename(String filename, String hash) {
        artifactFilenameEntries.add(new ArtifactFilenameEntry(filename, hash));
    }

    public ArtifactFilename(String filename, String hash, String hashAlgorithm) {
        artifactFilenameEntries.add(new ArtifactFilenameEntry(filename, hash, hashAlgorithm));
    }

    public Set<ArtifactFilenameEntry> getArtifactFilenameEntries() {
        return Collections.unmodifiableSet(artifactFilenameEntries);
    }

    public List<String> getFilenames() {
        return artifactFilenameEntries.stream()
                .map(ArtifactFilenameEntry::getFilename)
                .collect(Collectors.toList());
    }

    public Optional<ArtifactFilenameEntry> getBestFilenameEntryGuess() {
        return artifactFilenameEntries.stream()
                .filter(afe -> Objects.nonNull(afe.getFilename()))
                .max(Comparator.comparing(afe -> afe.getFilename().length()));
    }

    @Override
    public ArtifactFilename mergeWith(ArtifactFilename resultWithPrecedence) {
        if (resultWithPrecedence != null) {
            artifactFilenameEntries.addAll(resultWithPrecedence.getArtifactFilenameEntries());
        }
        return this;
    }


    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if (artifactIdentifier instanceof ArtifactFilename) {
            ArtifactFilename artifactFilename = (ArtifactFilename) artifactIdentifier;

            return artifactFilenameEntries.stream()
                    .anyMatch(artifactFilenameEntry ->
                            artifactFilename.getArtifactFilenameEntries().stream()
                                    .anyMatch(artifactFilenameEntry1 ->
                                            artifactFilenameEntry.matches(artifactFilenameEntry1)
                                                    || artifactFilenameEntry1.matches(artifactFilenameEntry)
                                    )
                    );
        }
        if (artifactIdentifier instanceof ArtifactPathnames) {
            List<String> artifactPathnames = ((ArtifactPathnames) artifactIdentifier).get();
            return artifactPathnames.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(pn ->
                            artifactFilenameEntries.stream()
                                    .map(ArtifactFilenameEntry::getFilename)
                                    .filter(Objects::nonNull)
                                    .anyMatch(filename -> pn.equals(filename) || pn.endsWith("/" + filename) || pn.endsWith("\\" + filename))
                    );
        }
        return false;
    }

    @Override
    public String getFactContentName() {
        return "Filename";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactFilename that = (ArtifactFilename) o;
        return Objects.equals(artifactFilenameEntries, that.artifactFilenameEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactFilenameEntries);
    }

    @Override
    public boolean isEmpty() {
        return artifactFilenameEntries.stream()
                .allMatch(ArtifactFilenameEntry::isEmpty);
    }

    @Override
    public String prettyPrint() {
        return "Set " + getFactContentName() + " to " + toString();
    }


    @Override
    public String toString() {
        return artifactFilenameEntries.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
