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
package org.eclipse.sw360.antenna.frontend.testProjects;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public interface ExecutableTestProject {

    void assertExecutionResult(Path pathToTarget, Map<String, IAttachable> buildArtifacts, AntennaContext context) throws Exception;

    Collection<String> getExpectedBuildArtifacts();

    Collection<String> getExpectedDependencies();

    default void assertBuildArtifactsExistence(IProject project, Set<Map.Entry<String, IAttachable>> buildArtifacts) {
        //#############################################################################################################
        // asserts for build artifacts at `context.getBuildArtifacts()`
        final List<String> keys = buildArtifacts.stream()
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(getExpectedBuildArtifacts().stream().sorted().collect(Collectors.toList()), keys);
        buildArtifacts.stream()
                .map(Map.Entry::getValue)
                .map(IAttachable::getFile)
                .forEach(a -> {
                    assertTrue(a.exists());
                    assertTrue(a.length() > 100);
                });

        //#############################################################################################################
        // asserts for build artifacts at `context.getProject().getAttachedArtifacts()`

        Map<String,String> artifactTypeToClassifierMap = new HashMap<>();
        artifactTypeToClassifierMap.put("artifact-information", "antenna-artifact-info");
        artifactTypeToClassifierMap.put("disclosure-doc", "antenna-processing-report");
        artifactTypeToClassifierMap.put("sources-zip", "antenna-sources-zip");
        artifactTypeToClassifierMap.put("antenna-report", "antenna-disclosure-doc");
        artifactTypeToClassifierMap.put("disclosure-sw360-doc-html", "antenna-sw360-disclosure-doc");
        artifactTypeToClassifierMap.put("disclosure-sw360-doc-txt", "antenna-sw360-disclosure-doc");

        if(project.getRawProject() instanceof MavenProject){
            MavenProject mavenProject = (MavenProject) project.getRawProject();

            final List<Artifact> attachedArtifacts = mavenProject.getAttachedArtifacts();
            getExpectedBuildArtifacts().stream()
                    .map(artifactTypeToClassifierMap::get)
                    .forEach(expected -> assertTrue("artifacts at projects should conain artifact of classifier=["+expected+"], only "+attachedArtifacts.size()+ " attachments present",
                            attachedArtifacts.stream()
                                    .map(Artifact::getClassifier)
                                    .anyMatch(expected::equals))); // TODO: race condition
            attachedArtifacts.stream()
                    .map(Artifact::getFile)
                    .peek(Assert::assertNotNull)
                    .forEach(artifact -> assertTrue(buildArtifacts.stream()
                            .peek(Assert::assertNotNull)
                            .map(Map.Entry::getValue)
                            .map(IAttachable::getFile)
                            .anyMatch(artifact::equals)));
        }

    }

    default void assertDependenciesExistence(Path depsDir) {
        getExpectedDependencies().forEach(dep -> {
            File jar = depsDir.resolve(dep + ".jar").toFile();
            assertTrue(jar.toString() + " should exist", jar.exists());
            assertTrue(jar.length() > 1000);
            File srcJar = depsDir.resolve(dep + "-sources.jar").toFile();
            assertTrue(srcJar.toString() + "should exist", srcJar.exists());
            assertTrue(srcJar.length() > 1000);
        });
    }

    default void assertSourceZipContents(Set<Map.Entry<String, IAttachable>> buildArtifacts) throws IOException {
        if(!getExpectedBuildArtifacts().contains("sources-zip")){
            return;
        }

        final File sourceZipFile = buildArtifacts.stream()
                .filter(e -> "sources-zip".equals(e.getKey()))
                .map(Map.Entry::getValue)
                .map(IAttachable::getFile)
                .findAny().orElseThrow(() -> new RuntimeException("should not happen"));

        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            List<String> filesInZip = zipFile.stream()
                    .map(ZipEntry::getName)
                    .collect(Collectors.toList());

            getExpectedDependencies().forEach(jarname -> assertTrue("zip should contain the manifest for=["+jarname+"]",
                            filesInZip.contains(jarname+"/META-INF/MANIFEST.MF") ||
                                    filesInZip.contains(jarname+"-sources/META-INF/MANIFEST.MF")));
            assertTrue(filesInZip.size() > 10);
        }
    }
}
