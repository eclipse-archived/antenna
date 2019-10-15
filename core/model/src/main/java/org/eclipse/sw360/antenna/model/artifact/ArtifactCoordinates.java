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

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ArtifactCoordinates implements ArtifactIdentifier<ArtifactCoordinates>, ArtifactFact<ArtifactCoordinates> {
    private final Map<String, Coordinate> coordinates = new HashMap<>();
    private String typeOfMainCoordinate; // Contains the type of the coordinate added first

    private void putCoordinate(Coordinate coordinate) {
        if (coordinate != null) {
            final String type = coordinate.getType();
            coordinates.put(type, coordinate);
            setTypeOfMainCoordinate(type);
        }
    }

    private void setTypeOfMainCoordinate(String type) {
        if(typeOfMainCoordinate == null || Coordinate.Types.GENERIC.equals(typeOfMainCoordinate)) {
            typeOfMainCoordinate = type;
        }
    }

    private void putCoordinates(Collection<Coordinate> coordinates) {
        coordinates.forEach(this::putCoordinate);
    }

    public ArtifactCoordinates(Coordinate... coordinatesToAdd) {
        putCoordinates(Arrays.asList(coordinatesToAdd));
    }

    public ArtifactCoordinates(Collection<Coordinate> coordinatesToAdd) {
        putCoordinates(coordinatesToAdd);
    }

    public ArtifactCoordinates(String typeOfMainCoordinate, Collection<Coordinate> coordinatesToAdd) {
        setTypeOfMainCoordinate(typeOfMainCoordinate);
        putCoordinates(coordinatesToAdd);
        if (! coordinates.containsKey(typeOfMainCoordinate)) {
            throw new ExecutionException("The type=[" + typeOfMainCoordinate + "] is missing in=" + this.toString());
        }
    }

    public ArtifactCoordinates(Set<String> coordinateStringsToAdd) {
        for (String coordinateStringToAdd: coordinateStringsToAdd) {
            putCoordinate(new Coordinate(coordinateStringToAdd));
        }
    }

    public boolean containsPurl(String coordinateString)  {
        return containsPurl(new Coordinate(coordinateString));
    }

    public boolean containsPurl(Coordinate coordinate) {
        return coordinates.values().contains(coordinate);
    }

    public Set<Coordinate> getCoordinates() {
        return new HashSet<>(coordinates.values());
    }

    public Optional<Coordinate> getCoordinateForType(String type) {
        return Optional.ofNullable(coordinates.get(type));
    }

    public Coordinate getMainCoordinate() {
        if (typeOfMainCoordinate == null) {
            throw new ExecutionException("Not yet any coordinate added");
        }
        return coordinates.get(typeOfMainCoordinate);
    }

    @Override
    public String prettyPrint() {
        return "Set ArtifactCoordinates to " + toString();
    }

    @Override
    public String toString() {
        return coordinates.values().stream()
                .map(Coordinate::canonicalize)
                .collect(Collectors.joining("\", \"","[ \"","\" ]"));
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if (artifactIdentifier instanceof ArtifactCoordinates) {
            ArtifactCoordinates artifactCoordinates = (ArtifactCoordinates) artifactIdentifier;
            return coordinates.entrySet()
                    .stream()
                    .filter(e -> Objects.nonNull(e.getValue()))
                    .anyMatch(e -> {
                        final Coordinate thisCoordinate = e.getValue();
                        final Coordinate otherCoordinate = artifactCoordinates.coordinates.get(e.getKey());
                        return thisCoordinate.matches(otherCoordinate);
                    });
        }
        return false;
    }

    @Override
    public String getFactContentName() {
        return "ArtifactCoordinates";
    }

    @Override
    public boolean isEmpty() {
        return coordinates.size() == 0;
    }

    @Override
    public ArtifactCoordinates mergeWith(ArtifactCoordinates resultWithPrecedence) {
        return new ArtifactCoordinates(typeOfMainCoordinate,
                Stream.concat(coordinates.values().stream(), resultWithPrecedence.coordinates.values().stream())
                        .collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactCoordinates that = (ArtifactCoordinates) o;
        return Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates);
    }
}
