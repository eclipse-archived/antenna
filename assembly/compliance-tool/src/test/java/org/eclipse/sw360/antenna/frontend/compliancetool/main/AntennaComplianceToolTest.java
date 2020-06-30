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
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter.*;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.updater.SW360Updater;
import org.eclipse.sw360.antenna.sw360.workflow.generators.SW360UpdaterImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AntennaComplianceToolTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static Object readField(Object source, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = source.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(source);
    }

    @Test
    public void testMainInitWithExporter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, URISyntaxException {
        Path propertiesFile = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).toURI());

        String methodName = "createExporter";

        AntennaComplianceTool antennaComplianceTool = new AntennaComplianceTool();
        Method initMethod = antennaComplianceTool.getClass().getDeclaredMethod(methodName, Path.class);
        initMethod.setAccessible(true);
        SW360Exporter exporter = (SW360Exporter) initMethod.invoke(antennaComplianceTool, propertiesFile);

        SW360Configuration configuration = (SW360Configuration) readField(exporter, "configuration");
        assertThat(configuration.getConnection()).isNotNull();
        assertThat(configuration.getProperties()).isNotNull();
    }

    @Test
    public void testMainInitWithUpdater() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, URISyntaxException {
        Path propertiesFile = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-updater.properties")).toURI());
        SW360Configuration configuration = new SW360Configuration(propertiesFile.toFile());

        String methodName = "createUpdater";

        AntennaComplianceTool antennaComplianceTool = new AntennaComplianceTool();
        Method initMethod = antennaComplianceTool.getClass().getDeclaredMethod(methodName, Path.class);
        initMethod.setAccessible(true);
        SW360Updater updater = (SW360Updater) initMethod.invoke(antennaComplianceTool, propertiesFile);

        assertThat(((SW360Configuration) readField(updater, "configuration")).getProperties()).isNotNull();

        SW360UpdaterImpl sw360updater = (SW360UpdaterImpl) readField(updater, AntennaComplianceToolOptions.MODE_NAME_UPDATER);
        assertThat(readField(sw360updater, "sw360MetaDataUpdater")).isNotNull();
        assertThat(sw360updater.isUpdateReleases())
                .isEqualTo(configuration.getBooleanConfigValue("sw360updateReleases"));
        assertThat(sw360updater.isUploadSources())
                .isEqualTo(configuration.getBooleanConfigValue("sw360uploadSources"));
        assertThat(sw360updater.isDeleteObsoleteSourceAttachments())
                .isEqualTo(configuration.getBooleanConfigValue("sw360deleteObsoleteSources"));

        assertThat(readField(updater, "generator")).isNotNull();
    }

    @Test
    public void testMainInitWithReporter() throws URISyntaxException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Path propertiesFile = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("compliancetool-exporter.properties")).toURI());
        Set<String> parameterSet = new HashSet<>();
        parameterSet.add("--info=" + new IRGetClearedReleases().getInfoParameter());

        String methodName = "createStatusReporter";

        AntennaComplianceTool antennaComplianceTool = new AntennaComplianceTool();
        Method initMethod = antennaComplianceTool.getClass().getDeclaredMethod(methodName, Path.class, Set.class);
        initMethod.setAccessible(true);
        SW360StatusReporter statusReporter = (SW360StatusReporter) initMethod.invoke(antennaComplianceTool, propertiesFile, parameterSet);

        assertThat(((SW360Configuration) readField(statusReporter, "configuration")).getProperties()).isNotNull();
        assertThat(((String) readField(statusReporter, "infoParameter"))).isEqualTo(new IRGetClearedReleases().getInfoParameter());
        assertThat(((InfoRequest) readField(statusReporter, "infoRequest"))).isExactlyInstanceOf(IRGetClearedReleases.class);
        assertThat(((ReporterOutput) readField(statusReporter, "reporterOutput"))).isExactlyInstanceOf(ReporterOutputCSV.class);
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
