/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.service.ServiceFactory;
import org.eclipse.sw360.antenna.api.workflow.ConfigurableWorkflowItem;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfigurationFactory;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils.mapPropertiesFile;

public class SW360Configuration extends ConfigurableWorkflowItem {
    private final SW360ConnectionConfigurationFactory connectionFactory;
    private final Map<String, String> properties;
    private final Path csvFilePath;
    private final SW360Connection connection;
    private final Path sourcesPath;
    private final Path baseDir;
    private final ServiceFactory serviceFactory;

    public SW360Configuration(File propertiesFile) {
        this(propertiesFile, new SW360ConnectionConfigurationFactory(), new ServiceFactory());
    }

    /**
     * Creates a new instance of {@code SW360Configuration} with the path to
     * the properties file and the factory to create the SW360 connection. This
     * constructor is used for testing purposes.
     *
     * @param propertiesFile the file containing configuration properties
     * @param factory        the factory for the SW360 connection
     * @param serviceFactory the factory for creating service objects
     * @throws ConfigurationException if mandatory properties are missing
     */
    SW360Configuration(File propertiesFile, SW360ConnectionConfigurationFactory factory,
                       ServiceFactory serviceFactory) {
        connectionFactory = factory;
        this.serviceFactory = serviceFactory;
        properties = mapPropertiesFile(propertiesFile);
        baseDir = Paths.get(getProperty("basedir")).toAbsolutePath().normalize();
        sourcesPath = baseDir.resolve(getProperty("sourcesDirectory"));
        csvFilePath = baseDir.resolve(getProperty("csvFilePath"));
        connection = makeConnection();
    }

    private SW360Connection makeConnection() {
        Map<String, String> configMap = Stream.of(new String[][]{
                {"rest.server.url", getConfigValue("sw360restServerUrl", properties)},
                {"auth.server.url", getConfigValue("sw360authServerUrl", properties)},
                {"user.id", getConfigValue("sw360user", properties)},
                {"user.password", getConfigValue("sw360password", properties)},
                {"client.id", getConfigValue("sw360clientId", properties)},
                {"client.password", getConfigValue("sw360clientPassword", properties)},
                {"download.attachments", getConfigValue("sw360downloadSources", properties, "false")}})
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        boolean useProxy = getBooleanConfigValue("proxyUse");
        String proxyHost = properties.get("proxyHost");
        int proxyPort = Integer.parseInt(StringUtils.defaultIfEmpty(properties.get("proxyPort"), "-1"));

        return connectionFactory.createConnection(
                key -> getConfigValue(key, configMap),
                serviceFactory.createHttpClient(useProxy, proxyHost, proxyPort),
                ServiceFactory.getObjectMapper());
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getSourcesPath() {
        return this.sourcesPath;
    }

    public Path getCsvFilePath() {
        return csvFilePath;
    }

    /**
     * Returns the value of the configuration property with the given key. All
     * properties are considered mandatory; so if the property cannot be
     * resolved, an exception is thrown.
     *
     * @param key the key of the property
     * @return the value of the property (can be <strong>null</strong>)
     * @throws ConfigurationException if the property is not defined
     */
    public String getProperty(String key) {
        String value = properties.get(key);
        if (value == null) {
            throw new ConfigurationException("Missing mandatory configuration property: " + key);
        }
        return value;
    }

    public SW360Connection getConnection() {
        return connection;
    }

    public Boolean getBooleanConfigValue(String key) {
        return getBooleanConfigValue(key, properties);
    }

    /**
     * Logs basic configuration settings. This method can be used by the
     * different applications of the compliance tool to log status information.
     *
     * @param logger the logger to use
     */
    public void logConfiguration(Logger logger) {
        logger.info("Base directory: {}", getBaseDir());
        logger.info("Sources directory: {}", getSourcesPath());
        logger.info("CSV path: {}", getCsvFilePath());
    }

    SW360ConnectionConfigurationFactory getConnectionFactory() {
        return connectionFactory;
    }

    ServiceFactory getServiceFactory() {
        return serviceFactory;
    }
}
