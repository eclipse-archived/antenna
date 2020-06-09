package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.apache.commons.csv.CSVParser;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterOutputCSVTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testPrint() throws IOException {
        Path csvFilePath = temporaryFolder.newFile("csvFile.csv").toPath();

        final ReporterOutput csvReporter = ReporterOutputFactory.getReporterOutput("csv");
        csvReporter.setFilePath(csvFilePath);
        csvReporter.setResultType(SW360SparseRelease.class);

        final SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease("test");
        csvReporter.print(Collections.singleton(sparseRelease));

        assertThat(csvFilePath).exists();
        final CSVParser csvParser = SW360TestUtils.getCsvParser(csvFilePath.toFile());
        assertThat(csvParser.getRecords()).hasSize(1);
    }
}
