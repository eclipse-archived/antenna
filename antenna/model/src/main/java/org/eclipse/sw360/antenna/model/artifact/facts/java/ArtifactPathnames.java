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

package org.eclipse.sw360.antenna.model.artifact.facts.java;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;

import java.util.Arrays;
import java.util.List;

public class ArtifactPathnames extends ArtifactFactWithPayload<List<String>>
        implements ArtifactIdentifier {
    public ArtifactPathnames(List<String> payload) {
        super(payload);
    }

    public ArtifactPathnames(String... payload) {
        super(Arrays.asList(payload));
    }

    @Override
    public String getFactContentName() {
        return "Pathnames";
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || get().size() == 0;
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if(! (artifactIdentifier instanceof ArtifactPathnames)) {
            return false;
        }
        ArtifactPathnames artifactPathnames = (ArtifactPathnames) artifactIdentifier;
        final List<String> pathnames = get();
        final List<String> otherPathnames = artifactPathnames.get();
        if(pathnames == null || otherPathnames == null) {
            return false;
        }
        return pathnames.stream()
                .anyMatch(pathname -> otherPathnames.stream()
                        .anyMatch(pathname::equals));
    }
}
