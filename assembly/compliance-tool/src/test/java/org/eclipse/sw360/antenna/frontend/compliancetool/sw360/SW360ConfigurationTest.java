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

import org.eclipse.sw360.antenna.api.service.ServiceFactory;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfigurationFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SW360ConfigurationTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File configFile(String s) {
        String propertiesFilePath =
                Objects.requireNonNull(this.getClass().getClassLoader().getResource(s)).getPath();
        return new File(propertiesFilePath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigurationWithNonExistentFile() {
        new SW360Configuration(new File("non-existent-file"));
    }

    @Test
    public void testDefaultFactories() {
        SW360Configuration config = new SW360Configuration(configFile("compliancetool-exporter.properties"));

        assertThat(config.getConnectionFactory()).isNotNull();
        assertThat(config.getServiceFactory()).isNotNull();
    }

    @Test
    public void testConfigurationWithExporterPropertiesFile() {
        File propertiesFile = configFile("compliancetool-exporter.properties");
        SW360Configuration configuration = new SW360Configuration(propertiesFile);
        assertThat(configuration.getTargetDir()).isEqualTo(Paths.get("./"));
        assertThat(configuration.getCsvFilePath()).isEqualTo(Paths.get("sample.csv"));
        assertThat(configuration.getConnection().getReleaseAdapter()).isNotNull();
        assertThat(configuration.getConnection().getComponentAdapter()).isNotNull();
    }

    @Test
    public void testConfigurationWithUpdaterPropertiesFile() {
        File propertiesFile = configFile("compliancetool-updater.properties");
        SW360Configuration configuration = new SW360Configuration(propertiesFile);
        assertThat(configuration.getCsvFilePath().toFile().getName()).isEqualTo("compliancetool_updater_test.csv");
        assertThat(configuration.getProperties().get("delimiter")).isEqualTo(",");
        assertThat(configuration.getProperties().get("sw360updateReleases")).isEqualTo("true");
        assertThat(configuration.getProperties().get("sw360uploadSources")).isEqualTo("false");
    }

    @Test
    public void testConfigurationWithVariables() throws IOException {
        File file = folder.newFile("fake.properties");
        Map<String, String> envVars = System.getenv();
        List<String> propertyLines = envVars.keySet().stream()
                .map(s -> s + "=" + "${" + s + "}" + System.lineSeparator())
                .limit(5)
                .collect(Collectors.toList());
        Files.write(file.toPath(), propertyLines);

        Map<String, String> propertiesMap = ComplianceFeatureUtils.mapPropertiesFile(file);
        assertThat(propertiesMap.entrySet().stream()
                .allMatch(e -> e.getValue().equals(System.getenv(e.getKey())))).isTrue();
    }

    @Test
    public void testConfigurationWithUnknownEnvironmentVariables() {
        String key = "my.property";
        String value = "${non existing environment variable?!}";
        Properties properties = new Properties();
        properties.setProperty(key, value);

        Map<String, String> map = ComplianceFeatureUtils.propertiesToMap(properties, ComplianceFeatureUtils::mapEnvironmentVariable);
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.get(key)).isEqualTo(value);
    }

    @Test
    public void testConnection() {
        SW360ConnectionConfigurationFactory conFactory = mock(SW360ConnectionConfigurationFactory.class);
        ServiceFactory svcFactory = mock(ServiceFactory.class);
        SW360Connection connection = mock(SW360Connection.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(svcFactory.createHttpClient(true, "proxy.net", 8080))
                .thenReturn(httpClient);
        when(conFactory.createConnection(any(), eq(httpClient), eq(ServiceFactory.getObjectMapper())))
                .thenReturn(connection);
        File propertiesFile = configFile("config-with-proxy.properties");

        SW360Configuration configuration = new SW360Configuration(propertiesFile, conFactory, svcFactory);
        assertThat(configuration.getConnection()).isEqualTo(connection);
    }
}
