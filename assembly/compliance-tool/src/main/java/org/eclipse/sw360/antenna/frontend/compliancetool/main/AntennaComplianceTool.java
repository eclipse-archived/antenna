/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
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
                LOGGER.error("Updater is not yet implemented.");
                return 0;
            default:
                LOGGER.error("You did not supply any compliance task.");
                return 1;
        }
    }

    private SW360Exporter init(SW360Exporter executor, Path propertiesFile) {
        SW360Configuration configuration = new SW360Configuration(propertiesFile.toFile());
        executor.setCsvFile(configuration.getCsvFile());
        SW360ConnectionConfiguration connectionConfiguration = configuration.getConnectionConfiguration();
        executor.setConnectionConfiguration(connectionConfiguration);
        return executor;
    }
}
