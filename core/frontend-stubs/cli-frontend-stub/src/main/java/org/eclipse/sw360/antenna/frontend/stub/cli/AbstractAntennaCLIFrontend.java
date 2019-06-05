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
package org.eclipse.sw360.antenna.frontend.stub.cli;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.core.AntennaCore;
import org.eclipse.sw360.antenna.frontend.AntennaFrontend;
import org.eclipse.sw360.antenna.frontend.AntennaFrontendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides configuration and execution services for using Antenna as a CLI tool.
 */
public abstract class AbstractAntennaCLIFrontend implements AntennaFrontend {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAntennaCLIFrontend.class);
    private final Map<String, IAttachable> output = new HashMap<>();
    private DefaultProject project;
    private final String pluginDescendantArtifactIdName;

    protected abstract String getPluginDescendantArtifactIdName();

    public AbstractAntennaCLIFrontend(File pomFile) {
        this.pluginDescendantArtifactIdName = getPluginDescendantArtifactIdName();
        Path buildDir = getBuildDirFromPomFile(pomFile);
        Path sourceDir = getSourceDirFromPomFile(pomFile);
        project = new DefaultProject(pomFile, buildDir.toString(), sourceDir.toString());
    }

    private Path getSourceDirFromPomFile(File pomFile) {
        Path parent = pomFile.toPath().getParent();
        return parent.resolve("src");
    }

    private Path getBuildDirFromPomFile(File pomFile) {
        Path parent = pomFile.toPath().getParent();
        return getBuildDirFromFolder(parent);
    }

    protected Path getBuildDirFromFolder(Path folder) {
        return folder.resolve("target");
    }

    /**
     * Assign the properties to the AntennaContext.
     */
    @Override
    public AntennaFrontendHelper init() {
        ToolConfiguration toolConfiguration = new AntennaCLISettingsReader(pluginDescendantArtifactIdName)
                .readSettingsToToolConfiguration(project);

        return new AntennaFrontendHelper(project)
                .setToolConfiguration(toolConfiguration);
    }

    @Override
    public void execute() throws AntennaException {
        AntennaCore antennaCore;
        try{
            final AntennaFrontendHelper antennaFrontendHelper = init();
            antennaCore = antennaFrontendHelper.buildAntennaCore();
        } catch (AntennaConfigurationException e) {
            LOGGER.error("AntennaCore was not initialized sucessfully");
            throw e;
        }

        try {
            output.putAll(antennaCore.compose());
        } catch (AntennaExecutionException | AntennaException e) {
            LOGGER.error("Antenna execution failed due to: " + e.getMessage(), e);
            throw e;
        } finally {
            final IAttachable report = antennaCore.writeAnalysisReport();
            output.put(IProcessingReporter.getIdentifier(), report);
        }
    }

    public Map<String, IAttachable> getOutputs() {
        return output;
    }
}
