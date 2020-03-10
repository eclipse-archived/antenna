/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven;

import org.eclipse.sw360.antenna.http.HttpClient;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.util.HttpHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.setVariableValueInObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class HttpRequesterTest extends AntennaTestWithMockedContext {
    private HttpHelper httpHelperMock = Mockito.mock(HttpHelper.class);
    private HttpRequester hr;
    private Coordinate mavenCoordinate;
    private ClassifierInformation classifierInformation;

    @Parameterized.Parameters(name = "{index}: isSource={0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{ClassifierInformation.DEFAULT_JAR}, {ClassifierInformation.DEFAULT_SOURCE_JAR}});
    }

    public HttpRequesterTest(ClassifierInformation classifierInformation) {
        this.classifierInformation = classifierInformation;
    }

    @Before
    public void before() throws Exception {
        this.baseBefore();
        mavenCoordinate = new Coordinate(Coordinate.Types.MAVEN, "groupId", "artifactId", "version");

        hr = new HttpRequester(mock(HttpClient.class), new URL("http://test.repo"));
        setVariableValueInObject(hr, "httpHelper", httpHelperMock);
    }

    @Test
    public void requestFileUsesTheCorrectUrl() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();

        hr.requestFile(mavenCoordinate, targetDirectory, classifierInformation);

        String filename = "artifactId-version" + (classifierInformation.isSource ? "-sources" : "") + ".jar";
        verify(httpHelperMock).downloadFile("http://test.repo/groupId/artifactId/version/" + filename, targetDirectory, filename);
    }

    @Test
    public void requestFileDealsWithExceptionReturningAnEmptyRequest() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();
        when(httpHelperMock.downloadFile(anyString(), eq(targetDirectory), anyString())).thenThrow(new IOException("Failed to download"));

        Optional<File> file = hr.requestFile(mavenCoordinate, targetDirectory, classifierInformation);
        assertThat(file).isEmpty();
    }

    @Test
    public void requestFileDoesNotDownloadIfFileExists() throws Exception {
        Path targetDirectory = temporaryFolder.newFolder("target").toPath();
        String filename = "artifactId-version" + (classifierInformation.isSource ? "-sources" : "") + ".jar";
        File expectedFile = new File(targetDirectory.toFile(), filename);

        new FileOutputStream(expectedFile).close();

        hr.requestFile(mavenCoordinate, targetDirectory, classifierInformation);

        verify(httpHelperMock, never()).downloadFile(anyString(), eq(targetDirectory), anyString());
    }
}