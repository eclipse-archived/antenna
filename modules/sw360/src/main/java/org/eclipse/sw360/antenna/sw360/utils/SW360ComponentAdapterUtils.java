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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;

public class SW360ComponentAdapterUtils {
    public static String createComponentName(Artifact artifact) {
        return artifact.askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getMainCoordinate)
                .map(Coordinate::getName)
                .orElse(artifact.toString()); // TODO: ugly hack
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
