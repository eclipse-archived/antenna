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
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.util.ArtifactUtils;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SW360ComponentAdapterUtils {
    private static final List<Class<? extends ArtifactCoordinates>> preferredCoordinatesTypes = Stream.of(MavenCoordinates.class,
            BundleCoordinates.class,
            JavaScriptCoordinates.class,
            DotNetCoordinates.class).collect(Collectors.toList());

    private static Optional<ArtifactCoordinates> getMostDominantArtifactCoordinates(Artifact artifact) {
        return ArtifactUtils.getMostDominantArtifactCoordinates(
                preferredCoordinatesTypes,
                artifact);
    }

    public static String createComponentName(Artifact artifact) {
        return getMostDominantArtifactCoordinates(artifact)
                .map(ArtifactCoordinates::getName)
                .orElse("");
    }

    public static String createComponentVersion(Artifact artifact) {
        return getMostDominantArtifactCoordinates(artifact)
                .map(ArtifactCoordinates::getVersion)
                .orElse("-");
    }

    public static void setCreatedOn(SW360Component component) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String createdOn = dateFormat.format(new Date());
        component.setCreatedOn(createdOn);
    }

    public static void setCreatedOn(SW360Component component, String date) {
        if (date != null && !date.isEmpty()) {
            component.setCreatedOn(date);
        } else {
            setCreatedOn(component);
        }
    }

    public static void setName(SW360Component component, Artifact artifact) {
        final String name = createComponentName(artifact);
        if (!name.isEmpty()) {
            component.setName(name);
        } else {
            component.setName(artifact.toString()); // TODO: ugly hack
        }
    }

    public static void setComponentType(SW360Component component, Artifact artifact) {
        if (artifact.isProprietary()) {
            component.setComponentType(SW360ComponentType.INTERNAL);
        } else {
            component.setComponentType(SW360ComponentType.OSS);
        }
    }

    public static void setHomePage(SW360Component component, Artifact artifact) {
        artifact.askForGet(ArtifactSourceUrl.class)
                .ifPresent(component::setHomepage);
    }

    public static void prepareComponent(SW360Component component, Artifact artifact) {
        SW360ComponentAdapterUtils.setCreatedOn(component);
        SW360ComponentAdapterUtils.setName(component, artifact);
        SW360ComponentAdapterUtils.setComponentType(component, artifact);
        SW360ComponentAdapterUtils.setHomePage(component, artifact);
    }

    public static boolean isValidComponent(SW360Component component) {
        if(component.getName() == null || component.getName().isEmpty()) {
            return false;
        }
        return true;
    }
}
