/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.ort.workflow.analyzers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.util.LicenseSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.StreamSupport;

public class OrtResultAnalyzer extends ManualAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrtResultAnalyzer.class);

    public OrtResultAnalyzer() {
        this.workflowStepOrder = 700;
    }

    static private Optional<ArtifactCoordinates> mapCoordinates(String ortIdentifier) {
        String[] ortIdentifierSeparate = ortIdentifier.split(":");
        if (ortIdentifierSeparate.length < 3) {
            return Optional.empty();
        }
        String name = ortIdentifierSeparate[ortIdentifierSeparate.length - 2];
        String version = ortIdentifierSeparate[ortIdentifierSeparate.length - 1];
        switch (ortIdentifierSeparate[0]) {
            // possible to add any kind of coordinate here that antenna and ort share
            case "nuget":
            case "dotnet":
                return Optional.of(mapDotNetCoordinates(name, version));
            case "Maven":
                return Optional.of(mapMavenCoordinates(ortIdentifierSeparate[1], name, version));
            case "NPM":
                return Optional.of(mapJavaScriptCoordinates(name, version));
            default:
                return Optional.of(mapSimpleCoordinates(name, version));
        }
    }

    static private ArtifactCoordinates mapDotNetCoordinates(String name, String version) {
        DotNetCoordinates.DotNetCoordinatesBuilder c = new DotNetCoordinates.DotNetCoordinatesBuilder();
        c.setPackageId(name);
        c.setVersion(version);
        return c.build();
    }

    static private ArtifactCoordinates mapMavenCoordinates(String namespace, String name, String version) {
        MavenCoordinates.MavenCoordinatesBuilder c = new MavenCoordinates.MavenCoordinatesBuilder();
        c.setGroupId(namespace);
        c.setVersion(version);
        c.setArtifactId(name);
        return c.build();
    }

    static private ArtifactCoordinates mapJavaScriptCoordinates(String name, String version) {
        JavaScriptCoordinates.JavaScriptCoordinatesBuilder c = new JavaScriptCoordinates.JavaScriptCoordinatesBuilder();
        c.setName(name);
        c.setVersion(version);
        c.setArtifactId(name + "-" + version);
        return c.build();
    }

    static private ArtifactCoordinates mapSimpleCoordinates(String name, String version) {
        return new GenericArtifactCoordinates(name, version);
    }

    @Override
    public WorkflowStepResult yield() throws AntennaException {
        if (!componentInfoFile.getAbsolutePath().endsWith(".yml")) {
            throw new AntennaException("Ort Result File is not a yaml file.");
        }
        try (InputStream is = new FileInputStream(componentInfoFile.getAbsolutePath())) {
            return new WorkflowStepResult(createArtifactList(is));
        } catch (IOException e) {
            throw new AntennaException("Error reading or parsing the ort result yaml file: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "OrtResult";
    }

    Collection<Artifact> createArtifactList(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode yamlContent = mapper.readTree(is);
        LOGGER.debug("Create artifact list from input stream");

        List<Artifact> artifacts = new ArrayList<>();

        boolean hasAnalyzerResults = !yamlContent.get("analyzer").isNull();
        boolean hasScannerResults = !yamlContent.get("scanner").isNull();

        if (hasAnalyzerResults) {
            Optional<JsonNode> ortPackages = Optional.ofNullable(yamlContent.get("analyzer").get("result").get("packages"));
            if (!ortPackages.isPresent()) {
                if (hasScannerResults) {
                    return getArtifactListFromScanResult(yamlContent.get("scanner").get("results").get("scan_results"));
                }
                return null;
            }

            Optional<JsonNode> scanResults = Optional.empty();
            if (hasScannerResults) {
                scanResults = Optional.ofNullable(yamlContent.get("scanner").get("results").get("scan_results"));
            }
            Iterator<JsonNode> ortPackageIterator = ortPackages.get().elements();
            while (ortPackageIterator.hasNext()) {
                Artifact artifact;
                Optional<JsonNode> ortPackage = Optional.ofNullable(ortPackageIterator.next().get("package"));

                Optional<JsonNode> scanResult = getScanResultWithId(ortPackage, scanResults);
                artifact = mapArtifact(ortPackage, scanResult);
                artifacts.add(artifact);
            }
            return artifacts;

        } else if (hasScannerResults) {
            return getArtifactListFromScanResult(yamlContent.get("scanner").get("results").get("scan_results"));
        } else {
            return null;
        }
    }

    private List<Artifact> getArtifactListFromScanResult(JsonNode scanResults) {
        List<Artifact> artifacts = new ArrayList<>();
        Iterator<JsonNode> scanResultsIterator = scanResults.elements();
        while (scanResultsIterator.hasNext()) {
            artifacts.add(mapArtifact(Optional.empty(), Optional.of(scanResultsIterator.next())));
        }
        return artifacts;
    }

    private Optional<JsonNode> getScanResultWithId(Optional<JsonNode> ortPackage, Optional<JsonNode> scanResult) {
        Optional<JsonNode> pkgId = ortPackage.map(p -> p.get("id"));

        return scanResult.flatMap(jsonNode -> StreamSupport.stream(jsonNode.spliterator(), false)
                .filter(currentScanResult -> currentScanResult.get("id").equals(pkgId.get()))
                .findFirst());
    }

    private Artifact mapArtifact(Optional<JsonNode> ortPackage, Optional<JsonNode> scanResult) {
        Artifact a;
        if (ortPackage.isPresent()) {
            a = mapArtifactFromAnalyzerResult(ortPackage.get());
        } else if (scanResult.isPresent()) {
            a = mapArtifactFromScanResult(scanResult.get());
        } else {
            return new Artifact("OrtResult");
        }

        if (scanResult.isPresent()) {
            LicensePair artifactLicenseCopyrightInfo = mapObservedLicense(scanResult.get());
            a.addFact(new ObservedLicenseInformation(artifactLicenseCopyrightInfo.getLicenseInformation()))
                    .addFact(artifactLicenseCopyrightInfo.getCopyrightStatement());
        }

        return a;
    }

    private Artifact mapArtifactFromAnalyzerResult(JsonNode ortPackage) {
        Artifact a = new Artifact("OrtResult")
                .addFact(mapFileName(ortPackage))
                .addFact(new DeclaredLicenseInformation(mapDeclaredLicenses("declared_licenses", ortPackage)))
                .addFact(new ArtifactSourceUrl(mapSourceUrl(ortPackage)));
        mapCoordinates(ortPackage.get("id").textValue()).ifPresent(a::addFact);

        return a;
    }

    private Artifact mapArtifactFromScanResult(JsonNode scanResult) {
        Artifact a = new Artifact("OrtResult")
                .addFact(mapFileName(scanResult))
                .addFact(new ArtifactSourceUrl(mapSourceUrl(scanResult.get("results"))));
        mapCoordinates(scanResult.get("id").textValue()).ifPresent(a::addFact);

        return a;
    }

    private LicenseInformation mapDeclaredLicenses(String identifier, JsonNode ortPackage) {
        if (ortPackage.get(identifier).isArray()) {
            Iterator<JsonNode> licenseIterator = ortPackage.get(identifier).elements();
            Collection<String> licenses = new ArrayList<>();
            while (licenseIterator.hasNext()) {
                licenses.add(licenseIterator.next().textValue());
            }
            return LicenseSupport.mapLicenses(licenses);
        }
        return new LicenseStatement();
    }

    private LicensePair mapObservedLicense(JsonNode scanResult) {
        if (scanResult.get("results").isArray()) {
            Collection<String> licenses = new ArrayList<>();
            CopyrightStatement copyrights = new CopyrightStatement("");
            Iterator<JsonNode> results = scanResult.get("results").elements();
            while (results.hasNext()) {
                Iterator<JsonNode> licenseFindings =
                        results.next().get("summary").get("license_findings").elements();

                while (licenseFindings.hasNext()) {
                    JsonNode licenseFind = licenseFindings.next();
                    licenses.add(licenseFind.get("license").textValue());
                    Iterator copyrightIterator = licenseFind.get("copyrights").elements();
                    while (copyrightIterator.hasNext()) {
                        copyrights = copyrights.mergeWith(new CopyrightStatement(copyrightIterator.next().toString()));
                    }
                }
            }
            return new LicensePair(LicenseSupport.mapLicenses(licenses), copyrights);
        } else {
            return new LicensePair(new LicenseStatement(), new CopyrightStatement(""));
        }
    }

    private String mapSourceUrl(JsonNode ortItem) {
        if (ortItem.has("source_artifact")) {
            return ortItem.get("source_artifact").get("url").textValue();
        } else {
            Iterator<JsonNode> ortItemProvenanceIterator = ortItem.elements();
            if (ortItemProvenanceIterator.hasNext()) {
                JsonNode provenanceNode = ortItemProvenanceIterator.next().get("provenance");
                if (provenanceNode.has("vcs_info")) {
                    return provenanceNode.get("vcs_info").get("url").textValue();
                } else if (provenanceNode.has("source_artifact")) {
                    return provenanceNode.get("source_artifact").get("url").textValue();
                } else if (provenanceNode.has("original_vcs_info")) {
                    return provenanceNode.get("original_vcs_info").get("url").textValue();
                } else {
                    return "";
                }
            }
            return "";
        }
    }

    private ArtifactFilename mapFileName(JsonNode ortItem) {
        return getOrtArtifact(ortItem).map(ortArtifact -> {
            String fileName = ortArtifact.get("url").textValue();
            String hash = ortArtifact.get("hash").textValue();
            String hashAlgorithm = ortArtifact.get("hash_algorithm").textValue();
            return new ArtifactFilename(fileName, hash, hashAlgorithm);
        }).orElse(new ArtifactFilename(null));
    }

    private Optional<JsonNode> getOrtArtifact(JsonNode ortItem) {
        if (ortItem.get("source_artifact") != null
                && !ortItem.get("source_artifact").get("url").textValue().equals("")) {
            return Optional.ofNullable(ortItem.get("source_artifact"));
        } else if (ortItem.get("binary_artifact") != null
                && !ortItem.get("binary_artifact").get("url").textValue().equals("")) {
            return Optional.ofNullable(ortItem.get("binary_artifact"));
        } else if (ortItem.get("results") != null) {
            Iterator<JsonNode> ortItemIterator = ortItem.get("results").elements();
            while (ortItemIterator.hasNext()) {
                JsonNode ortResult = ortItemIterator.next();
                if (ortResult.get("provenance").has("source_artifact")) {
                    return Optional.of(ortResult.get("provenance").get("source_artifact"));
                }
            }
        }
        return Optional.empty();
    }

    private class LicensePair {
        private LicenseInformation li;
        private CopyrightStatement cs;

        private LicensePair(LicenseInformation licenseInformation, CopyrightStatement copyrightStatement) {
            this.li = licenseInformation;
            this.cs = copyrightStatement;
        }

        private LicenseInformation getLicenseInformation() {
            return this.li;
        }

        private CopyrightStatement getCopyrightStatement() {
            return this.cs;
        }
    }
}
