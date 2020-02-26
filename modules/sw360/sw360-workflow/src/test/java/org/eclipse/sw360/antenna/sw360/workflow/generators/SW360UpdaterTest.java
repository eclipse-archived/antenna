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

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.SW360ProjectCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.FromXmlSW360ProjectCoordinates;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SW360UpdaterTest {

    @Mock
    protected AntennaContext antennaContextMock = mock(AntennaContext.class);
    @Mock
    protected ToolConfiguration toolConfigMock = mock(ToolConfiguration.class);
    @Mock
    protected Configuration configMock = mock(Configuration.class);
    private SW360Updater updater;

    @Before
    public void setUp() {
        updater = new SW360Updater();
        when(toolConfigMock.getProxyHost())
                .thenReturn("localhost");
        when(toolConfigMock.getProxyPort())
                .thenReturn(8080);

        FromXmlSW360ProjectCoordinates coordinates = new FromXmlSW360ProjectCoordinates();
        final String projectName = "projectName";
        coordinates.setName(projectName);
        final String version = "version";
        coordinates.setVersion(version);
        when(configMock.getConfiguredSW360Project())
                .thenReturn(new SW360ProjectCoordinates(coordinates));

        when(antennaContextMock.getToolConfiguration())
                .thenReturn(toolConfigMock);
        when(antennaContextMock.getConfiguration())
                .thenReturn(configMock);
    }

    @Test
    public void testConfigure() {
        updater.setAntennaContext(antennaContextMock);

        Map<String, String> configMap = Stream.of(new String[][]{
                {"rest.server.url", ""},
                {"auth.server.url", ""},
                {"user.id", ""},
                {"user.password", ""},
                {"client.id", ""},
                {"client.password", ""},
                {"upload_sources", "true"},
                {"update_releases", "true"},
                {"proxy.use", ""}})
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));

        updater.configure(configMap);

        verify(configMock,times(1)).getConfiguredSW360Project();
        verify(toolConfigMock, times(1)).getProxyHost();
        verify(toolConfigMock, times(1)).getProxyPort();
    }

    @Test
    public void testProduce() {
        SW360UpdaterImpl updaterImpl = mock(SW360UpdaterImpl.class);
        when(updaterImpl.produce(Collections.emptySet()))
                .thenReturn(Collections.emptyMap());
        updater.setUpdaterImpl(updaterImpl);
        final Map<String, IAttachable> releases = updater.produce(Collections.emptySet());

        assertThat(releases).isEmpty();
    }
}
