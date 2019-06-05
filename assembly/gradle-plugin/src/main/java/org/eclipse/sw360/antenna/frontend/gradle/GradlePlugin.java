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

import org.eclipse.sw360.antenna.frontend.stub.gradle.AntennaGradlePlugin;
import org.gradle.api.DefaultTask;

public class GradlePlugin extends AntennaGradlePlugin {
    public Class<? extends DefaultTask> getTaskClass() {
        return AntennaGradleTask.class;
    }
}