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
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
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
    private static final String COORDINATE_TYPE = "Coordinate Type";
    private static final String OBSERVED_LICENSE_SHORT_NAME = "Observed License Short Name";
    private static final String OBSERVED_LICENSE_LONG_NAME = "Observed License Long Name";
    private static final String SOURCE_URL = "Source URL";
    private static final String RELEASE_ARTIFACT_URL = "Release Tag URL";
    private static final String SWH_URL = "Software Heritage URL";

    public CsvAnalyzer() {
        this.workflowStepOrder = 500;
    }

    @Override
    public WorkflowStepResult yield() throws AntennaException {
        List<Artifact> artifacts = new ArrayList<>();
        List<CSVRecord> records = getRecords();

        for (CSVRecord record : records) {
            String pathName = record.get(PATH_NAME);
            String absolutePathName = Paths.get(pathName).isAbsolute()
                    ? Paths.get(pathName).toString()
                    : baseDir.resolve(Paths.get(pathName)).toAbsolutePath().toString();

            Artifact artifact = new Artifact(getName())
                    .addFact(new DeclaredLicenseInformation(
                            createLicense(record.get(LICENSE_SHORT_NAME), record.get(LICENSE_LONG_NAME))))
                    .addFact(new ArtifactPathnames(absolutePathName))
                    .addFact(new ArtifactMatchingMetadata(MatchState.EXACT))
                    .addFact(createCoordinates(record));
            addOptionalArtifactFacts(record, artifact);

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

    private License createLicense(String short_name, String long_name) {
        License license = new License();
        license.setName(short_name);
        license.setLongName(long_name);

        return license;
    }

    private ArtifactCoordinates createCoordinates(CSVRecord record) {
        String type = record.isMapped(COORDINATE_TYPE) ? record.get(COORDINATE_TYPE) : "mvn";

        switch (type) {
            case "mvn":
                return new MavenCoordinates(record.get(NAME), record.get(GROUP), record.get(VERSION));
            case "dotnet":
                return new DotNetCoordinates(record.get(NAME), record.get(VERSION));
            case "javascript":
                return new JavaScriptCoordinates(record.get(NAME), record.get(GROUP), record.get(VERSION));
            case "bundle":
                return new BundleCoordinates(record.get(NAME), record.get(VERSION));
            default:
                return new GenericArtifactCoordinates(record.get(NAME), record.get(VERSION));
        }
    }

    private void addOptionalArtifactFacts(CSVRecord record, Artifact artifact) {
        if (record.isMapped(OBSERVED_LICENSE_SHORT_NAME) && record.isMapped(OBSERVED_LICENSE_LONG_NAME)) {
            artifact.addFact(new ObservedLicenseInformation(
                    createLicense(record.get(OBSERVED_LICENSE_SHORT_NAME), record.get(OBSERVED_LICENSE_LONG_NAME))));
        }
        if (record.isMapped(SOURCE_URL)) {
            artifact.addFact(new ArtifactSourceUrl(record.get(SOURCE_URL)));
        }
        if (record.isMapped(RELEASE_ARTIFACT_URL)) {
            artifact.addFact(new ArtifactReleaseTagURL(record.get(RELEASE_ARTIFACT_URL)));
        }
        if (record.isMapped(SWH_URL)) {
            artifact.addFact(new ArtifactSoftwareHeritageURL(record.get(SWH_URL)));
        }
    }
}
