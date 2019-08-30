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
package org.eclipse.sw360.antenna.model.util;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.GenericArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactUtils {
    private ArtifactUtils() {
        // only static methods
    }

    public static <T extends ArtifactFact> Optional<T> getMostDominantFact(Class<T> tClass,
            List<Class<? extends T>> preferedFactTypes,
            Artifact artifact) {
        return getMostDominantFact(tClass, preferedFactTypes, a -> Optional.empty(), artifact);
    }

    public static <T extends ArtifactFact> Optional<T> getMostDominantFact(Class<T> tClass,
            List<Class<? extends T>> preferedFactTypes,
            Function<Artifact, Optional<? extends T>> fallback,
            Artifact artifact) {
        final Optional<T> prefered = preferedFactTypes.stream()
                .map(preferedFact -> artifact.askFor(preferedFact)
                        .map(tClass::cast))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        if (prefered.isPresent()) {
            return prefered;
        }

        final Optional<T> other = artifact.askForAll(tClass)
                .stream()
                .findFirst();
        if (other.isPresent()) {
            return other;
        }

        return fallback.apply(artifact)
                .map(tClass::cast);
    }

    public static final List<Class<? extends ArtifactCoordinates>> DEFAULT_PREFERED_COORDINATE_TYPES = Stream
            .of(MavenCoordinates.class, BundleCoordinates.class, JavaScriptCoordinates.class, DotNetCoordinates.class)
            .collect(Collectors.toList());

    public static Optional<ArtifactCoordinates> getMostDominantArtifactCoordinates(
            List<Class<? extends ArtifactCoordinates>> preferedCoordinatesTypes, Artifact artifact) {
        return getMostDominantFact(ArtifactCoordinates.class,
                preferedCoordinatesTypes,
                a -> a.askFor(ArtifactFilename.class)
                        .flatMap(ArtifactFilename::getBestFilenameEntryGuess)
                        .map(ArtifactFilename.ArtifactFilenameEntry::getFilename)
                        .map(fn -> new GenericArtifactCoordinates(fn, "-")),
                artifact);
    }
}
