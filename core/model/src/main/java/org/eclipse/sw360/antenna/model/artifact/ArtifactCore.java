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
package org.eclipse.sw360.antenna.model.artifact;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactCore
        implements IPrettyPrintable{
    private final Map<Class<? extends ArtifactFact>, ArtifactFact> artifactFacts = new HashMap<>();
    private final ArtifactFlags artifactFlags = new ArtifactFlags();
    private final Optional<String> analysisSource;

    private final Logger LOGGER =  LoggerFactory.getLogger(ArtifactCore.class);

    public ArtifactCore() {
        analysisSource = Optional.empty();
    }

    public ArtifactCore(String analysisSource) {
        this.analysisSource = Optional.ofNullable(analysisSource);
    }

    private <T extends ArtifactFact> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    private <T extends ArtifactFact> Stream<T> filterEmptyFacts(Stream<T> stream) {
        return stream
                .filter(not(ArtifactFact::isEmpty));
    }

    private <T extends ArtifactFact> Optional<T> filterEmptyFacts(Optional<T> stream) {
        return stream
                .filter(not(ArtifactFact::isEmpty));
    }

    @SuppressWarnings("unchecked")
    public ArtifactCore addFact(ArtifactFact artifactFact) {
        LOGGER.trace(artifactFact.prettyPrint());

        final Class<? extends ArtifactFact> rowClass = artifactFact.getKey();

        if(artifactFacts.containsKey(rowClass)) {
            artifactFacts.put(rowClass, artifactFacts.get(rowClass).mergeWith(artifactFact));
        } else {
            artifactFacts.put(rowClass, artifactFact);
        }
        return this;
    }

    public ArtifactCore addCoordinate(Coordinate coordinate) {
        return addFact(new ArtifactCoordinates(coordinate));
    }

    public Set<Coordinate> getCoordinates() {
        return askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getCoordinates)
                .orElse(Collections.emptySet());
    }

    public Optional<Coordinate> getCoordinateForType(String type) {
        return askFor(ArtifactCoordinates.class)
                .flatMap(a -> a.getCoordinateForType(type));
    }

    public Optional<Coordinate> getMainCoordinate() {
        return askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getMainCoordinate);
    }

    @SuppressWarnings("unchecked")
    public <T extends ArtifactFact> Optional<T> askFor(Class<T> rowClass) {
        return filterEmptyFacts(Optional.ofNullable(artifactFacts.get(rowClass))
                .filter(rowClass::isInstance)
                .map(rowClass::cast));
    }

    @SuppressWarnings("unchecked")
    public <T extends ArtifactFact> List<T> askForAll(Class<T> rowSuperClass) {
        return filterEmptyFacts(artifactFacts.entrySet().stream()
                .filter(classArtifactFactEntry -> rowSuperClass.isAssignableFrom(classArtifactFactEntry.getKey()))
                .map(Map.Entry::getValue)
                .map(o -> (T) o))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <S, T extends ArtifactFactWithPayload<S>> Optional<S> askForGet(Class<T> rowClass) {
        return askFor(rowClass)
                .map(ArtifactFactWithPayload::get);
    }

    public List<ArtifactIdentifier> getArtifactIdentifiers() {
        return askForAll(ArtifactIdentifier.class);
    }

    public ArtifactCore setFlag(String key, boolean value) {
        artifactFlags.setFlag(key, value);
        return this;
    }

    public ArtifactCore setFlag(String key) {
        artifactFlags.setFlag(key, true);
        return this;
    }

    public boolean getFlag(String key) {
        return artifactFlags.getFlag(key);
    }

    public String getAnalysisSource() {
        return analysisSource
                .orElse("UNKNOWN");
    }

    public void overrideWith(ArtifactCore artifactWithPrecedence) {
        filterEmptyFacts(artifactWithPrecedence.artifactFacts.values().stream())
                .forEach(this::addFact);
        artifactWithPrecedence.artifactFlags.getRawContent()
                .forEach(this::setFlag);
    }

    public String artifactAsCoordinate() {
        return getMainCoordinate()
                .map(Coordinate::canonicalize)
                .orElse(prettyPrint());
    }

    @Override
    public String prettyPrint() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Artifact");
        if(getAnalysisSource() != null) {
            stringBuilder.append(" (")
                    .append(getAnalysisSource())
                    .append(")");
        }
        stringBuilder.append(": ");
        if((artifactFacts.isEmpty() || artifactFacts.values().stream().allMatch(ArtifactFact::isEmpty)) &&
                artifactFlags.isEmpty()) {
            stringBuilder.append("empty");
        } else {
            stringBuilder.append(filterEmptyFacts(artifactFacts.values().stream())
                    .map(IPrettyPrintable::prettyPrint)
                    .sorted()
                    .collect(Collectors.joining("\n\t")));
            stringBuilder.append("\n\tFlags: ")
                    .append(artifactFlags.prettyPrint());
        }

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder()
                .append("Artifact=");
        final List<ArtifactIdentifier> artifactIdentifiers = getArtifactIdentifiers();
        if(artifactIdentifiers.size() > 0) {
            stringBuilder.append(artifactIdentifiers
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(",","[","]")));
        } else {
            stringBuilder.append("[no identifier]");
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtifactCore)) return false;
        ArtifactCore that = (ArtifactCore) o;
        return Objects.equals(artifactFacts, that.artifactFacts) &&
                Objects.equals(artifactFlags, that.artifactFlags) &&
                Objects.equals(analysisSource, that.analysisSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactFacts, artifactFlags, analysisSource);
    }
}
