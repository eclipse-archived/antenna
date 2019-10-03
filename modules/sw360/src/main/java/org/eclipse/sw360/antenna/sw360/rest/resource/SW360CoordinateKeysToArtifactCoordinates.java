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
package org.eclipse.sw360.antenna.sw360.rest.resource;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.GenericArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum SW360CoordinateKeysToArtifactCoordinates {
    MVN(MavenCoordinates.class, "mvn"),
    DOTNET(DotNetCoordinates.class, "dotnet"),
    BUNDLES(BundleCoordinates.class, "bundles"),
    JAVASCRIPT(JavaScriptCoordinates.class, "javascript"),
    GENERAL(GenericArtifactCoordinates.class, "general");

    private final Class<? extends ArtifactCoordinates> key;
    private final String value;

    private static final List<SW360CoordinateKeysToArtifactCoordinates> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    SW360CoordinateKeysToArtifactCoordinates(Class<? extends ArtifactCoordinates> key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String get(Class<? extends ArtifactCoordinates> key0) {
        return VALUES.stream()
                .filter(e -> e.key == key0)
                .findFirst()
                .map(e -> e.value)
                .orElse(null);
    }

    public static Class<? extends ArtifactCoordinates> get(String value0) {
        return VALUES.stream()
                .filter(e -> e.value.equals(value0))
                .findFirst()
                .map(e -> e.key)
                .orElse(null);
    }

    public static Set<Class<? extends ArtifactCoordinates>> getKeys() {
        return VALUES.stream()
                .map(e -> e.key)
                .collect(Collectors.toSet());
    }

    public static Set<String> getValues() {
        return VALUES.stream()
                .map(e -> e.value)
                .collect(Collectors.toSet());
    }

    public static ArtifactCoordinates createArtifactCoordinates(String group, String name, String version, Class<?extends ArtifactCoordinates> coordinateClass) {
        try {
            Object object = null;
            if(coordinateClass == SW360CoordinateKeysToArtifactCoordinates.MVN.key){
                object = coordinateClass.getConstructors()[0].newInstance(name, group, version);
            } else if (coordinateClass == SW360CoordinateKeysToArtifactCoordinates.DOTNET.key
                    || coordinateClass == SW360CoordinateKeysToArtifactCoordinates.BUNDLES.key
                    || coordinateClass == SW360CoordinateKeysToArtifactCoordinates.GENERAL.key) {
                object = coordinateClass.getConstructors()[0].newInstance(name, version);
            } else if (coordinateClass == SW360CoordinateKeysToArtifactCoordinates.JAVASCRIPT.key) {
                object = coordinateClass.getConstructors()[0].newInstance(name, group, version);
            }
            return (ArtifactCoordinates) object;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new AntennaExecutionException();
        }
    }

}
