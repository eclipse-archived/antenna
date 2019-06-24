/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.testing.testProjects;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract public class AbstractTestProject {

    private static final String POM = "pom.xml";

    public Path temporaryRoot;
    public Path projectRoot;

    List<String> defaultFilesToCopy = Stream.of(POM, "src/antennaconf.xml", "src/workflow.xml")
            .collect(Collectors.toList());

    public AbstractTestProject() {
        String projectResourcesRoot = "/" + getExpectedProjectArtifactId();
        try {
            init(projectResourcesRoot);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<String> getOtherFilesToCopy() {
        return Collections.emptyList();
    }

    public List<String> getOutOfProjectFilesToCopy() {
        return Collections.emptyList();
    }

    private void init(String projectResourcesRoot) throws IOException {
        this.temporaryRoot = Files.createTempDirectory(this.getClass().getName());
        this.projectRoot = Files.createDirectory(this.temporaryRoot.resolve(getExpectedProjectArtifactId()).normalize());

        copyFilesToProjectRoot(projectResourcesRoot, defaultFilesToCopy);
        copyFilesToProjectRoot(projectResourcesRoot, getOtherFilesToCopy());
        copyFilesToTemporaryRoot(projectResourcesRoot, getOutOfProjectFilesToCopy());
    }

    private void copyFilesToTemporaryRoot(String projectResourcesRoot, List<String> filesToCopy) throws IOException {
        for (String fileToCopy : filesToCopy) {
            try (InputStream resource = AbstractTestProject.class.getResourceAsStream(
                    Paths.get(projectResourcesRoot, fileToCopy).normalize().toString())) {
                if (resource != null) {
                    Path destination = projectRoot.resolve(fileToCopy).normalize();
                    destination.getParent().toFile().mkdirs();
                    Files.copy(resource, destination);
                }
            }
        }
    }

    private void copyFilesToProjectRoot(String projectResourcesRoot, List<String> filesToCopy) throws IOException {
        for (String fileToCopy : filesToCopy) {
            try (InputStream resource = AbstractTestProject.class.getResourceAsStream(projectResourcesRoot + "/" + fileToCopy)) {
                if (resource != null) {
                    Path destination = projectRoot.resolve(fileToCopy);
                    destination.getParent().toFile().mkdirs();
                    Files.copy(resource, destination);
                }
            }
        }
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public void cleanUpTemporaryProjectFolder() throws IOException {

        FileUtils.deleteDirectory(temporaryRoot.toFile());
    }


    public void addAndOverwriteFile(InputStream inputStream, String targetFileName) throws IOException {
        if (inputStream != null) {
            try {
                Files.copy(inputStream, getProjectRoot().resolve(targetFileName), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                inputStream.close();
            }
        }
    }

    public Path getProjectPom() {
        return getProjectRoot().resolve(POM);
    }

    abstract public String getExpectedProjectArtifactId();
}
