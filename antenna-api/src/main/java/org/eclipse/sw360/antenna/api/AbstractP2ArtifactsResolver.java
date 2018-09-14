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

import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.Artifact;

import java.io.IOException;
import java.util.List;

/**
 * Resolves p2 artifacts.
 */
public abstract class AbstractP2ArtifactsResolver extends AbstractProcessor {
    /**
     * 
     * @param targetDirectory
     *            Directory where the resolved p2 artifacts will be saved.
     */
    public abstract void setTargetDirectory(String targetDirectory);
    public abstract void resolveArtifacts(List<Artifact> list) throws IOException;
}
