/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class SW360ExporterCSVWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360ExporterCSVWriter.class);

    static void writeReleasesToCsvFile(Collection<SW360Release> sw360ReleasesNotApproved, File csvFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile.toPath());
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                     "Artifact Id",
                     "Group Id",
                     "Version",
                     "Coordinate Type",
                     "Effective License",
                     "Declared License",
                     "Observed License",
                     "Copyrights",
                     "Hash",
                     "Source URL",
                     "Release Tag URL",
                     "Software Heritage ID",
                     "Clearing State",
                     "Change Status",
                     "CPE",
                     "File Name"))) {
            for (SW360Release sw360Release : sw360ReleasesNotApproved) {
                csvPrinter.printRecords(makeCsvRecordsFromRelease(sw360Release));
            }
            csvPrinter.flush();
        } catch (IOException e) {
            LOGGER.error("Error when writing the csv file:", e);
        }

    }

    private static Object[] makeCsvRecordsFromRelease(SW360Release sw360Release) {
        List<Object> csvRecordsByNamespaceAndHashes = new ArrayList<>();

        List<String> coordinateTypes = sw360Release.getCoordinates().values().stream()
                .map(SW360ExporterCSVWriter::getCoordinateTypeFromCoordinate)
                .collect(Collectors.toList());

        if (coordinateTypes.size() != 0) {

            if (sw360Release.getHashes() == null ||
                    sw360Release.getHashes().isEmpty()) {
                csvRecordsByNamespaceAndHashes.add(
                        makeCsvRecordFromRelease(sw360Release, "", coordinateTypes.get(0)));
            }
            for (String hash : sw360Release.getHashes()) {
                csvRecordsByNamespaceAndHashes.add(
                        makeCsvRecordFromRelease(sw360Release, hash, coordinateTypes.get(0)));
            }
            if (coordinateTypes.size() > 1) {
                coordinateTypes.remove(0);
                for (String coordinateType : coordinateTypes) {
                    SW360Release tempRelease = new SW360Release()
                            .setName(sw360Release.getName())
                            .setVersion(sw360Release.getVersion());
                    csvRecordsByNamespaceAndHashes.add(
                            makeCsvRecordFromRelease(tempRelease, "", coordinateType));
                }
            }
        } else {
            LOGGER.debug("{}:{} failed to write to csv file, since it does not have coordinates",
                    sw360Release.getName(),
                    sw360Release.getVersion());
        }
        return csvRecordsByNamespaceAndHashes.toArray();
    }

    private static Object[] makeCsvRecordFromRelease(SW360Release sw360Release, String hash, String coordinateType) {
        String[] name = separateReleaseName(sw360Release.getName());
        List<String> csvRecordString = new ArrayList<>();
        csvRecordString.add(name[0]);
        if (name.length > 1) {
            csvRecordString.add(name[1]);
        } else {
            csvRecordString.add("");
        }
        csvRecordString.add(Optional.ofNullable(sw360Release.getVersion()).orElse(""));
        csvRecordString.add(coordinateType);
        csvRecordString.add(Optional.ofNullable(sw360Release.getOverriddenLicense()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getDeclaredLicense()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getObservedLicense()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getCopyrights()).orElse(""));
        csvRecordString.add(hash);
        csvRecordString.add(Optional.ofNullable(sw360Release.getDownloadurl()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getReleaseTagUrl()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getSoftwareHeritageId()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getClearingState()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getChangeStatus()).orElse(""));
        csvRecordString.add(Optional.ofNullable(sw360Release.getCpeId()).orElse(""));
        // File Name does not exist in release object
        csvRecordString.add("");

        return csvRecordString.toArray();
    }

    private static String[] separateReleaseName(String name) {
        return name.split(":");
    }

    private static String getCoordinateTypeFromCoordinate(String coordinate) {
        Coordinate coordinate1 = new Coordinate(coordinate);
        return coordinate1.getType();
    }
}
