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

import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class IArtifactRequesterTest extends AntennaTestWithMockedContext {

    IArtifactRequester artifactRequester = new IArtifactRequester(antennaContextMock) {
        @Override
        public File requestFile(ArtifactIdentifier identifier, Path targetDirectory, boolean isSource) throws IOException, ArtifactDoesNotExistException {
            return null;
        }
    };
    MavenCoordinates mavenCoordinates;

    @Before
    public void before() {
        mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setGroupId("groupId");
        mavenCoordinates.setArtifactId("artifactId");
        mavenCoordinates.setVersion("version");
    }

    @Test
    public void getExpectedJarBaseNameTest() throws Exception {
        final String expectedJarBaseName = artifactRequester.getExpectedJarBaseName(mavenCoordinates, false);
        assertThat(expectedJarBaseName, CoreMatchers.endsWith(MavenInvokerRequester.JAR_EXTENSION));
        assertThat(expectedJarBaseName, containsString(mavenCoordinates.getArtifactId()));
        assertThat(expectedJarBaseName, containsString(mavenCoordinates.getVersion()));
        assertThat(expectedJarBaseName, not(containsString("/")));
    }

    @Test
    public void getExpectedJarBaseNameTestSource() throws Exception {
        final String expectedJarBaseName = artifactRequester.getExpectedJarBaseName(mavenCoordinates, true);
        assertThat(expectedJarBaseName, CoreMatchers.endsWith(MavenInvokerRequester.SOURCES_JAR_EXTENSION));
        assertThat(expectedJarBaseName, containsString(mavenCoordinates.getArtifactId()));
        assertThat(expectedJarBaseName, containsString(mavenCoordinates.getVersion()));
        assertThat(expectedJarBaseName, not(containsString("/")));
    }

}