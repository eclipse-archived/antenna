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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvAnalyzer extends ManualAnalyzer {
    private static final String NAME = "Artifact Id";
    private static final String GROUP = "Group Id";
    private static final String VERSION = "Version";
    private static final String LICENSE_SHORT_NAME = "License Short Name";
    private static final String LICENSE_LONG_NAME = "License Long Name";
    private static final String PATH_NAME = "File Name";

    public CsvAnalyzer() {
        this.workflowStepOrder = 500;
    }

    @Override
    public WorkflowStepResult yield() throws AntennaException {
        List<Artifact> artifacts = new ArrayList<>();
        List<CSVRecord> records = getRecords();

        for (CSVRecord record : records) {
            MavenCoordinates coordinates = new MavenCoordinates(record.get(NAME), record.get(GROUP), record.get(VERSION));

            License license = new License();
            license.setName(record.get(LICENSE_SHORT_NAME));
            license.setLongName(record.get(LICENSE_LONG_NAME));

            String pathName = record.get(PATH_NAME);
            String absolutePathName = Paths.get(pathName).isAbsolute()
                    ? Paths.get(pathName).toString()
                    : baseDir.resolve(Paths.get(pathName)).toAbsolutePath().toString();

            Artifact artifact = new Artifact(getName())
                    .addFact(coordinates)
                    .addFact(new DeclaredLicenseInformation(license))
                    .addFact(new ArtifactPathnames(absolutePathName))
                    .addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

            artifacts.add(artifact);
        }

        return new WorkflowStepResult(artifacts, true);
    }

    private List<CSVRecord> getRecords() throws AntennaException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        String filename = componentInfoFile.getAbsolutePath();
        List<CSVRecord> records;
        ToolConfiguration toolConfig = context.getToolConfiguration();

        try (FileInputStream fs = new FileInputStream(filename);
             InputStreamReader isr = new InputStreamReader(fs, toolConfig.getEncoding());
             CSVParser csvParser = new CSVParser(isr, csvFormat)) {
            records = csvParser.getRecords();
        } catch (FileNotFoundException e) {
            throw new AntennaException(
                    "Antenna is configured to read a CSV configuration file (" + filename + "), but the file wasn't found",
                    e);
        } catch (IOException e) {
            throw new AntennaException("Error when attempting to parse CSV configuration file: " + filename, e);
        }

        return records;
    }

    @Override
    public String getName() {
        return "CSV";
    }
}
