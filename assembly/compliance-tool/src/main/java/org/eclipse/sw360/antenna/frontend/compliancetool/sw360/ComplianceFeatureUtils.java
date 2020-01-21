/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360;

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.csvreader.CSVReader;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ComplianceFeatureUtils {
    private ComplianceFeatureUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, String> mapPropertiesFile(File propertiesFile) {
        if (!propertiesFile.exists()) {
            throw new IllegalArgumentException("Cannot find " + propertiesFile.toString() + ". Please check the path.");
        }

        try (InputStream input = new FileInputStream(propertiesFile)) {
            Properties prop = new Properties();
            prop.load(input);

            return prop.entrySet().stream()
                    .collect(Collectors.toMap(
                            p -> p.getKey().toString(),
                            p -> p.getValue().toString()));
        } catch (IOException e) {
            throw new ConfigurationException("IO exception when reading properties file: " + e.getMessage());
        }
    }

    public static Collection<Artifact> getArtifactsFromCsvFile(Map<String, String> properties) {
        char delimiter = properties.get("delimiter").charAt(0);
        File csvFile = new File(properties.get("csvFilePath"));
        if (!csvFile.exists()) {
            throw new ConfigurationException("csvFile for " + csvFile.getName() + " could not be found");
        }
        Charset encoding = Charset.forName(properties.get("encoding"));

        return new CSVReader(csvFile.toPath(), encoding, delimiter, csvFile.getParentFile().toPath()).createArtifactsList();
    }
}
