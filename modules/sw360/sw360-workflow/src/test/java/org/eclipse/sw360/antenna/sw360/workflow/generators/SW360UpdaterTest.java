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
package org.eclipse.sw360.antenna.sw360.workflow.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfigurationFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360UpdaterTest {

    private AntennaContext antennaContextMock;

    @Before
    public void setUp() {
        antennaContextMock = mock(AntennaContext.class);
        ToolConfiguration toolConfigMock = mock(ToolConfiguration.class);
        Configuration configMock = mock(Configuration.class);

        final String projectName = "projectName";
        final String version = "version";
        when(toolConfigMock.getProductFullName())
                .thenReturn(projectName);
        when(toolConfigMock.getVersion())
                .thenReturn(version);

        when(antennaContextMock.getToolConfiguration())
                .thenReturn(toolConfigMock);
        when(antennaContextMock.getConfiguration())
                .thenReturn(configMock);
    }

    private static Map<String, String> createConfigMap() {
        return Stream.of(new String[][]{
                {"rest.server.url", "https://sw360.org/api"},
                {"auth.server.url", "https://auth.sw360.org/token"},
                {"user.id", "scott"},
                {"user.password", "tiger"},
                {"client.id", "test-client"},
                {"client.password", "test-client-pwd"},
                {"upload_sources", "true"},
                {"update_releases", "true"},
                {"delete_obsolete_sources", "true"},
                {"proxy.use", "true"}})
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
    }

    @Test
    public void testDefaultConnectionFactoryIsCreated() {
        SW360Updater updater = new SW360Updater();

        assertThat(updater.getConnectionFactory()).isNotNull();
    }

    @Test
    public void testConfigure() {
        SW360ConnectionConfigurationFactory connectionFactory = mock(SW360ConnectionConfigurationFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        SW360Connection connection = mock(SW360Connection.class);
        when(antennaContextMock.getHttpClient()).thenReturn(httpClient);
        when(antennaContextMock.getObjectMapper()).thenReturn(mapper);
        when(connectionFactory.createConnection(any(), eq(httpClient), eq(mapper))).thenReturn(connection);

        SW360Updater updater = new SW360Updater(connectionFactory);
        updater.setAntennaContext(antennaContextMock);

        Map<String, String> configMap = createConfigMap();

        updater.configure(configMap);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<SW360ConnectionConfigurationFactory.Getter<String>> captor =
                ArgumentCaptor.forClass(SW360ConnectionConfigurationFactory.Getter.class);
        verify(connectionFactory).createConnection(captor.capture(), eq(httpClient), eq(mapper));
        SW360ConnectionConfigurationFactory.Getter<String> getter = captor.getValue();
        for (Map.Entry<String, String> e : configMap.entrySet()) {
            assertThat(getter.apply(e.getKey())).isEqualTo(e.getValue());
        }
        verify(connection).getReleaseAdapter();
    }

    @Test
    public void testCreateUpdaterImpl() {
        SW360ConnectionConfigurationFactory connectionFactory = mock(SW360ConnectionConfigurationFactory.class);
        SW360Connection connection = mock(SW360Connection.class);
        when(connectionFactory.createConnection(any(), any(), any())).thenReturn(connection);
        SW360Updater updater = new SW360Updater(connectionFactory);
        updater.setAntennaContext(antennaContextMock);

        SW360UpdaterImpl updaterImpl = updater.createUpdaterImpl(createConfigMap());
        assertThat(updaterImpl.isUpdateReleases()).isTrue();
        assertThat(updaterImpl.isUploadSources()).isTrue();
        assertThat(updaterImpl.isDeleteObsoleteSourceAttachments()).isTrue();
    }

    @Test
    public void testProduce() {
        Set<Artifact> artifacts = Collections.singleton(mock(Artifact.class));
        Map<String, IAttachable> updateResult = Collections.singletonMap("foo", mock(IAttachable.class));
        final SW360UpdaterImpl updaterImpl = mock(SW360UpdaterImpl.class);
        when(updaterImpl.produce(artifacts))
                .thenReturn(updateResult);
        SW360Updater updater = new SW360Updater() {
            @Override
            SW360UpdaterImpl createUpdaterImpl(Map<String, String> configMap) {
                return updaterImpl;
            }
        };
        updater.configure(createConfigMap());

        final Map<String, IAttachable> releases = updater.produce(artifacts);
        assertThat(releases).isEqualTo(updateResult);
    }
}
