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

package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.util.Objects;

public class ArtifactMatchingMetadata implements ArtifactFact<ArtifactMatchingMetadata> {
    private final MatchState matchState;

    public MatchState getMatchState() {
        return matchState;
    }

    public ArtifactMatchingMetadata(MatchState matchState) {
        this.matchState = matchState;
    }

    @Override
    public String getFactContentName() {
        return "Artifact Matchig Metadata";
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String prettyPrint() {
        return "ArtifactMatchingMetadata{" +
                "matchState=" + matchState +
                '}';
    }

    @Override
    public String toString() {
        return matchState.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactMatchingMetadata that = (ArtifactMatchingMetadata) o;
        return matchState == that.matchState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchState);
    }
}
