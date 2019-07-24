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
package org.eclipse.sw360.antenna.frontend;

import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;

import java.util.Map;

public interface AntennaFrontend<T extends IProject> {
    AntennaFrontendHelper init() throws AntennaConfigurationException;
    void execute() throws AntennaException, MojoExecutionException;
    Map<String, IAttachable> getOutputs();
    T getProject();
}
