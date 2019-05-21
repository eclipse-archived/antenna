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

package org.eclipse.sw360.antenna.model.artifact;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;

import java.util.*;
import java.util.stream.Collectors;

public class ArtifactSelectorAndSet implements ArtifactSelector {

    private final Set<? extends ArtifactSelector> selectors;

    public ArtifactSelectorAndSet(ArtifactSelector...  selectors) {
        this.selectors = new HashSet<>(Arrays.asList(selectors));
    }

    public ArtifactSelectorAndSet(Collection<? extends ArtifactSelector> selectors) {
        this.selectors = new HashSet<>(selectors);
    }

    @Override
    public boolean matches(Artifact artifact) {
        return selectors.stream()
                .allMatch(as -> as.matches(artifact));
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        return selectors.stream()
                .allMatch(as -> as.matches(artifactIdentifier));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactSelectorAndSet that = (ArtifactSelectorAndSet) o;
        return Objects.equals(selectors, that.selectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectors);
    }

    @Override
    public String toString() {
        if(selectors.size() == 1) {
            return selectors.stream().findAny().get().toString();
        }
        return selectors.stream()
                .map(ArtifactSelector::toString)
                .collect(Collectors.joining(" AND ", "( ", " )"));
    }
}
