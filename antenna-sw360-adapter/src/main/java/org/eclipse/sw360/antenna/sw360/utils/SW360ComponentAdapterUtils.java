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
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

public class SW360ComponentAdapterUtils {
    private static Optional<ArtifactCoordinates> getMostDominantArtifactCoordinates(Artifact artifact) {
        final Optional<ArtifactCoordinates> mavenCoordinates = artifact.askFor(MavenCoordinates.class)
                .map(ArtifactCoordinates.class::cast);
        if(mavenCoordinates.isPresent()) {
            return mavenCoordinates;
        }
        final Optional<ArtifactCoordinates> bundleCoordinates = artifact.askFor(BundleCoordinates.class)
                .map(ArtifactCoordinates.class::cast);
        if(bundleCoordinates.isPresent()) {
            return bundleCoordinates;
        }
        final Optional<ArtifactCoordinates> otherCoordinates = artifact.askForAll(ArtifactCoordinates.class)
                .stream()
                .findFirst();
        if(otherCoordinates.isPresent()) {
            return otherCoordinates;
        }
        return artifact.askFor(ArtifactFilename.class)
                .map(ArtifactFilename::getFilename)
                .map(fn -> new GenericArtifactCoordinates(fn, "-"))
                .map(ArtifactCoordinates.class::cast);
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
        if ((date != null) && (!date.isEmpty())) {
            component.setCreatedOn(date);
        } else {
            setCreatedOn(component);
        }
    }

    public static void setName(SW360Component component, Artifact artifact) {
        final String name = createComponentName(artifact);
        if ((name != null) && (!name.isEmpty())) {
            component.setName(name);
        }
    }

    public static void setComponentType(SW360Component component, Artifact artifact) {
        if (artifact.isProprietary()) {
            component.setComponentType(SW360ComponentType.INTERNAL);
        } else {
            component.setComponentType(SW360ComponentType.OSS);
        }
    }

    public static void prepareComponent(SW360Component component, Artifact artifact) {
        SW360ComponentAdapterUtils.setCreatedOn(component);
        SW360ComponentAdapterUtils.setName(component, artifact);
        SW360ComponentAdapterUtils.setComponentType(component, artifact);
    }

    public static void prepareRelease(SW360Release release, SW360Component component, Set<String> sw360LicenseIds, Artifact artifact) {
        String componentId = SW360HalResourceUtility.getLastIndexOfLinkObject(component.get_Links()).orElse("");

        SW360ComponentAdapterUtils.setVersion(release, artifact);
        SW360ComponentAdapterUtils.setCPEId(release, artifact);
        release.setName(component.getName());
        release.setComponentId(componentId);
        release.setMainLicenseIds(sw360LicenseIds);
    }

    public static String createSW360ReleaseVersion(Artifact artifact) {
        return createComponentVersion(artifact);
    }

    public static void setVersion(SW360Release release, Artifact artifact) {
        final String version = SW360ComponentAdapterUtils.createSW360ReleaseVersion(artifact);
        if ((version != null) && (!version.isEmpty())) {
            release.setVersion(version);
        }
    }

    private static void setCPEId(SW360Release release, Artifact artifact) {
        release.setCpeid(artifact.askForGet(ArtifactCPE.class)
                .orElse("n/a"));
    }
}
