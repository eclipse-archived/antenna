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

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterOutputCSVTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testPrint() throws IOException {
        Path csvFilePath = temporaryFolder.newFile("csvFile.csv").toPath();
        final String delimiter = ";";

        final ReporterOutput csvReporter = ReporterOutputFactory.getReporterOutput("csv");
        csvReporter.setFilePath(csvFilePath);
        csvReporter.setResultType(SW360SparseRelease.class);

        final SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease("test");
        csvReporter.print(Collections.singleton(sparseRelease));

        assertThat(csvFilePath).exists();
        final CSVParser csvParser = SW360TestUtils.getCsvParser(csvFilePath.toFile(), ';');
        final List<CSVRecord> records = csvParser.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).size()).isEqualTo(ReporterUtils.sparseReleaseCsvPrintHeader(delimiter).split(delimiter).length);
    }
}
