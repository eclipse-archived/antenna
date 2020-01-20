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
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;

import java.util.Collection;

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

        artifacts.forEach(updater::artifactToReleaseInSW360);
    }
}
