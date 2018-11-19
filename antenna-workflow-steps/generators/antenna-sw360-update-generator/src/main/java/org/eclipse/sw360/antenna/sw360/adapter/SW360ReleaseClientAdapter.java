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
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.sw360.comparator.ArtifactCommons;
import org.eclipse.sw360.antenna.sw360.comparator.ArtifactSW360ComponentComparator;
import org.eclipse.sw360.antenna.sw360.comparator.SW360ComponentCommons;
import org.eclipse.sw360.antenna.sw360.rest.SW360ReleaseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseEmbedded;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SW360ReleaseClientAdapter {
    private final SW360ReleaseClient releaseClient;

    public SW360ReleaseClientAdapter(String restUrl) {
        this.releaseClient = new SW360ReleaseClient(restUrl);
    }

    public SW360Release addRelease(Artifact artifact, SW360Component sw360Component, Set<String> sw360LicenseIds, HttpHeaders header) throws IOException, AntennaException {
        SW360Release release = new SW360Release();
        SW360ComponentAdapterUtils.prepareRelease(release, sw360Component, sw360LicenseIds, artifact);
        return releaseClient.createRelease(release, header);
    }

    public SW360Release getReleaseById(String releaseId, HttpHeaders header) throws AntennaException {
        return releaseClient.getRelease(releaseId, header);
    }

    public List<SW360Release> getReleaseSummary(HttpHeaders header) throws IOException, AntennaException {
        return releaseClient.getReleases(header);
    }

    public boolean isArtifactAvailableAsRelease(Artifact artifact, SW360Component sw360Component, HttpHeaders header) {
        ArtifactCommons artifactCommons = new ArtifactCommons(artifact);
        SW360ComponentCommons componentCommons = new SW360ComponentCommons(sw360Component);
        return ArtifactSW360ComponentComparator.compare(artifactCommons, componentCommons);
    }

    public Optional<SW360Release> getReleaseByArtifact(SW360Component<LinkObjects, SW360ReleaseEmbedded> component, Artifact artifact) {
        String releaseVersionOfArtifact = SW360ComponentAdapterUtils.createSW360ReleaseVersion(artifact.getArtifactIdentifier());
        if ((component.get_Embedded() != null) && (component.get_Embedded().getReleases() != null)) {
            List<SW360Release> releases = component.get_Embedded().getReleases();
            for (SW360Release release : releases) {
                if (release.getVersion().equals(releaseVersionOfArtifact)){
                    return Optional.of(release);
                }
            }
        }
        return Optional.empty();
    }
}
