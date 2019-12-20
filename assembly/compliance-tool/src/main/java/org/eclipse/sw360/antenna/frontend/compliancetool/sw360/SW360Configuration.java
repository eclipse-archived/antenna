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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360;

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.ConfigurableWorkflowItem;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SW360Configuration extends ConfigurableWorkflowItem {
    private final Map<String, String> properties;
    private final File csvFile;
    private final SW360ConnectionConfiguration connectionConfiguration;

    public SW360Configuration(File propertiesFile) {
        properties = mapPropertiesFile(propertiesFile);
        connectionConfiguration = makeConnectionConfiguration();
        csvFile = new File(properties.get("csvFilePath"));
    }

    public SW360ConnectionConfiguration makeConnectionConfiguration() {
        Map<String, String> configMap = Stream.of(new String[][]{
                {"rest.server.url", getConfigValue("sw360restServerUrl", properties)},
                {"auth.server.url", getConfigValue("sw360authServerUrl", properties)},
                {"user.id", getConfigValue("sw360user", properties)},
                {"user.password", getConfigValue("sw360password", properties)},
                {"client.id", getConfigValue("sw360clientId", properties)},
                {"client.password", getConfigValue("sw360clientPassword", properties)},
                {"download.attachments", getConfigValue("sw360downloadSources", properties, "false")},
                {"proxy.use", getConfigValue("proxyUse", properties)}})
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        return new SW360ConnectionConfiguration(
                key -> getConfigValue(key, configMap),
                key -> getBooleanConfigValue(key, configMap),
                properties.get("proxyHost"), Integer.parseInt(properties.get("proxyPort")));
    }

    public static Map<String, String> mapPropertiesFile(File propertiesFile) {
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

    public File getCsvFileName() {
        return csvFile;
    }

    public SW360ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }
}
