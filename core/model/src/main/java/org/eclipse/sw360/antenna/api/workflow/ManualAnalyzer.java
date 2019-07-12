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

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * Executes an analysis of the source code artifacts in a project manually.
 */
public abstract class ManualAnalyzer extends AbstractAnalyzer {

    private static final String FILE_KEY = "file.path";
    private static final String BASEDIR_KEY = "base.dir";

    protected Path baseDir;
    protected File componentInfoFile;

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        final Path basedir = context.getProject().getBasedir().toPath();

        String filename = getConfigValue(FILE_KEY, configMap);
        this.componentInfoFile = basedir.resolve(filename).toFile();

        String baseDirStr = getConfigValue(BASEDIR_KEY, configMap);
        this.baseDir = basedir.resolve(baseDirStr).toAbsolutePath();
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public File getComponentInfoFile() {
        return componentInfoFile;
    }
}
