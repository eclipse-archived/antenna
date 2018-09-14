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
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.util.Set;

/**
 * Checks if MatchState of artifact is allowed. Disallowed MatchStates can be
 * set in blacklist. MatchStateArtifactFilter
 */

public class MatchStateArtifactFilter implements IArtifactFilter {
    private Set<MatchState> blacklist;

    public MatchStateArtifactFilter(Set<MatchState> blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * @return Returns true if the artifacts MatchState is not in the blacklist.
     */
    @Override
    public boolean passed(Artifact artifact) {
        MatchState matchState = artifact.getMatchState();
        boolean blacklisted = this.blacklist.contains(matchState);
        boolean canPass = !blacklisted;
        return canPass;
    }

}
