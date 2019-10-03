/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

public class SW360MetaDataReceiver {
    // rest service adapters
    private SW360ComponentClientAdapter componentClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;
    private SW360LicenseClientAdapter licenseClientAdapter;

    private SW360ConnectionConfiguration sw360ConnectionConfiguration;

    public SW360MetaDataReceiver(SW360ConnectionConfiguration sw360ConnectionConfiguration) {
        componentClientAdapter = sw360ConnectionConfiguration.getSW360ComponentClientAdapter();
        releaseClientAdapter = sw360ConnectionConfiguration.getSW360ReleaseClientAdapter();
        licenseClientAdapter = sw360ConnectionConfiguration.getSW360LicenseClientAdapter();
        this.sw360ConnectionConfiguration = sw360ConnectionConfiguration;
    }

    public Optional<SW360Release> findReleaseForArtifact(Artifact artifact) {
        HttpHeaders headers = sw360ConnectionConfiguration.getHttpHeaders();
        Optional<SW360Component> component = componentClientAdapter.getComponentByArtifact(artifact, headers);
        if (component.isPresent()) {
            return releaseClientAdapter.getReleaseByArtifact(component.get(), artifact, headers);
        }
        return Optional.empty();
    }

    public Optional<SW360License> getLicenseDetails(SW360SparseLicense sparseLicense) {
        HttpHeaders headers = sw360ConnectionConfiguration.getHttpHeaders();
        return licenseClientAdapter.getLicenseDetails(sparseLicense, headers);
    }
}
