/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.analysis.filter;

import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.ArtifactSelector;

import java.util.List;

/**
 * This filter can be used to check if an artifact is part of a list, which is
 * for example configured in the config.xml.
 */
public class BlacklistFilter implements IArtifactFilter {
    private List<ArtifactSelector> artifactSelectorBlackList;

    public BlacklistFilter(List<ArtifactSelector> blackList) {
        this.artifactSelectorBlackList = blackList;
    }

    /**
     * @return Returns true if artifact does not match an ArtifactSelector in in list.
     */
    @Override
    public boolean passed(Artifact artifact) {
        return artifactSelectorBlackList.stream()
                .noneMatch(artifactSelector -> artifactSelector.matches(artifact));
    }

}
