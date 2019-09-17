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

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;

/**
 * Executes an analysis of the source code artifacts in a project.
 */
public abstract class AbstractAnalyzer extends ConfigurableWorkflowItem {

    /**
     * @return Identifies this analyzer.
     */
    public abstract String getName();

    public abstract WorkflowStepResult yield() throws ExecutionException;
}
