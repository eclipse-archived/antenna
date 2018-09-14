/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.analyzers;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.License; import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;

public class CsvAnalyzer extends ManualAnalyzer {
    private static final String NAME = "Artifact Id";
    private static final String GROUP = "Group Id";
    private static final String VERSION = "Version";
    private static final String LICENSE_SHORT_NAME = "License Short Name";
    private static final String LICENSE_LONG_NAME = "License Long Name";
    private static final String PATH_NAME = "File Name";

    @Override
    public WorkflowStepResult yield() throws AntennaExecutionException {
        List<Artifact> artifacts = new ArrayList<>();
        List<CSVRecord> records = getRecords();

        for (CSVRecord record : records) {
            MavenCoordinates coordinates = new MavenCoordinates();
            coordinates.setArtifactId(record.get(NAME));
            coordinates.setGroupId(record.get(GROUP));
            coordinates.setVersion(record.get(VERSION));

            ArtifactIdentifier identifier = new ArtifactIdentifier();
            identifier.setMavenCoordinates(coordinates);

            License license = new License();
            license.setName(record.get(LICENSE_SHORT_NAME));
            license.setLongName(record.get(LICENSE_LONG_NAME));

            String[] pathNames = new String[] { record.get(PATH_NAME) };
            pathNames = Arrays.stream(pathNames).map(path -> Paths.get(path)).map(path -> {
                if (path.isAbsolute()) {
                    return path.toString();
                } else {
                    return baseDir.resolve(path).toAbsolutePath().toString();
                }
            }).toArray(String[]::new);

            Artifact artifact = new Artifact();
            artifact.setDeclaredLicenses(license);
            artifact.setArtifactIdentifier(identifier);
            artifact.setPathnames(pathNames);
            artifact.setAnalysisSource(getName());
            artifact.setMatchState(MatchState.EXACT);

            artifacts.add(artifact);
        }

        return new WorkflowStepResult(artifacts);
    }

    private List<CSVRecord> getRecords() throws AntennaExecutionException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        String filename = componentInfoFile.getAbsolutePath();
        Reader fileReader;
        CSVParser csvParser = null;
        List<CSVRecord> records;
        ToolConfiguration toolConfig = context.getToolConfiguration();

        try {
            fileReader = new InputStreamReader(new FileInputStream(filename), toolConfig.getEncoding());
            csvParser = new CSVParser(fileReader, csvFormat);
            records = csvParser.getRecords();
        } catch (FileNotFoundException e) {
            throw new AntennaExecutionException(
                    "Antenna is configured to read a CSV configuration file (" + filename + "), but the file wasn't found",
                    e);
        } catch (IOException e) {
            throw new AntennaExecutionException("Error when attempting to parse CSV configuration file: " + filename, e);
        } finally {
            IOUtils.closeQuietly(csvParser);
        }

        return records;
    }

    @Override
    public String getName() {
        return "CSV";
    }
}
