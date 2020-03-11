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

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.csvreader.CSVArtifactMapper;
import org.eclipse.sw360.antenna.model.artifact.Artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
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

    public static Collection<Artifact> getArtifactsFromCsvFile(Map<String, String> properties) {
        char delimiter = properties.get("delimiter").charAt(0);
        File csvFile = new File(properties.get("csvFilePath"));
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
     * Reads in a properties file, replaces references to environment
     * variables, creates a (partially initialized) {@code AntennaContext}, and
     * returns the result as a {@code MappedConfigurationData} object. This
     * method bridges between the limited configuration of the compliance tool
     * and the configuration of Antenna. Although the compliance tool does not
     * need the full configuration settings supported by Antenna, it uses some
     * central services that need to be configured (e.g. the central HTTP
     * client). To make this possible, parts of the configuration for the
     * compliance tool are used to create an Antenna context, from which those
     * service objects can be obtained.
     *
     * @param propertiesFile the properties file with the configuration of the
     *                       compliance tool
     * @return the resulting {@code MappedConfigurationData} object
     */
    public static MappedConfigurationData createMappedConfigurationData(File propertiesFile) {
        Map<String, String> properties = mapPropertiesFile(propertiesFile);

        ToolConfiguration.ConfigurationBuilder configurationBuilder =
                new ToolConfiguration.ConfigurationBuilder();
        if (Boolean.parseBoolean(properties.get("proxyUse"))) {
            configurationBuilder.setProxyHost(properties.get("proxyHost"))
                    .setProxyPort(Integer.parseInt(properties.get("proxyPort")));
        }
        AntennaContext context = new AntennaContext.ContextBuilder()
                .setToolConfiguration(configurationBuilder.buildConfiguration())
                .buildContext();
        return new MappedConfigurationData(properties, context);
    }

    /**
     * A class representing configuration data for the compliance tool that has
     * already been processed.
     */
    public static class MappedConfigurationData {
        /**
         * A map with properties from the config file.
         */
        private final Map<String, String> properties;

        /**
         * A context that has been created from the configuration.
         */
        private final AntennaContext context;

        /**
         * Creates a new instance of {@code MappedConfigurationData} with the
         * given content.
         *
         * @param properties properties from the configuration file
         * @param context    a context created from the properties
         */
        public MappedConfigurationData(Map<String, String> properties, AntennaContext context) {
            this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
            this.context = context;
        }

        /**
         * Returns a (unmodifiable) map with properties that have been read
         * from the configuration file.
         *
         * @return a map with configuration properties
         */
        public Map<String, String> getProperties() {
            return properties;
        }

        /**
         * Returns a partial {@code AntennaContext} that was created from the
         * content of the configuration file. This object is not fully
         * initialized, as the compliance tool does not require a full Antenna
         * configuration. But it allows access to central service objects.
         *
         * @return the Antenna context created from the configuration
         */
        public AntennaContext getContext() {
            return context;
        }
    }
}
