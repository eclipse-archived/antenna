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

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class SW360ComponentAdapterUtils {
    public static String createComponentName(ArtifactIdentifier artifactIdentifier) {
        String name = "";
        if (artifactIdentifier != null) {
            if ((artifactIdentifier.getMavenCoordinates() != null)
                    && (artifactIdentifier.getMavenCoordinates().getGroupId() != null)
                    && (artifactIdentifier.getMavenCoordinates().getArtifactId() != null)) {
                final String groupId = artifactIdentifier.getMavenCoordinates().getGroupId();
                final String artifactId = artifactIdentifier.getMavenCoordinates().getArtifactId();
                name = groupId + ":" + artifactId;
            } else if ((artifactIdentifier.getBundleCoordinates() != null)
                    && (artifactIdentifier.getBundleCoordinates().getSymbolicName() != null)) {
                name = artifactIdentifier.getBundleCoordinates().getSymbolicName();
            } else {
                name = artifactIdentifier.getFilename();
            }
        }
        return name;
    }

    public static String createComponentVersion(ArtifactIdentifier artifactIdentifier) {
        String version = "-";
        if (artifactIdentifier != null) {
            if ((artifactIdentifier.getMavenCoordinates() != null)
                    && (artifactIdentifier.getMavenCoordinates().getVersion() != null)) {
                version = artifactIdentifier.getMavenCoordinates().getVersion();
            } else if ((artifactIdentifier.getBundleCoordinates() != null)
                    && (artifactIdentifier.getBundleCoordinates().getBundleVersion() != null)) {
                version = artifactIdentifier.getBundleCoordinates().getBundleVersion();
            }
        }
        return version;
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
        final String name = createComponentName(artifact.getArtifactIdentifier());
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
        SW360ComponentAdapterUtils.setCpeid(release, artifact);
        release.setName(component.getName());
        release.setComponentId(componentId);
        release.setMainLicenseIds(sw360LicenseIds);
    }

    public static String createSW360ReleaseVersion(ArtifactIdentifier artifactIdentifier) {
        String version = "-";
        if (artifactIdentifier != null) {
            if ((artifactIdentifier.getMavenCoordinates() != null)
                    && (artifactIdentifier.getMavenCoordinates().getVersion() != null)) {
                version = artifactIdentifier.getMavenCoordinates().getVersion();
            } else if ((artifactIdentifier.getBundleCoordinates() != null)
                    && (artifactIdentifier.getBundleCoordinates().getBundleVersion() != null)) {
                version = artifactIdentifier.getBundleCoordinates().getBundleVersion();
            }
        }
        return version;
    }

    public static void setCpeid(SW360Release release, Artifact artifact) {
        String cpeId = "cpe:/a:";
        if (artifact.getArtifactIdentifier() != null) {
            if (artifact.getArtifactIdentifier().getMavenCoordinates() != null) {
                if ((artifact.getArtifactIdentifier().getMavenCoordinates().getGroupId() != null)
                        && (artifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId() != null)
                        && (artifact.getArtifactIdentifier().getMavenCoordinates().getVersion() != null)) {
                    final String vendor = artifact.getArtifactIdentifier().getMavenCoordinates().getGroupId();
                    final String product = artifact.getArtifactIdentifier().getMavenCoordinates().getArtifactId();
                    final String version = artifact.getArtifactIdentifier().getMavenCoordinates().getVersion();
                    cpeId += vendor + ":" + product + ":" + version + ":" + "::";
                }
            } else if (artifact.getArtifactIdentifier().getBundleCoordinates() != null) {
                if (artifact.getArtifactIdentifier().getBundleCoordinates().getSymbolicName() != null) {
                    final String vendor = artifact.getArtifactIdentifier().getBundleCoordinates().getSymbolicName();
                    cpeId += vendor + ":" + "::::";
                }
            }
        }
        release.setCpeid(cpeId);
    }

    public static void setVersion(SW360Release release, Artifact artifact) {
        final String version = SW360ComponentAdapterUtils.createSW360ReleaseVersion(artifact.getArtifactIdentifier());
        if ((version != null) && (!version.isEmpty())) {
            release.setVersion(version);
        }
    }
}
