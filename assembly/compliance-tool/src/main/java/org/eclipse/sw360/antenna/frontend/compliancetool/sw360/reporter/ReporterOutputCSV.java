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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

/**
 * An implementation of the {@link ReporterOutput} that
 * creates an output file in a csv format
 */
public class ReporterOutputCSV implements ReporterOutput {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReporterOutputCSV.class);

    private Class resultType;

    private Path filePath;

    @Override
    public void setResultType(Class type) {
        this.resultType = type;
    }

    @Override
    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public <T> void print(Collection<T> result) {
        String header = getHeader();
        String[] body = setBody(result);

        printCsvFile(header, body);
    }

    private <T> String[] setBody(Collection<T> result) {
        if (resultType.equals(SW360Release.class)) {
            return ReporterUtils.printCollectionOfReleases((Collection<SW360Release>) result);
        } else if (resultType.equals(SW360SparseRelease.class)) {
            return ReporterUtils.printCollectionOfSparseReleases((Collection<SW360SparseRelease>) result);
        } else {
            return new String[]{};
        }
    }

    private String getHeader() {
        if (resultType.equals(SW360Release.class)) {
            return ReporterUtils.releaseCsvPrintHeader();
        } else if (resultType.equals(SW360SparseRelease.class)) {
            return ReporterUtils.sparseReleaseCsvPrintHeader();
        } else {
            return "";
        }
    }

    /**
     * Prints a csv file with a given name to a given target directory.
     * Header and body are written
     *
     * @param header header columns used for the csv file
     * @param body   rows used for the csv file
     */
    private void printCsvFile(String header, String[] body) {
        if (header.split(";").length != Arrays.stream(body).findAny().map(s -> s.split(";").length).orElse(0)) {
            LOGGER.error("Number of header columns does not equal columns of body for the csv file.");
        }
        if (!filePath.endsWith(".csv")) {
            LOGGER.warn("CSV file {} does not have the correct file extension", filePath);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(filePath);
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
