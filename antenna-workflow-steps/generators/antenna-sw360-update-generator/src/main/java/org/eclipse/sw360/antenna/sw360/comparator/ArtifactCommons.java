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
package org.eclipse.sw360.antenna.sw360.comparator;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;

import java.util.Arrays;
import java.util.List;

public class ArtifactCommons implements ICommonComparisonProperties {
    private final Artifact artifact;

    public ArtifactCommons(Artifact artifact) {
        this.artifact = artifact;
    }

    public String getName() {
        String name = SW360ComponentAdapterUtils.createComponentName(artifact.getArtifactIdentifier());
        return name;
    }

    public List<String> getVersions() {
        String version = SW360ComponentAdapterUtils.createComponentVersion(artifact.getArtifactIdentifier());
        return Arrays.asList(version);
    }
}
