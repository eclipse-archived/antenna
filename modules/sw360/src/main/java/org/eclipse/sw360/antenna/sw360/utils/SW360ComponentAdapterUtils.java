/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename.ArtifactFilenameEntry;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SW360ComponentAdapterUtils {
    public static String createComponentName(Artifact artifact) {
        return artifact.askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getMainCoordinate)
                .map(o -> Stream.of(o.getNamespace(), o.getName()))
                .map(o -> o.collect(Collectors.joining("/")))
                .orElseGet(() -> artifact.askFor(ArtifactFilename.class)
                        .flatMap(ArtifactFilename::getBestFilenameEntryGuess)
                        .map(ArtifactFilenameEntry::getFilename)
                        .orElseThrow(() -> new ExecutionException("Artifact " + artifact.prettyPrint() + " does not have " +
                                "enough facts to create a component name. Please provide more information " +
                                "by an analyzer (" + artifact.getAnalysisSource() + ")")));
    }

    public static String createComponentVersion(Artifact artifact) {
        return artifact.askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getMainCoordinate)
                .map(Coordinate::getVersion)
                .orElse("-");
    }

    public static void setName(SW360Component component, Artifact artifact) {
        component.setName(createComponentName(artifact));
    }

    private static void setComponentType(SW360Component component, Artifact artifact) {
        setComponentType(component, artifact.isProprietary());
    }

    private static void setComponentType(SW360Component component, boolean isProprietary) {
        if (isProprietary) {
            component.setComponentType(SW360ComponentType.INTERNAL);
        } else {
            component.setComponentType(SW360ComponentType.OSS);
        }
    }

    public static SW360Component createFromRelease(SW360Release release) {
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName(release.getName());
        setComponentType(sw360Component, release.isProprietary());
        return sw360Component;
    }

    public static void prepareComponent(SW360Component component, Artifact artifact) {
        SW360ComponentAdapterUtils.setName(component, artifact);
        SW360ComponentAdapterUtils.setComponentType(component, artifact);
    }

    public static boolean isValidComponent(SW360Component component) {
        return component.getName() != null && !component.getName().isEmpty();
    }
}
