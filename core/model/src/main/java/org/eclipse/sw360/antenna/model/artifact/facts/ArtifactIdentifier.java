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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;

public interface ArtifactIdentifier<T extends ArtifactIdentifier>
        extends ArtifactFact<T>, ArtifactSelector {
    @Override
    default boolean matches(Artifact artifact) {
        return artifact.getArtifactIdentifiers()
                .stream()
                .anyMatch(this::matches);
    }

    default boolean matches(ArtifactFact artifactFact) {
        if(artifactFact instanceof ArtifactIdentifier) {
            return matches((ArtifactIdentifier) artifactFact);
        }
        return false;
    }

    boolean matches(ArtifactIdentifier artifactIdentifier);
}
