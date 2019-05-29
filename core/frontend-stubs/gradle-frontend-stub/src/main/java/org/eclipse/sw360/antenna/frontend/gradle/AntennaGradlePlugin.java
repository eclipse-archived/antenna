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
package org.eclipse.sw360.antenna.frontend.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class AntennaGradlePlugin implements Plugin<Project> {
    public static final String TASK_NAME = "analyze";

    public abstract Class<? extends DefaultTask> getTaskClass();

    @Override
    public void apply(Project target) {
        target.getExtensions().create("AntennaConfiguration", AntennaExtension.class);
        target.getTasks().create(TASK_NAME, getTaskClass());
    }

}

