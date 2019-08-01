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
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.ort.resolver.OrtAnalyzerResultResolver;
import org.eclipse.sw360.antenna.ort.resolver.OrtScannerResultResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

public class OrtResultAnalyzer extends ManualAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrtResultAnalyzer.class);

    public OrtResultAnalyzer() {
        this.workflowStepOrder = 700;
    }

    @Override
    public WorkflowStepResult yield() throws AntennaException {
        try {
            return new WorkflowStepResult(createArtifactList(componentInfoFile));
        } catch (IOException e) {
            throw new AntennaException("Error reading or parsing the ort result yaml file: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "OrtResult";
    }

    Collection<Artifact> createArtifactList(File ortResultFile) throws IOException {
        Optional<String> extension = Optional.ofNullable(ortResultFile.getName())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1));

        ObjectMapper mapper;
        switch (extension.get()) {
            case "json": mapper = new ObjectMapper(); break;
            case "yml": mapper = new YAMLMapper(); break;
            default: throw new IOException("Ort Result File is not in a supported format.");
        }

        JsonNode ortResult = mapper.readTree(ortResultFile);
        LOGGER.debug("Create artifact list from Ort Result File");

        Optional<JsonNode> optionalScanResults = Optional.ofNullable(ortResult.get("scanner"))
                .filter(j -> !j.isNull())
                .map(v -> v.get("results").get("scan_results"));

        if (!ortResult.get("analyzer").isNull() &&
                ortResult.get("analyzer").get("result").get("packages") != null) {
            return getArtifactListFromAnalyzerResult(ortResult.get("analyzer").get("result").get("packages"), optionalScanResults);

        } else if (optionalScanResults.isPresent()) {
            return getArtifactListFromScanResult(optionalScanResults.get());
        }
        return Collections.emptyList();
    }

    private List<Artifact> getArtifactListFromAnalyzerResult(JsonNode ortPackages, Optional<JsonNode> optionalScanResults) {
        List<Artifact> artifacts = new ArrayList<>();

        Iterator<JsonNode> ortPackageIterator = ortPackages.elements();
        while (ortPackageIterator.hasNext()) {
            Optional.ofNullable(ortPackageIterator.next().get("package")).ifPresent(ortPackage -> {
                        Optional<JsonNode> scanResult = getScanResultWithId(ortPackage, optionalScanResults);
                        artifacts.add(mapAnalyzerArtifact(ortPackage, scanResult));
                    }
            );
        }
        return artifacts;
    }

    private List<Artifact> getArtifactListFromScanResult(JsonNode scanResults) {
        List<Artifact> artifacts = new ArrayList<>();

        Iterator<JsonNode> scanResultsIterator = scanResults.elements();
        while (scanResultsIterator.hasNext()) {
            artifacts.add(new OrtScannerResultResolver().apply(scanResultsIterator.next()));
        }
        return artifacts;
    }

    private Optional<JsonNode> getScanResultWithId(JsonNode ortPackage, Optional<JsonNode> scanResult) {
        JsonNode pkgId = ortPackage.get("id");

        return scanResult.flatMap(jsonNode -> StreamSupport.stream(jsonNode.spliterator(), false)
                .filter(currentScanResult -> currentScanResult.get("id").equals(pkgId))
                .findFirst());
    }

    private Artifact mapAnalyzerArtifact(JsonNode analyzerResult, Optional<JsonNode> scanResult) {
        Artifact artifact = new OrtAnalyzerResultResolver().apply(analyzerResult);

        scanResult.map(new OrtScannerResultResolver())
                .ifPresent(artifact::overrideWith);

        return artifact;
    }
}
