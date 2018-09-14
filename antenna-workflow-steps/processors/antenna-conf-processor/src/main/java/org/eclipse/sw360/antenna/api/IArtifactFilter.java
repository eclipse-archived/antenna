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

package org.eclipse.sw360.antenna.api;

import org.eclipse.sw360.antenna.model.Artifact;

/**
 * An IArtifactFilter filters artifacts according to the Method implemented by a
 * class.
 */
public interface IArtifactFilter {

    /**
     * Returns true if the given artifact has passed the filter, false
     * otherwise.
     * 
     * @param artifact
     * @return
     */
    boolean passed(Artifact artifact);

}
