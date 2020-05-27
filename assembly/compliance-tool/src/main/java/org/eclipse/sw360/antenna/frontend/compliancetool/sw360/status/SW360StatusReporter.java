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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of a status reporter for the compliance tool.
 * It takes parameters given to the reporter mode and produces
 * a csv file with the information requested in its information
 * parameter
 */
public class SW360StatusReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360StatusReporter.class);

    private final SW360Configuration configuration;
    private InfoParameter infoParameter;

    public SW360StatusReporter(SW360Configuration configuration, Set<String> parameters) {
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
        this.infoParameter =  SW360StatusReporterParameters
                .getInfoRequestFromParameter(Objects.requireNonNull(parameters, "Parameters must not be null"));
    }

    void setInfoParameter(InfoParameter infoParameter) {
        this.infoParameter = infoParameter;
    }

    /**
     * Executes the execute function of the infoParameter and prints it
     * to a csv file.
     */
    public void execute() {
        LOGGER.debug("{} has started.", SW360StatusReporter.class.getName());
        final SW360Connection connection = configuration.getConnection();

        infoParameter.execute(connection);

        String header = infoParameter.getResultFileHeader();
        String[] body = infoParameter.printResult();

        printCsvFile(header, body, configuration.getCsvFileName(), configuration.getTargetDir());
    }

    /**
     * Prints a csv file with a given name to a given target directory.
     * Header and body are written
     * @param header
     * @param body
     * @param csvFileName
     * @param targetDir
     */
    private void printCsvFile(String header, String[] body, String csvFileName, Path targetDir) {
        if (header.split(";").length != Arrays.stream(body).findAny().map(s -> s.split(";").length).orElse(0)) {
            LOGGER.error("Number of header columns does not equal columns of body for the csv file.");
        }
        Path csvFile = Paths.get(targetDir.toString(), csvFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header))
        ) {
            for (String item : body) {
                csvPrinter.printRecords(item);
            }
            csvPrinter.flush();
        } catch (IOException e) {
            LOGGER.error("Error when writing the csv file", e);
        }
    }
}
