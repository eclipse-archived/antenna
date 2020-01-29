/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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
import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;

public class ArtifactClearingState extends ArtifactFactWithPayload<ArtifactClearingState.ClearingState>{

    public ArtifactClearingState(ClearingState clearingState) {
        super(clearingState);
    }

    @Override
    public String getFactContentName() {
        return "Clearing State";
    }

    @Override
    public Class<? extends ArtifactFact> getKey() {
        return ArtifactClearingState.class;
    }

    public enum ClearingState {
        INITIAL,
        EXTERNAL_SOURCE,
        AUTO_EXTRACT,
        PROJECT_APPROVED,
        OSM_APPROVED
    }
}