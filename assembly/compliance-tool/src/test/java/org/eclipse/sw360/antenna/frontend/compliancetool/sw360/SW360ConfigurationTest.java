/*
 * Copyright (c) Bosch Software Innovations GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360;

import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ConfigurationTest {
    @Test(expected = IllegalArgumentException.class)
    public void testConfigurationWithNonExistentFile() {
        new SW360Configuration(new File("non-existent-file"));
    }

    @Test
    public void testConfigurationWithExporterPropertiesFile() {
        String propertiesFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).getPath();
        File propertiesFile = new File(propertiesFilePath);
        SW360Configuration configuration = new SW360Configuration(propertiesFile);
        assertThat(configuration.getCsvFile().getName()).isEqualTo("<example-path>");
        assertThat(configuration.getConnectionConfiguration().getSW360ReleaseClientAdapter()).isNotNull();
        assertThat(configuration.getConnectionConfiguration().getSW360ComponentClientAdapter()).isNotNull();
    }
}
