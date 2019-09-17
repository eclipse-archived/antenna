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
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.sw360.adapter.commonComparisonProperties.ArtifactCommons;
import org.eclipse.sw360.antenna.sw360.adapter.commonComparisonProperties.SW360ComponentCommons;
import org.eclipse.sw360.antenna.sw360.rest.SW360ReleaseClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.SW360ReleaseAdapterUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SW360ReleaseClientAdapter {
    private final SW360ReleaseClient releaseClient;

    public SW360ReleaseClientAdapter(String restUrl, ProxySettings proxySettings) {
        this.releaseClient = new SW360ReleaseClient(restUrl, proxySettings);
    }

    public SW360Release addRelease(Artifact artifact, SW360Component sw360Component, Set<String> sw360LicenseIds, boolean uploadSource, HttpHeaders header) throws ExecutionException {
        SW360Release release = SW360ReleaseAdapterUtils.prepareRelease(sw360Component, sw360LicenseIds, artifact);

        if (! SW360ReleaseAdapterUtils.isValidRelease(release)) {
            throw new ExecutionException("Can not write invalid release for " + artifact.toString());
        }


        final SW360Release release1 = releaseClient.createRelease(release, header);

        final Optional<Path> sourceFile = artifact.askForGet(ArtifactSourceFile.class);
        if (uploadSource && sourceFile.isPresent()) {
            return releaseClient.uploadAndAttachAttachment(release1, sourceFile.get(), "SOURCE", header);
        } else {
            return release1;
        }
    }
    public SW360Release updateRelease(SW360Release release, Artifact artifact, SW360Component sw360Component, Set<String> sw360LicenseIds, HttpHeaders header) throws ExecutionException {
        SW360Release sw360ReleaseFromArtifact = SW360ReleaseAdapterUtils.prepareRelease(sw360Component, sw360LicenseIds, artifact);

        if (release.equals(sw360ReleaseFromArtifact)) {
            return release;
        } else if(release.shareIdentifier(sw360ReleaseFromArtifact)) {
            return releaseClient.patchRelease(sw360ReleaseFromArtifact.mergeWith(release), header);
        } else {
            return sw360ReleaseFromArtifact;
        }
    }

    public SW360Release getReleaseById(String releaseId, HttpHeaders header) throws ExecutionException {
        return releaseClient.getRelease(releaseId, header);
    }

    public List<SW360SparseRelease> getReleases(HttpHeaders header) throws ExecutionException {
        return releaseClient.getReleases(header);
    }

    public boolean isArtifactAvailableAsRelease(Artifact artifact, SW360Component sw360Component, HttpHeaders header) {
        ArtifactCommons artifactCommons = new ArtifactCommons(artifact);
        SW360ComponentCommons componentCommons = new SW360ComponentCommons(sw360Component);
        return artifactCommons.matchesOnComparisonProperties(componentCommons);
    }

    public Optional<SW360Release> getReleaseByArtifact(SW360Component component, Artifact artifact, HttpHeaders header) throws ExecutionException {
        String releaseVersionOfArtifact = SW360ReleaseAdapterUtils.createSW360ReleaseVersion(artifact);
        if (component != null &&
                component.get_Embedded() != null &&
                component.get_Embedded().getReleases() != null) {

            List<SW360SparseRelease> releases = component.get_Embedded().getReleases();
            Optional<String> releaseId = releases.stream()
                    .filter(release -> release.getVersion().equals(releaseVersionOfArtifact))
                    .findFirst()
                    .flatMap(release -> SW360HalResourceUtility.getLastIndexOfSelfLink(release.get_Links()));
            if (releaseId.isPresent()) {
                return Optional.of(getReleaseById(releaseId.get(), header));
            }
        }
        return Optional.empty();
    }
}
