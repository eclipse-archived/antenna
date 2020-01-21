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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SW360UpdaterTest {
    @Mock
    public SW360Configuration configurationMock = mock(SW360Configuration.class);

    @Mock
    SW360UpdaterImpl updater = mock(SW360UpdaterImpl.class);

    @Before
    public void setUp() {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getPath();
        Map<String, String> propertiesMap = ComplianceFeatureUtils.mapPropertiesFile(new File(propertiesFilePath));
        when(configurationMock.getProperties()).
                thenReturn(propertiesMap);
        when(updater.artifactToReleaseInSW360(any()))
                .thenReturn(new SW360Release());
    }

    @Test
    public void testExecute() {
        SW360Updater sw360Updater = new SW360Updater();
        sw360Updater.setUpdater(updater);
        sw360Updater.setConfiguration(configurationMock);

        sw360Updater.execute();

        verify(updater, atLeast(1)).artifactToReleaseInSW360(any());
    }
}
