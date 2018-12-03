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
package org.eclipse.sw360.antenna.api.workflow;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractGenerator extends ConfigurableWorkflowItem implements IWorkflowable {
    public Map<String,IAttachable> produce(ProcessingState previousState) throws AntennaException {
        return produce(previousState.getArtifacts());
    }

    public abstract Map<String,IAttachable> produce(Collection<Artifact> intermediates) throws AntennaException;
}
