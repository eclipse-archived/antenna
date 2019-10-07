/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017,2019.
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
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvAnalyzerImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvAnalyzerImpl.class);

    private static final String NAME = "Artifact Id";
    private static final String GROUP = "Group Id";
    private static final String VERSION = "Version";
    private static final String COORDINATE_TYPE = "Coordinate Type";
    private static final String EFFECTIVE_LICENSE = "Effective License";
    private static final String DECLARED_LICENSE = "Declared License";
    private static final String OBSERVED_LICENSE = "Observed License";
    private static final String COPYRIGHTS = "Copyrights";
    private static final String HASH = "Hash";
    private static final String SOURCE_URL = "Source URL";
    private static final String RELEASE_ARTIFACT_URL = "Release Tag URL";
    private static final String SWH_ID = "Software Heritage ID";
    private static final String CLEARING_STATE = "Clearing State";
    private static final String CHANGES_STATUS = "Change Status";
    private static final String CPE = "CPE";
    private static final String PATH_NAME = "File Name";
    private final String nameOfAnalyzer;
    private final Character delimiter;
    private final String csvFile;
    private final Charset encoding;
    private final Path baseDir;

    public CsvAnalyzerImpl(String nameOfAnalyzer, Character delimiter, File csvFile, Charset encoding, Path baseDir) {
        this.nameOfAnalyzer = nameOfAnalyzer;
        this.delimiter = delimiter;
        this.csvFile = csvFile.getAbsolutePath();
        this.encoding = encoding;
        this.baseDir = baseDir;
    }

    public List<Artifact> yield() throws AntennaException {
        List<Artifact> artifacts = new ArrayList<>();
        List<CSVRecord> records = getRecords();

        for (CSVRecord record : records) {
            Artifact newArtifact = createNewArtifact(record);

            if(artifactListContainsArtifact(artifacts, newArtifact)) {
                Artifact oldArtifact = getArtifactWithCoordinatesFromList(artifacts, newArtifact.askForAll(ArtifactCoordinates.class));
                oldArtifact.mergeWith(newArtifact);
            } else {
                artifacts.add(newArtifact);
            }
        }

        return artifacts;
    }

    private boolean artifactListContainsArtifact(List<Artifact> artifacts, Artifact newArtifact) {
        return artifacts.stream()
                .anyMatch(artifact ->
                        artifact.askForAll(ArtifactCoordinates.class)
                                .stream()
                                .anyMatch(artifactCoordinates ->
                                        newArtifact.askForAll(ArtifactCoordinates.class).stream()
                                                .anyMatch(artifactCoordinates1 -> artifactCoordinates1.equals(artifactCoordinates)))
                );
    }

    private Artifact getArtifactWithCoordinatesFromList(List<Artifact> artifacts, List<ArtifactCoordinates> coordinates) {
        return artifacts.stream()
                .filter(artifact ->
                        artifact.askForAll(ArtifactCoordinates.class).stream()
                                .anyMatch(coordinates1 -> coordinates.stream().anyMatch(coordinates1::equals)))
                .findFirst()
                .orElse(new Artifact());
    }

    private Artifact createNewArtifact(CSVRecord record) {
        Artifact artifact = new Artifact(nameOfAnalyzer)
                .addFact(createCoordinates(record))
                .addFact(new ArtifactMatchingMetadata(MatchState.EXACT));
        addOptionalArtifactFacts(record, artifact);
        return artifact;
    }

    private List<CSVRecord> getRecords() throws AntennaException {
        CSVFormat csvFormat = CSVFormat.DEFAULT;
        csvFormat = csvFormat.withFirstRecordAsHeader();
        csvFormat = csvFormat.withDelimiter(delimiter);
        String filename = csvFile;
        List<CSVRecord> records;

        try (FileInputStream fs = new FileInputStream(filename);
             InputStreamReader isr = new InputStreamReader(fs, encoding);
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

    private String makePathAbsolute(String pathName) {
        return Paths.get(pathName).isAbsolute()
                ? Paths.get(pathName).toString()
                : baseDir.resolve(Paths.get(pathName)).toAbsolutePath().toString();
    }

    private void addOptionalArtifactFacts(CSVRecord record, Artifact artifact) {
        if (record.isMapped(EFFECTIVE_LICENSE) && !record.get(EFFECTIVE_LICENSE).isEmpty()) {
            License license = new License.Builder()
                    .setLicenseId(record.get(EFFECTIVE_LICENSE))
                    .build();
            artifact.addFact(new OverriddenLicenseInformation(license));
        }
        if (record.isMapped(DECLARED_LICENSE) && !record.get(DECLARED_LICENSE).isEmpty()) {
            License license = new License.Builder()
                    .setLicenseId(record.get(DECLARED_LICENSE))
                    .build();
            artifact.addFact(new DeclaredLicenseInformation(license));
        }
        if (record.isMapped(OBSERVED_LICENSE) && !record.get(OBSERVED_LICENSE).isEmpty()) {
            License license = new License.Builder()
                    .setLicenseId(record.get(OBSERVED_LICENSE))
                    .build();
            artifact.addFact(new ObservedLicenseInformation(license));
        }
        if (record.isMapped(COPYRIGHTS) && !record.get(COPYRIGHTS).isEmpty()) {
            artifact.addFact(new CopyrightStatement(record.get(COPYRIGHTS)));
        }
        if (record.isMapped(HASH) && !record.get(HASH).isEmpty()) {
            artifact.addFact(new ArtifactFilename(null, record.get(HASH)));
        }
        if (record.isMapped(SOURCE_URL) && !record.get(SOURCE_URL).isEmpty()) {
            artifact.addFact(new ArtifactSourceUrl(record.get(SOURCE_URL)));
        }
        if (record.isMapped(RELEASE_ARTIFACT_URL) && !record.get(RELEASE_ARTIFACT_URL).isEmpty()) {
            artifact.addFact(new ArtifactReleaseTagURL(record.get(RELEASE_ARTIFACT_URL)));
        }
        if (record.isMapped(SWH_ID) && !record.get(SWH_ID).isEmpty()) {
            try {
                artifact.addFact(new ArtifactSoftwareHeritageID.Builder(record.get(SWH_ID)).build());
            } catch (AntennaException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        if (record.isMapped(CLEARING_STATE) && !record.get(CLEARING_STATE).isEmpty()) {
            artifact.addFact(new ArtifactClearingState(
                    ArtifactClearingState.ClearingState.valueOf(record.get(CLEARING_STATE))));
        }
        if (record.isMapped(CHANGES_STATUS) && !record.get(CHANGES_STATUS).isEmpty()) {
            artifact.addFact(new ArtifactChangeStatus(
                    ArtifactChangeStatus.ChangeStatus.valueOf(record.get(CHANGES_STATUS))));
        }
        if (record.isMapped(CPE) && !record.get(CPE).isEmpty()) {
            artifact.addFact(new ArtifactCPE(record.get(CPE)));
        }
        if (record.isMapped(PATH_NAME) && !record.get(PATH_NAME).isEmpty()) {
            String pathName = record.get(PATH_NAME);
            String absolutePathName = makePathAbsolute(pathName);
            artifact.addFact(new ArtifactPathnames(absolutePathName));
        }
    }
}
