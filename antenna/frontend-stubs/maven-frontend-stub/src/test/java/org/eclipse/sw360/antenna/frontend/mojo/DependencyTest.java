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

package org.eclipse.sw360.antenna.frontend.mojo;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Due to the maven-flatten-plugin and the structure of the repository, we need to declare all compile and runtime dependencies
 * which are needed by workflow-steps also in the pom.xml of the maven-frontend-plugin.
 *
 * If a dependency for any workflow-step is missing, this will result in runtime failure of the corresponding workflow step.
 * This test ensures that whenever a developer adds a new dependency to the dependency management section of the parent pom,
 * she must also add it to the pom.xml of the maven-frontend-stub.
 */
public class DependencyTest {
    private static final Path PARENT_POM = Paths.get(DependencyTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()).resolve("../../../../../pom.xml").normalize();
    private static final Path MAVEN_FRONTEND_POM = Paths.get(DependencyTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()).resolve("../../pom.xml").normalize();
    /*
     * List of Dependencies that explicitly do not need to be present in the pom.xml.
     * Those may only be dependencies which are neither needed by any workflow-step nor by Maven (e.g. Gradle dependencies)
     */
    private static final List<Dependency> ALLOWED_MISSING_DEPENDENCIES = Arrays.asList(
            dependency("org.gradle", "gradle-core"),
            dependency("org.gradle", "gradle-model-core"),
            dependency("org.gradle", "gradle-tooling-api"),
            dependency("org.gradle", "gradle-base-services"),
            dependency("org.gradle", "gradle-base-services-groovy"),
            dependency("org.gradle", "gradle-logging"),
            dependency("org.codehaus.groovy", "groovy")
    );

    @Test
    public void testAllNonTestingDependenciesInDependencyManagementPresentInMavenFrontendStubPom() throws IOException, XmlPullParserException {
        try (FileReader parentReader = new FileReader(PARENT_POM.toFile());
             FileReader mavenFrontendReader = new FileReader(MAVEN_FRONTEND_POM.toFile())) {

            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model parentModel = reader.read(parentReader);
            Model mavenFrontendModel = reader.read(mavenFrontendReader);
            List<Dependency> dependencyManagement = parentModel.getDependencyManagement().getDependencies();

            ensureParsingCorrectPoms(dependencyManagement);

            List<Dependency> mavenFrontendModelDependencies = mavenFrontendModel.getDependencies();

            List<Dependency> dependenciesMissing = dependencyManagement
                    .stream()
                    .filter(dependency -> !"test".equals(dependency.getScope()))
                    .filter(dependency -> isNotContainedIn(mavenFrontendModelDependencies, dependency))
                    .filter(dependency -> isNotContainedIn(ALLOWED_MISSING_DEPENDENCIES, dependency))
                    .collect(Collectors.toList());

            assertThat(dependenciesMissing).isEmpty();
        }
    }

    private void ensureParsingCorrectPoms(List<Dependency> dependencyManagement) {
        assertThat(dependencyManagement).isNotEmpty();  // Ensure that we are parsing the parent pom with dependency management
        assertThat(MAVEN_FRONTEND_POM.toString()).contains("maven-frontend-stub");  // Ensure we are parsing the `maven-frontend-stub` pom.
    }

    private boolean isNotContainedIn(List<Dependency> dependencies, Dependency dependency) {
        return dependencies
                .stream()
                .noneMatch(dep ->
                        dep.getGroupId().equals(dependency.getGroupId()) && dep.getArtifactId().equals(dependency.getArtifactId()));
    }

    private static Dependency dependency(String groupId, String artifactId) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        return dependency;
    }
}
