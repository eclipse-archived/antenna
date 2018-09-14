/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors.filter;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.junit.After;
import org.junit.Before;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;

public abstract class CommonConfigurationHandlerTest extends AntennaTestWithMockedContext {

    protected List<Artifact> artifacts = new ArrayList<>();
    protected Artifact specialArtifact;
    protected String FILENAME = "filename";

    final protected MatchState artifactMatchState;

    public CommonConfigurationHandlerTest(MatchState artifactMatchState) {
        this.artifactMatchState = artifactMatchState;
    }


    @Before
    public void init(){
        ArtifactIdentifier identifier = new ArtifactIdentifier();
        identifier.setFilename(FILENAME);
        specialArtifact = new Artifact();
        specialArtifact.setArtifactIdentifier(identifier);
        if(artifactMatchState != null){
            specialArtifact.setMatchState(artifactMatchState);
        }

        // initialize dummy data
        Stream.of("Name1", "Name2", "Name3")
                .map(this::generateDummyArtifact)
                .forEach(artifacts::add);
    }

    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(configMock, atLeast(0)).getIgnoreForSourceResolving();
        verify(configMock, atLeast(0)).getRemoveArtifact();
    }


    protected Artifact generateDummyArtifact(String name) {
        BundleCoordinates otherBundleCoordinates = new BundleCoordinates();
        otherBundleCoordinates.setBundleVersion("1.2");
        otherBundleCoordinates.setSymbolicName(name);
        ArtifactIdentifier otherArtifactIdentifier = new ArtifactIdentifier();
        otherArtifactIdentifier.setFilename(name);
        otherArtifactIdentifier.setBundleCoordinates(otherBundleCoordinates);
        Artifact otherArtifact = new Artifact();
        otherArtifact.setArtifactIdentifier(otherArtifactIdentifier);

        return otherArtifact;
    }
}
