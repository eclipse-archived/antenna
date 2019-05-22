/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.mojo;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.internal.DefaultLegacySupport;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.sw360.antenna.frontend.AbstractAntennaFrontendTest;
import org.eclipse.sw360.antenna.frontend.testProjects.AbstractTestProjectWithExpectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Properties;
import java.util.function.Supplier;

import static org.eclipse.sw360.antenna.frontend.testProjects.MavenTestProject.LOCAL_REPOSITORY_ROOT;

public abstract class AbstractAntennaMojoFrontendTest extends AbstractAntennaFrontendTest {
    private static final String ANTENNA_MOJO_GOAL = "analyze";

    @Rule
    public MojoRule mojoRule = new MojoRule();

    public AbstractAntennaMojoFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name) {
        super(testDataSupplier, name);
    }

    @Before
    public void setUp() throws Exception {
        File basedir = testData.getProjectRoot().toFile();

        final MavenSession mvnSession = setUpMavenSession(basedir);
        final MojoExecution mvnExecution = setUpMavenExecution(mvnSession);

        antennaFrontend = (AbstractAntennaMojoFrontend) mojoRule.lookupConfiguredMojo(mvnSession, mvnExecution);
        Assert.assertNotNull(antennaFrontend);

        antennaContext = antennaFrontend.init().buildAntennaContext();
        Assert.assertNotNull(antennaContext);
        Assert.assertNotNull(antennaContext.getProject());
        Assert.assertNotNull(antennaContext.getToolConfiguration());
        Assert.assertNotNull(antennaContext.getToolConfiguration().getAntennaTargetDirectory());

        runExecutionTest = true;
    }

    private MavenSession setUpMavenSession(File basedir) throws Exception {
        Properties userProperties = new Properties();
        userProperties.setProperty("file.separator", File.separator);

        MavenProject mvnProject = mojoRule.readMavenProject(basedir);
        Assert.assertNotNull(mvnProject);

        MavenSession mvnSession = mojoRule.newMavenSession(mvnProject);
        Assert.assertNotNull(mvnSession);
        setUpRepositorySession(mvnSession);
        mvnSession.getRequest().setUserProperties(userProperties);
        return mvnSession;
    }

    private void setUpRepositorySession(MavenSession mvnSession) throws NoLocalRepositoryManagerException {
        DefaultRepositorySystemSession repositorySession = (DefaultRepositorySystemSession) mvnSession.getProjectBuildingRequest().getRepositorySession();
        String baseDir = String.format("%s%s%s", this.testData.projectRoot, File.separator, LOCAL_REPOSITORY_ROOT);
        LocalRepository localRepository = new LocalRepository(baseDir);
        SimpleLocalRepositoryManagerFactory managerFactory = new SimpleLocalRepositoryManagerFactory();
        LocalRepositoryManager manager = managerFactory.newInstance(repositorySession, localRepository);
        repositorySession.setLocalRepositoryManager(manager);
    }

    private MojoExecution setUpMavenExecution(MavenSession mvnSession) {
        LegacySupport mvnBuildContext = new DefaultLegacySupport();
        mvnBuildContext.setSession(mvnSession);
        MojoExecution mvnExecution = mojoRule.newMojoExecution(ANTENNA_MOJO_GOAL);
        Assert.assertNotNull(mvnExecution);
        return mvnExecution;
    }

    @Test
    public void testThatAntennaPluginIsListed() {
        Assert.assertTrue(((MavenProject) antennaContext.getProject().getRawProject()).getBuild().getPlugins().stream()
                .anyMatch(p -> p.getArtifactId().equals("basic-maven-plugin")));
    }
}
