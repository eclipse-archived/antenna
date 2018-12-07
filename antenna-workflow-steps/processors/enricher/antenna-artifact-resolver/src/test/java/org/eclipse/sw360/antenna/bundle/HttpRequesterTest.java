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
package org.eclipse.sw360.antenna.bundle;

import org.apache.http.client.methods.HttpGet;
import org.codehaus.plexus.util.ReflectionUtils;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.util.HttpHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class HttpRequesterTest extends AntennaTestWithMockedContext {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ToolConfiguration toolConfigMock = Mockito.mock(ToolConfiguration.class);
    private HttpHelper httpHelperMock = Mockito.mock(HttpHelper.class);

    private HttpRequester hr;
    
    private MavenCoordinates mavenCoordinates;

    private boolean isSource;

    @Parameterized.Parameters(name = "{index}: isSource={0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{false}, {true}});
    }

    public HttpRequesterTest(boolean isSource) {
        this.isSource = isSource;
    }

    @Before
    public void before() throws Exception {
        String sourceRepositoryUrl = "http://test.repo" 
                + "/" + HttpRequester.GROUP_ID_PLACEHOLDER 
                + "/" + HttpRequester.ARTIFACT_ID_PLACEHOLDER 
                + "/" + HttpRequester.VERSION_PLACEHOLDER + "/";

        mavenCoordinates = new MavenCoordinates("artifactId", "groupId", "version");


        when(toolConfigMock.getSourcesRepositoryUrl())
                .thenReturn(sourceRepositoryUrl);
        when(toolConfigMock.getAntennaTargetDirectory())
                .thenReturn(temporaryFolder.newFolder("target").toPath());
        when(antennaContextMock.getToolConfiguration())
                .thenReturn(toolConfigMock);

        hr = new HttpRequester(antennaContextMock);
        ReflectionUtils.setVariableValueInObject(hr, "httpHelper", httpHelperMock);
    }
        
    @Test
    public void requestFileUsesTheCorrectUrl() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();

        hr.requestFile(mavenCoordinates, targetDirectory, isSource);

        String filename = "artifactId-version" + (isSource ? "-sources" : "") + ".jar";
        verify(httpHelperMock).downloadFile("http://test.repo/groupId/artifactId/version/" + filename, targetDirectory, filename);
    }

    @Test(expected = IOException.class)
    public void requestFilePassesThroughExceptions() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();
        when(httpHelperMock.downloadFile(anyString(), eq(targetDirectory), anyString())).thenThrow(new IOException("Failed to download"));

        hr.requestFile(mavenCoordinates, targetDirectory, isSource);
    }

    @Test
    public void requestFileDoesNotDownloadIfFileExists() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();
        String filename = "artifactId-version" + (isSource ? "-sources" : "") + ".jar";
        File expectedFile = new File(targetDirectory.toFile(), filename);

        new FileOutputStream(expectedFile).close();

        hr.requestFile(mavenCoordinates, targetDirectory, isSource);

        verify(httpHelperMock, never()).downloadFile(anyString(), eq(targetDirectory), anyString());
    }
}