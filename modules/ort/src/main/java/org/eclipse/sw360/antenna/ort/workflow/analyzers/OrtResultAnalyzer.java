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

import com.here.ort.model.AnalyzerRun;
import com.here.ort.model.OrtResult;
import com.here.ort.model.OutputFormatKt;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.ort.resolver.OrtResultArtifactResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            throw new AntennaException("Error parsing the ORT result file: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "OrtResult";
    }

    Collection<Artifact> createArtifactList(File ortResultFile) throws IOException {
        LOGGER.debug("Creating artifact list from ORT result file '" + ortResultFile + "'.");
        OrtResult result = OutputFormatKt.mapper(ortResultFile).readValue(ortResultFile, OrtResult.class);
        OrtResultArtifactResolver resolver = new OrtResultArtifactResolver(result);
        AnalyzerRun analyzerRun = Optional.ofNullable(result.getAnalyzer())
                .orElseThrow(() -> new IOException("No analyzer run found in ORT result file."));
        return analyzerRun.getResult().getPackages().stream()
                .map(p -> resolver.apply(p.getPkg()))
                .collect(Collectors.toSet());
    }
}
