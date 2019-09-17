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
package org.eclipse.sw360.antenna.sw360.workflow.generators;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;

import java.io.IOException;
import java.util.*;

public class SW360UpdaterImpl {
    private final String projectName;
    private final String projectVersion;
    private final SW360MetaDataUpdater sw360MetaDataUpdater;

    public SW360UpdaterImpl(SW360MetaDataUpdater sw360MetaDataUpdater,
                            String projectName, String projectVersion) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.sw360MetaDataUpdater = sw360MetaDataUpdater;
    }

    public Map<String, IAttachable> produce(Collection<Artifact> intermediates) throws ExecutionException {

        try {
            List<SW360Release> releases = new ArrayList<>();
            for (Artifact artifact : intermediates) {
                Set<String> licenses = sw360MetaDataUpdater.getOrCreateLicenses(artifact);
                SW360Component component = sw360MetaDataUpdater.getOrCreateComponent(artifact);
                releases.add(sw360MetaDataUpdater.getOrCreateRelease(artifact, licenses, component));
            }
            sw360MetaDataUpdater.createProject(projectName, projectVersion, releases);
        } catch (IOException e) {
            throw new ExecutionException("Problem occurred during updating SW360.", e);
        }
        return Collections.emptyMap();
    }

}
