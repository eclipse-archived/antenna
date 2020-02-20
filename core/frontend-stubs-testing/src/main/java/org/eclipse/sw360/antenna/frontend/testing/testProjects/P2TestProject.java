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

package org.eclipse.sw360.antenna.frontend.testing.testProjects;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class P2TestProject extends AbstractTestProjectWithExpectations implements ExecutableTestProject {

    public P2TestProject() {
        super();
    }

    @Override
    public List<String> getOtherFilesToCopy() {
        return Stream.of(
                "src/dependencies.csv",
                "additional_p2_resources/p2.index",
                "additional_p2_resources/artifacts.jar",
                "additional_p2_resources/artifacts.xml.xz",
                "additional_p2_resources/content.jar",
                "additional_p2_resources/content.xml.xz",
                "additional_p2_resources/features/some_feature_0.0.1.201902181544.jar",
                "additional_p2_resources/plugins/some_bundle_0.0.1.201902181544.jar")
                .collect(Collectors.toList());
    }

    @Override
    public String getExpectedProjectVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public String getExpectedToolConfigurationProductName() {
        return "Antenna EP2";
    }

    @Override
    public String getExpectedToolConfigurationProductFullName() {
        return "Antenna Example P2 Project";
    }

    @Override
    public String getExpectedToolConfigurationProductVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public List<String> getExpectedToolConfigurationConfigFiles() {
        List<String> fileList = super.getExpectedToolConfigurationConfigFiles();
        fileList.add("src" + File.separator + "antennaconf.xml");
        return fileList;
    }

    @Override
    public List<String> getExpectedToolConfigurationConfigFilesEndings() {
        List<String> endingsList = super.getExpectedToolConfigurationConfigFilesEndings();
        endingsList.add(File.separator + "src" + File.separator + "antennaconf.xml");
        return endingsList;
    }

    @Override
    public String getExpectedProjectArtifactId() {
        return "p2-example-project";
    }

    @Override
    public void assertExecutionResult(Path pathToTarget, Map<String, IAttachable> buildArtifacts, AntennaContext context) throws Exception {
        final Path antennaOutDir = pathToTarget.resolve("antenna");
        assertTrue(antennaOutDir.toFile().exists());

        final Path report = antennaOutDir.resolve("Antenna_3rdPartyAnalysisReport.txt");
        assertTrue(report.toFile().exists());

        final Path depsDir = antennaOutDir.resolve("dependencies");
        assertTrue(depsDir.toFile().exists());
    }

    @Override
    public Collection<String> getExpectedBuildArtifacts() {
        return Stream.of("artifact-information", "attribution-doc").collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getExpectedDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getExpectedP2Dependencies() {
        return Stream.of("some_bundle_0.0.1.201902181544").collect(Collectors.toSet());
    }
}
