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
import org.eclipse.sw360.antenna.model.artifact.Artifact;

/**
 * Checks if artifact is proprietary.
 */
public class ProprietaryArtifactFilter implements IArtifactFilter {
    @Override
    public boolean passed(Artifact artifact) {
        return ! artifact.isProprietary();
    }

}
