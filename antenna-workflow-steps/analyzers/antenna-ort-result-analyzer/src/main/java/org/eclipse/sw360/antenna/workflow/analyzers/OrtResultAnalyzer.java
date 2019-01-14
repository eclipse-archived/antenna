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
package org.eclipse.sw360.antenna.workflow.analyzers;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.GenericArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrtResultAnalyzer extends ManualAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrtResultAnalyzer.class);

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

        JsonNode ortPackages = yamlContent.get("analyzer").get("result").get("packages");

        if (ortPackages == null) {
            return null;
        }

        boolean hasScannerResults = yamlContent.get("scanner").elements().hasNext();
        JsonNode scanResults = null;
        if (hasScannerResults) {
            scanResults = yamlContent.get("scanner").get("results").get("scan_results");
        }
        Iterator<JsonNode> ortPackageIterator = ortPackages.elements();
        while (ortPackageIterator.hasNext()) {
            Artifact artifact;
            if (hasScannerResults) {
                JsonNode ortPackage = ortPackageIterator.next().get("package");
                Optional<JsonNode> scanResult = getScanResultWithId(ortPackage, scanResults);
                if (scanResult.isPresent()) {
                    artifact = mapArtifact(ortPackage, scanResult.get());
                } else {
                    artifact = mapArtifact(ortPackage);
                }
            } else {
                artifact = mapArtifact(ortPackageIterator.next().get("package"));
            }
            artifacts.add(artifact);
        }
        return artifacts;
    }

    private Artifact mapArtifact(JsonNode ortPackage) {
        Artifact a = new Artifact("OrtResult")
                .addFact(mapFileName(ortPackage))
                .addFact(new DeclaredLicenseInformation(mapDeclaredLicenses("declared_licenses", ortPackage)))
                .addFact(new ArtifactSourceUrl(mapSourceUrl(ortPackage)));
        mapCoordinates(ortPackage.get("id").textValue()).ifPresent(a::addFact);
        return a;

    }

    private Artifact mapArtifact(JsonNode ortPackage, JsonNode scanResult) {
        Artifact a = mapArtifact(ortPackage);
        LicensePair artifactLicenseCopyrightInfo = mapObservedLicense(scanResult);
        a.addFact(new ObservedLicenseInformation(artifactLicenseCopyrightInfo.getLicenseInformation()))
                .addFact(artifactLicenseCopyrightInfo.getCopyrightStatement());
        return a;
    }

    private Optional<JsonNode> getScanResultWithId(JsonNode ortPackage, JsonNode scanResult) {
        JsonNode pkgId = ortPackage.get("id");

        return StreamSupport.stream(scanResult.spliterator(), false)
                .filter(currentScanResult -> currentScanResult.get("id").equals(pkgId))
                .findFirst();
    }

    private LicenseInformation mapDeclaredLicenses(String identifier, JsonNode ortPackage) {
        if (null != ortPackage) {
            if (ortPackage.get(identifier).isArray()) {
                Iterator<JsonNode> licenseIterator = ortPackage.get(identifier).elements();
                Collection<String> licenses = new ArrayList<>();
                while (licenseIterator.hasNext()) {
                    licenses.add(licenseIterator.next().textValue());
                }
                return LicenseSupport.mapLicenses(licenses);
            }
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

    private String mapSourceUrl(JsonNode ortPackage) {
        return ortPackage.get("source_artifact").get("url").textValue();
    }

    private ArtifactFilename mapFileName(JsonNode ortPackage) {
        JsonNode ortArtifact;
        if (ortPackage.get("source_artifact") != null
                && !ortPackage.get("source_artifact").get("url").textValue().equals("")) {
            ortArtifact = ortPackage.get("source_artifact");
        } else if (ortPackage.get("binary_artifact") != null
                && !ortPackage.get("binary_artifact").get("url").textValue().equals("")) {
            ortArtifact = ortPackage.get("binary_artifact");
        } else {
            return new ArtifactFilename(null);
        }
        String fileName = ortArtifact.get("url").textValue();
        String hash = ortArtifact.get("hash").textValue();
        String hashAlgorithm = ortArtifact.get("hash_algorithm").textValue();

        return new ArtifactFilename(fileName, hash, hashAlgorithm);
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
