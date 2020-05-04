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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter.SW360Exporter;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater.ClearingReportGenerator;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater.SW360Updater;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataUpdater;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AntennaComplianceTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaComplianceTool.class);

    public static void main(String[] args) {
        AntennaComplianceToolOptions options = AntennaComplianceToolOptions.parse(args);
        if (options.isShowHelp()) {
            printUsage();
            System.exit(1);
        }

        if (options.isDebugLog()) {
            enableDebugLogging();
        }
        try {
            Path propertiesFile = Paths.get(options.getPropertiesFilePath()).toAbsolutePath();

            if (!propertiesFile.toFile().exists()) {
                LOGGER.error("Cannot find {}", propertiesFile.toString());
                throw new IllegalArgumentException("Cannot find " + propertiesFile.toString());
            }

            LOGGER.info("Starting Compliance Tool with mode '{}'", options.getComplianceMode());

            System.exit(new AntennaComplianceTool().execute(options.getComplianceMode(), propertiesFile));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error:" + e.getMessage());
            System.exit(1);
        }
    }

    private int execute(String mode, Path propertiesFile) {
        switch (mode) {
            case "exporter":
                createExporter(propertiesFile).execute();
                return 0;
            case "updater":
                createUpdater(propertiesFile).execute();
                return 0;
            default:
                LOGGER.error("You did not supply any compliance task.");
                return 1;
        }
    }

    private SW360Exporter createExporter(Path propertiesFile) {
        SW360Configuration configuration = new SW360Configuration(propertiesFile.toFile());
        return new SW360Exporter(configuration);
    }

    private SW360Updater createUpdater(Path propertiesFile) {
        SW360Configuration configuration = new SW360Configuration(propertiesFile.toFile());

        SW360UpdaterImpl updaterImpl = new SW360UpdaterImpl(new SW360MetaDataUpdater(
                configuration.getConnection(),
                configuration.getBooleanConfigValue("sw360updateReleases"),
                configuration.getBooleanConfigValue("sw360uploadSources")),
                "redundant project name",
                "redundant project version");
        // since we only use the updaters functionality to handle individual releases,
        // we do not need to give a project name or version.

        return new SW360Updater(updaterImpl, configuration, new ClearingReportGenerator());
    }

    private static void enableDebugLogging() {
        Configurator.setRootLevel(Level.DEBUG);
        Configurator.setLevel("org.eclipse.sw360.antenna", Level.DEBUG);
    }

    private static void printUsage() {
        System.out.println(AntennaComplianceToolOptions.helpMessage());
    }
}
