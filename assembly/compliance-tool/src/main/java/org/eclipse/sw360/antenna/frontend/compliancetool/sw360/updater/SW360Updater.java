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
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils.getArtifactsFromCsvFile;

public class SW360Updater {
    private SW360UpdaterImpl updater;
    private SW360Configuration configuration;
    private ClearingReportGenerator generator;

    public void setUpdater(SW360UpdaterImpl updater) {
        this.updater = updater;
    }

    public void setConfiguration(SW360Configuration configuration) {
        this.configuration = configuration;
    }

    public void setClearingReportGenerator(ClearingReportGenerator generator) {
        this.generator = generator;
    }

    public void execute() {
        Collection<Artifact> artifacts = getArtifactsFromCsvFile(configuration.getProperties());

        artifacts.forEach(this::uploadReleaseWithClearingDocumentFromArtifact);
    }

    private void uploadReleaseWithClearingDocumentFromArtifact(Artifact artifact) {
        SW360Release release = updater.artifactToReleaseInSW360(artifact);
        SW360ReleaseClientAdapter releaseClientAdapter = configuration.getConnectionConfiguration().getSW360ReleaseClientAdapter();

        if (release.getClearingState() != null &&
                !release.getClearingState().isEmpty() &&
                ArtifactClearingState.ClearingState.valueOf(release.getClearingState()) != ArtifactClearingState.ClearingState.INITIAL) {
            Map<Path, SW360AttachmentType> attachmentPathMap =
                    Collections.singletonMap(getOrGenerateClearingDocument(release, artifact),
                            SW360AttachmentType.CLEARING_REPORT);
            releaseClientAdapter.uploadAttachments(release, attachmentPathMap);
        }
    }

    private Path getOrGenerateClearingDocument(SW360Release release, Artifact artifact) {
        return artifact.askFor(ArtifactClearingDocument.class).map(ArtifactClearingDocument::get)
                .orElse(generator.createClearingDocument(release, configuration.getTargetDir()));
    }
}
