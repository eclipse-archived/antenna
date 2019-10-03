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

import org.eclipse.sw360.antenna.model.Configuration;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * Reads a given config file (uri or File) and returns a Configuration. The
 * config file is written to the targetDirectory as backup. The
 * processingReporter can be used to keep track of the configuration.
 * IConfigReader
 */
public interface IConfigReader {
    Configuration readConfigFromFile(File configFile, Path targetDirectory);
    Configuration readConfigFromUri(URI configFileUri, Path targetDirectory);
}
