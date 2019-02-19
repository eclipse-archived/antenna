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

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.util.JsonReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class JsonAnalyzer extends ManualAnalyzer {
    private void validate(ToolConfiguration toolConfig) throws AntennaException {
        // Check that JSON file is present
        if (!componentInfoFile.exists()) {
            throw new AntennaExecutionException("Antenna is configured to read a JSON configuration file ("
                    + componentInfoFile.getAbsolutePath() + "), but the file wasn't found.");
        }

        // Check that JSON file contains valid JSON.
        try (FileInputStream fileInputStream = new FileInputStream(componentInfoFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, toolConfig.getEncoding())) {
            Jsoner.deserialize(inputStreamReader);
        } catch (JsonException e) {
            throw new AntennaException("Encountered a problem when trying to parse "
                    + componentInfoFile.getAbsolutePath() + ": " + e.getMessage());
        } catch (IOException e) {
            throw new AntennaException("Unexpected error: " + e.getMessage());
        }
    }


    @Override
    public WorkflowStepResult yield() throws AntennaException {
        ToolConfiguration toolConfig = context.getToolConfiguration();
        validate(toolConfig);

        final Path dependenciesDir = context.getToolConfiguration().getDependenciesDirectory();
        JsonReader jsonReader = new JsonReader(null, dependenciesDir, toolConfig.getEncoding());

        try (InputStream is = new FileInputStream(componentInfoFile)) {
            return new WorkflowStepResult(jsonReader.createArtifactsList(is));
        } catch (IOException e) {
            throw new AntennaException("Error opening the component information file: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "JSON";
    }
}
