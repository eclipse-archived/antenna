/*
 * Copyright (c) Bosch Software Innovations GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils.mkSW360Release;

public class SW360ExporterCSVWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File csvFile;

    @Before
    public void setUp() throws IOException {
        csvFile = folder.newFile("csvTest.csv");
    }

    private String[] csvColumns = {
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
            "File Name"};

    @Test
    public void writeReleaseListToCSVFileTest() throws IOException {
        List<SW360Release> releases = new ArrayList<>();
        releases.add(mkSW360Release("test"));
        Map<String, String> coordinates = new HashMap<>();
        coordinates.put(Coordinate.Types.MAVEN, "pkg:maven/test/test1@1.2.3");
        coordinates.put(Coordinate.Types.P2, "pkg:p2/test/test1@1.2.3");
        releases.add(mkSW360Release("test1").setCoordinates(coordinates));
        SW360ExporterCSVWriter.writeReleasesToCsvFile(releases, csvFile);

        assertThat(csvFile.exists()).isTrue();

        CSVParser csvParser = SW360TestUtils.getCsvParser(csvFile);
        assertThat(csvParser.getRecords().size()).isEqualTo(3);
    }

    @Test
    public void writeSingleReleaseListToCSVFileTest() throws IOException {
        List<SW360Release> oneRelease = Collections.singletonList(mkSW360Release("test:test")
                .setCpeId("cpeId").setOverriddenLicense("Apache-2.0"));
        SW360ExporterCSVWriter.writeReleasesToCsvFile(oneRelease, csvFile);

        assertThat(csvFile.exists()).isTrue();

        CSVParser csvParser = SW360TestUtils.getCsvParser(csvFile);
        List<CSVRecord> records = csvParser.getRecords();
        assertThat(records.size()).isEqualTo(1);
        CSVRecord csvRecord = records.get(0);
        for (String csvColumn : csvColumns) {
            if (!csvColumn.equals("File Name")) {
                assertThat(csvRecord.get(csvColumn).isEmpty()).isFalse();
            }
        }
    }

    @Test
    public void writeEmptyReleaseListToCSVFileTest() throws IOException {
        List<SW360Release> emptyRelease = new ArrayList<>();
        SW360ExporterCSVWriter.writeReleasesToCsvFile(emptyRelease, csvFile);

        assertThat(csvFile.exists()).isTrue();

        CSVParser csvParser = SW360TestUtils.getCsvParser(csvFile);
        assertThat(csvParser.getHeaderMap().size()).isEqualTo(csvColumns.length);
        assertThat(csvParser.getRecordNumber()).isEqualTo(0);
    }

}
