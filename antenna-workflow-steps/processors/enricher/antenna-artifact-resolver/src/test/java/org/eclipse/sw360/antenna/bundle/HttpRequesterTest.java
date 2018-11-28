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

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest(HttpClients.class)
public class HttpRequesterTest extends AntennaTestWithMockedContext {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private ToolConfiguration toolConfigMock = Mockito.mock(ToolConfiguration.class);
    
    @Mock
    private CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
    @Mock
    private CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
    @Mock
    private HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
    @Mock 
    private StatusLine statusLine = Mockito.mock(StatusLine.class);
    
    @Captor
    private ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    private HttpRequester hr = new HttpRequester(antennaContextMock);
    
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
    public void before() throws IOException {
        String sourceRepositoryUrl = "http://test.repo" 
                + "/" + HttpRequester.GROUP_ID_PLACEHOLDER 
                + "/" + HttpRequester.ARTIFACT_ID_PLACEHOLDER 
                + "/" + HttpRequester.VERSION_PLACEHOLDER + "/";
        
        mavenCoordinates = new MavenCoordinates("groupId", "artifactId", "version");

        PowerMockito.mockStatic(HttpClients.class);
        Mockito.when(HttpClients.createDefault()).thenReturn(httpClientMock);
        Mockito.when(toolConfigMock.getSourcesRepositoryUrl())
                .thenReturn(sourceRepositoryUrl);
        Mockito.when(toolConfigMock.getAntennaTargetDirectory())
                .thenReturn(temporaryFolder.newFolder("target").toPath());
        Mockito.when(antennaContextMock.getToolConfiguration())
                .thenReturn(toolConfigMock);
    }
        
    @Test
    public void requestFileTestThatRequestIsComposedCorrectly() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();
        final String expectedJarBaseName = hr.getExpectedJarBaseName(mavenCoordinates, isSource);
        File expectedJarFile = new File(targetDirectory.toFile(), expectedJarBaseName);
        
        Mockito.when(httpResponseMock.getStatusLine())
                .thenReturn(statusLine);
        Mockito.when(httpResponseMock.getStatusLine().getStatusCode())
                .thenReturn(HttpStatus.SC_OK);
        Mockito.when(httpResponseMock.getEntity())
                .thenReturn(httpEntityMock);
        Mockito.when(httpClientMock.execute(ArgumentMatchers.any(HttpGet.class)))
                .then((Answer<CloseableHttpResponse>) httpGetMock -> {
                    new FileOutputStream(expectedJarFile).close();
                    return httpResponseMock;
                });
        Mockito.when(httpEntityMock.getContent())
                .then((Answer<InputStream>) result -> new FileInputStream(expectedJarFile));

        File resultFile = hr.requestFile(mavenCoordinates, targetDirectory, isSource);
        Mockito.verify(httpClientMock).execute(captor.capture());
        Mockito.verify(httpEntityMock).getContent();
        
        assertThat(resultFile).isEqualTo(expectedJarFile);
        
        HttpGet response = captor.getValue();
        Iterator<Path> requestPathIterator = Paths.get(response.getURI().getPath()).iterator();
        
        assertThat(requestPathIterator.next().toString()).isEqualTo(mavenCoordinates.getGroupId());
        assertThat(requestPathIterator.next().toString()).isEqualTo(mavenCoordinates.getArtifactId());
        assertThat(requestPathIterator.next().toString()).isEqualTo(mavenCoordinates.getVersion());
        assertThat(requestPathIterator.next().toString()).isEqualTo(expectedJarBaseName);
    }
    
    @Test(expected = ArtifactDoesNotExistException.class)
    public void requestFileTestThatRecognizedNonExistingArtifact() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();

        Mockito.when(httpResponseMock.getStatusLine())
                .thenReturn(statusLine);
        Mockito.when(httpResponseMock.getStatusLine().getStatusCode())
                .thenReturn(HttpStatus.SC_OK);
        Mockito.when(httpClientMock.execute(ArgumentMatchers.any(HttpGet.class)))
                .thenReturn(httpResponseMock);
        
       hr.requestFile(mavenCoordinates, targetDirectory, isSource);
    }
    
    @Test(expected = ArtifactDoesNotExistException.class)
    public void requestFileTestThatHandlesReturnCodes() throws Exception {
        Path targetDirectory = toolConfigMock.getAntennaTargetDirectory();

        Mockito.when(httpResponseMock.getStatusLine())
                .thenReturn(statusLine);
        Mockito.when(httpResponseMock.getStatusLine().getStatusCode())
                .thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(httpClientMock.execute(ArgumentMatchers.any(HttpGet.class)))
                .thenReturn(httpResponseMock);
        
        hr.requestFile(mavenCoordinates, targetDirectory, isSource);
    }
}