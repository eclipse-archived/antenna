/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingDocument;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
import java.util.Collections;

import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils.getArtifactsFromCsvFile;

public class SW360Updater {
    private SW360UpdaterImpl updater;
    private SW360Configuration configuration;

    public void setUpdater(SW360UpdaterImpl updater) {
        this.updater = updater;
    }

    public void setConfiguration(SW360Configuration configuration) {
        this.configuration = configuration;
    }

    public void execute() {
        Collection<Artifact> artifacts = getArtifactsFromCsvFile(configuration.getProperties());
        HttpHeaders headers = configuration.getConnectionConfiguration().getHttpHeaders();

        artifacts.forEach(artifact -> uploadReleaseWithClearingDocumentFromArtifact(artifact, headers));
    }

    private void uploadReleaseWithClearingDocumentFromArtifact(Artifact artifact, HttpHeaders headers) {
        SW360Release release = updater.artifactToReleaseInSW360(artifact);
        SW360ReleaseClientAdapter releaseClientAdapter = configuration.getConnectionConfiguration().getSW360ReleaseClientAdapter();
        artifact.askFor(ArtifactClearingDocument.class)
                .map(ArtifactClearingDocument::get)
                .map(acd -> Collections.singletonMap(acd, SW360AttachmentType.CLEARING_REPORT))
                .ifPresent(attachmentPathMap -> releaseClientAdapter
                        .uploadAttachments(release, attachmentPathMap, headers));
    }
}
