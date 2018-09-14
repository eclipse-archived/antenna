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
package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.util.ArtifactsComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArtifactSorter extends AbstractProcessor {

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        List<Artifact> sortedArtifacts = new ArrayList<>(intermediates);
        sortedArtifacts.sort(new ArtifactsComparator());
        return sortedArtifacts;
    }
}
