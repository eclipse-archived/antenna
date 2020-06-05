package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

/**
 * An implementation of the {@link ReporterOutput} that
 * creates an output file in a csv format
 */
public class ReporterOutputCSV implements ReporterOutput {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReporterOutputCSV.class);

    @Override
    public void printFile(Collection result, SW360Configuration configuration) {
        String header = "";
        String[] body = {};
        final Object o = result.stream().findFirst().map(Object::getClass).orElse(null);
        if (o instanceof SW360Release) {
            header = new SW360Release().csvPrintHeader();
            body = ReporterUtils.printCollectionOfReleases(result);
        }
        printCsvFile(header, body, configuration.getCsvFileName(), configuration.getTargetDir());
    }

    /**
     * Prints a csv file with a given name to a given target directory.
     * Header and body are written
     * @param header header columns used for the csv file
     * @param body rows used for the csv file
     * @param csvFileName name of the csv file
     * @param targetDir target directory csv file is written to
     */
    private void printCsvFile(String header, String[] body, String csvFileName, Path targetDir) {
        if (header.split(";").length != Arrays.stream(body).findAny().map(s -> s.split(";").length).orElse(0)) {
            LOGGER.error("Number of header columns does not equal columns of body for the csv file.");
        }
        if (!csvFileName.endsWith(".csv")){
            LOGGER.warn("CSV file {} does not have the correct file extension", csvFileName);
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
