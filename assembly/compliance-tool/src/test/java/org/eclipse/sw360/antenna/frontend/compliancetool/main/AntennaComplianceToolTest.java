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
package org.eclipse.sw360.antenna.frontend.compliancetool.main;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter.SW360Exporter;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater.SW360Updater;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AntennaComplianceToolTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testMainInitWithExporter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchFieldException {
        Path propertiesFile = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).getFile());

        Object[] obj = {new SW360Exporter(), propertiesFile};
        Class<?>[] params = new Class[obj.length];
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] instanceof SW360Exporter) {
                params[i] = SW360Exporter.class;
            } else if (obj[i] != null) {
                params[i] = Path.class;
            }
        }

        String methodName = "init";

        AntennaComplianceTool antennaComplianceTool = new AntennaComplianceTool();
        Method initMethod = antennaComplianceTool.getClass().getDeclaredMethod(methodName, params);
        initMethod.setAccessible(true);
        SW360Exporter exporter = (SW360Exporter) initMethod.invoke(antennaComplianceTool, obj);

        Field connectionConfiguration = exporter.getClass().getDeclaredField("connectionConfiguration");
        connectionConfiguration.setAccessible(true);
        assertThat(((SW360ConnectionConfiguration) connectionConfiguration.get(exporter)).getSW360ReleaseClientAdapter()).isNotNull();
        assertThat(((SW360ConnectionConfiguration) connectionConfiguration.get(exporter)).getSW360ComponentClientAdapter()).isNotNull();

        Field csvFile = exporter.getClass().getDeclaredField("csvFile");
        csvFile.setAccessible(true);
        assertThat(((File) csvFile.get(exporter)).getName()).isNotEmpty();
    }

    @Test
    public void testMainInitWithUpdater() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Path propertiesFile = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).getFile());

        Object[] obj = {new SW360Updater(), propertiesFile};
        Class<?>[] params = new Class[obj.length];
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] instanceof SW360Updater) {
                params[i] = SW360Updater.class;
            } else if (obj[i] != null) {
                params[i] = Path.class;
            }
        }

        String methodName = "init";

        AntennaComplianceTool antennaComplianceTool = new AntennaComplianceTool();
        Method initMethod = antennaComplianceTool.getClass().getDeclaredMethod(methodName, params);
        initMethod.setAccessible(true);
        SW360Updater updater = (SW360Updater) initMethod.invoke(antennaComplianceTool, obj);

        Field configuration = updater.getClass().getDeclaredField("configuration");
        configuration.setAccessible(true);
        assertThat(((SW360Configuration) configuration.get(updater)).getProperties()).isNotNull();

        Field sw360updater = updater.getClass().getDeclaredField("updater");
        sw360updater.setAccessible(true);
        assertThat(sw360updater.get(updater).getClass().getDeclaredField("sw360MetaDataUpdater")).isNotNull();
    }

    @Test
    public void testMainFailsWithEmptyArgs() {
        exit.expectSystemExitWithStatus(1);
        AntennaComplianceTool.main(new String[]{});
    }

    @Test
    public void testMainFailsWithNonExistentComplianceToolArgs() throws IOException {
        exit.expectSystemExitWithStatus(1);
        File testFile = folder.newFile("test");
        AntennaComplianceTool.main(new String[]{"non-existent-option", testFile.getAbsolutePath()});
    }

    @Test
    public void testMainFailsWithNonExistentFilenameArgs() {
        exit.expectSystemExitWithStatus(1);
        AntennaComplianceTool.main(new String[]{"non-existent-option", "non-existent-file"});
    }
}
