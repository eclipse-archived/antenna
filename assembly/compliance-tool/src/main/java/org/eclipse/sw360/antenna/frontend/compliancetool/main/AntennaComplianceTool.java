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

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter.SW360Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AntennaComplianceTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaComplianceTool.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.warn("Number of arguments: {}", args.length);
            Arrays.asList(args).forEach(LOGGER::warn);
            LOGGER.warn("Usage: java -jar compliancetool.jar <complianceMode> <propertiesFilePath>");
            System.exit(1);
        }
        try {
            Path propertiesFilePath = Paths.get(args[1]).toAbsolutePath();
            File propertiesFile = propertiesFilePath.toFile();
            if (!propertiesFile.exists()) {
                throw new IllegalArgumentException("Cannot find " + propertiesFilePath.toString() + ". Please check the path.");
            }

            List<String> argList = Arrays.asList(args);

            if (argList.contains("exporter")) {
                executeSW360Exporter(propertiesFile);
            } else if (argList.contains("updater")) {
                LOGGER.error("Updater is not yet implemented.");
                System.exit(0);
            } else {
                LOGGER.error("You did not supply any compliance task.");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void executeSW360Exporter(File propertiesFile) {
        try {
            new SW360Exporter(propertiesFile).execute();
        } catch (ExecutionException e) {
            LOGGER.error("Error occured while executing the SW360 Exporter");
            LOGGER.error("Failure: ", e);
        }
    }
}
