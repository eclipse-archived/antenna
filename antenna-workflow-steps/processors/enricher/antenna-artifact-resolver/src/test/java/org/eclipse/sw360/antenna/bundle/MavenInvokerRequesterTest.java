/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.bundle;

import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class MavenInvokerRequesterTest extends AntennaTestWithMockedContext {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private IProject projectMock = Mockito.mock(IProject.class);

    @Mock
    private DefaultInvoker defaultInvokerMock = Mockito.mock(DefaultInvoker.class);
    @Captor
    private
    ArgumentCaptor<InvocationRequest> captor = ArgumentCaptor.forClass(InvocationRequest.class);

    private MavenInvokerRequester mir;

    private ArtifactIdentifier artifactIdentifier;
    private MavenCoordinates mavenCoordinates;

    @Before
    public void before() throws IOException {
        artifactIdentifier = new ArtifactIdentifier();
        mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setGroupId("groupId");
        mavenCoordinates.setArtifactId("artifactId");
        mavenCoordinates.setVersion("version");
        artifactIdentifier.setMavenCoordinates(mavenCoordinates);

        System.setProperty("maven.home", temporaryFolder.newFolder("m2").toString());

        Mockito.when(projectMock.getBasedir())
                .thenReturn(temporaryFolder.newFile("projectBasedir"));
        Mockito.when(antennaContextMock.getProject())
                .thenReturn(projectMock);

        mir = new MavenInvokerRequester(antennaContextMock, defaultInvokerMock);
    }

    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        Mockito.verify(antennaContextMock, Mockito.atLeastOnce()).getProject();
    }

    @Test
    public void getPomFileSanityCheck() throws Exception {
        File pomFile = mir.getPomFileFromContext();
        assertThat(pomFile.toString(), endsWith(MavenInvokerRequester.POM_FILENAME));
    }

    private InvocationResult getDummyInvocationResult(int returncode){
        return new InvocationResult() {
            @Override
            public CommandLineException getExecutionException() {
                return null;
            }

            @Override
            public int getExitCode() {
                return returncode;
            }
        };
    }

    @Test
    public void requestFileTestThatRequestIsComposedCorrectly() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

        final String expectedJarBaseName = mir.getExpectedJarBaseName(mavenCoordinates, false);
        File expectedJarFile = new File(targetDirectory.toFile(), expectedJarBaseName);

        Mockito.when(defaultInvokerMock.execute(ArgumentMatchers.any(InvocationRequest.class)))
                .then((Answer<InvocationResult>) invocationOnMock -> {
                    // touch expected file
                    new FileOutputStream(expectedJarFile).close();

                    // return dummy result
                    return getDummyInvocationResult(0);
                });

        File resultFile = mir.requestFile(artifactIdentifier, targetDirectory, false);

        Mockito.verify(defaultInvokerMock).execute(captor.capture());

        assertThat(resultFile, is(expectedJarFile));
        InvocationRequest invocationRequest = captor.getValue();
        Collection<String> goals = invocationRequest.getGoals();
        assertThat(goals.size(), not(is(0)));

        assertThat(goals.stream()
                .filter(s -> s.contains(mavenCoordinates.getGroupId()))
                .count(), is(1L));
        assertThat(goals.stream()
                .filter(s -> s.contains(mavenCoordinates.getArtifactId()))
                .count(), is(1L));
        assertThat(goals.stream()
                .filter(s -> s.contains(mavenCoordinates.getVersion()))
                .count(), is(1L));
        assertThat(goals.stream()
                .filter(s -> s.contains(targetDirectory.toString()))
                .count(), is(1L));
    }

    @Test(expected = ArtifactDoesNotExistException.class)
    public void requestFileTestThatRecognizeNonExistingArtifact() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();
        Mockito.when(defaultInvokerMock.execute(ArgumentMatchers.any(InvocationRequest.class)))
                .thenReturn(getDummyInvocationResult(0));
        mir.requestFile(artifactIdentifier, targetDirectory, false);
    }

    @Test(expected = ArtifactDoesNotExistException.class)
    public void requestFileTestThatHandlesReturnCodes() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

        final String expectedJarBaseName = mir.getExpectedJarBaseName(mavenCoordinates, false);
        File expectedJarFile = new File(targetDirectory.toFile(), expectedJarBaseName);

        Mockito.when(defaultInvokerMock.execute(ArgumentMatchers.any(InvocationRequest.class)))
                .then((Answer<InvocationResult>) invocationOnMock -> {
                    // touch expected file
                    new FileOutputStream(expectedJarFile).close();

                    // return dummy result
                    return getDummyInvocationResult(1);
                });
        mir.requestFile(artifactIdentifier, targetDirectory, false);
    }
}