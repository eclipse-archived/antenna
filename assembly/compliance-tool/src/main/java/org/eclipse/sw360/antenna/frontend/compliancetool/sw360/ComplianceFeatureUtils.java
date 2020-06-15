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
import org.eclipse.sw360.antenna.csvreader.CSVArtifactMapper;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ComplianceFeatureUtils {
    /**
     * Prefix to indicate a property value as environment variable reference.
     * Properties starting with this prefix are looked up in the environment.
     */
    private static final String VARIABLE_PREFIX = "${";
    private static final String VARIABLE_SUFFIX = "}";

    private ComplianceFeatureUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a {@code Properties} object to a String-based map applying the
     * given mapping function to the property values.
     *
     * @param properties  the source properties
     * @param valueMapper the mapping function for the property values
     * @return the resulting map
     */
    public static Map<String, String> propertiesToMap(Properties properties, UnaryOperator<String> valueMapper) {
        return properties.entrySet().stream()
                .collect(Collectors.toMap(
                        p -> p.getKey().toString(),
                        p -> valueMapper.apply(p.getValue().toString())));
    }

    public static Map<String, String> mapPropertiesFile(File propertiesFile) {
        if (!propertiesFile.exists()) {
            throw new IllegalArgumentException("Cannot find " + propertiesFile.toString() + ". Please check the path.");
        }

        try (InputStream input = new FileInputStream(propertiesFile)) {
            Properties prop = new Properties();
            prop.load(input);

            return propertiesToMap(prop, ComplianceFeatureUtils::mapEnvironmentVariable);
        } catch (IOException e) {
            throw new ConfigurationException("IO exception when reading properties file: " + e.getMessage());
        }
    }

    public static Collection<Artifact> getArtifactsFromCsvFile(Map<String, String> properties, Path csvFilePath) {
        char delimiter = properties.get("delimiter").charAt(0);
        File csvFile = csvFilePath.toFile();
        if (!csvFile.exists()) {
            throw new ConfigurationException("csvFile for " + csvFile.getName() + " could not be found");
        }
        Charset encoding = Charset.forName(properties.get("encoding"));

        return new CSVArtifactMapper(csvFile.toPath(), encoding, delimiter, csvFile.getParentFile().toPath()).createArtifactsList();
    }

    /**
     * Processes the value of a property and performs an environment lookup if
     * necessary. This function checks whether the given property value starts
     * with a specific prefix. If this is the case, the name (without the
     * prefix) is looked up in the current environment. If it can be resolved,
     * the corresponding environment variable value is returned. Otherwise, the
     * value is returned as is.
     *
     * @param value the value read from the properties file
     * @return the processed value
     */
    public static String mapEnvironmentVariable(String value) {
        if (value.startsWith(VARIABLE_PREFIX) &&
                value.endsWith(VARIABLE_SUFFIX)) {
            return Optional.ofNullable(System.getenv(value.substring(2, value.length() - 1)))
                    .orElse(value);
        }

        return value;
    }

    /**
     * Checks if a release is has an approved clearing state
     *
     * @param sw360Release release to be checked
     * @return true if approved, otherwise false
     */
    public static boolean isApproved(SW360Release sw360Release) {
        return Optional.ofNullable(sw360Release.getClearingState())
                .map(clearingState -> ArtifactClearingState.ClearingState.valueOf(clearingState) != ArtifactClearingState.ClearingState.INITIAL)
                .orElse(false) &&
                Optional.ofNullable(sw360Release.getSw360ClearingState())
                        .map(sw360ClearingState -> sw360ClearingState.equals(SW360ClearingState.APPROVED) ||
                                sw360ClearingState.equals(SW360ClearingState.REPORT_AVAILABLE))
                        .orElse(false);
    }
}
