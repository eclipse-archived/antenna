/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.main;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter.SW360Exporter;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater.SW360Updater;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class AntennaComplianceTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaComplianceTool.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.warn("Number of arguments: {}", args.length);
            Arrays.asList(args).forEach(LOGGER::warn);
            LOGGER.warn("Usage: java -jar compliancetool.jar <complianceMode> <propertiesFilePath>");
            System.exit(1);
        }

        String mode = args[0];
        Path propertiesFile = Paths.get(args[1]).toAbsolutePath();

        System.exit(new AntennaComplianceTool().execute(mode, propertiesFile));
    }

    private int execute(String mode, Path propertiesFile) {
        switch (mode) {
            case "exporter":
                init(new SW360Exporter(), propertiesFile).execute();
                return 0;
            case "updater":
                init(new SW360Updater(), propertiesFile).execute();
                return 0;
            default:
                LOGGER.error("You did not supply any compliance task.");
                return 1;
        }
    }

    private SW360Exporter init(SW360Exporter executor, Path propertiesFile) {
        SW360Configuration configuration = new SW360Configuration(propertiesFile.toFile());
        executor.setCsvFile(configuration.getTargetDir()
                .resolve(configuration.getCsvFileName())
                .toFile());
        SW360ConnectionConfiguration connectionConfiguration = configuration.getConnectionConfiguration();
        executor.setConnectionConfiguration(connectionConfiguration);
        return executor;
    }

    private SW360Updater init(SW360Updater executor, Path propertiesFile) {
        SW360Configuration configuration = new SW360Configuration(propertiesFile.toFile());
        executor.setConfiguration(configuration);

        executor.setUpdater(new SW360UpdaterImpl(new SW360MetaDataUpdater(
                configuration.getConnectionConfiguration(),
                configuration.getBooleanConfigValue("sw360updateReleases"),
                configuration.getBooleanConfigValue("sw360uploadSources")),
                "redundant project name",
                "redundant project version"));
        // since we only use the updaters functionality to handle individual releases,
        // we do not need to give a project name or version.

        return executor;
    }
}
