/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2017.
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.ArtifactSelector;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;

import static org.mockito.Mockito.verify;

public class ConfigurationHandlerRemoveTest extends AntennaTestWithMockedContext {
    private List<Artifact> artifacts;
    private Artifact artifact;
    private ArtifactIdentifier identifier;
    private List<ArtifactSelector> selectors;
    private ArtifactSelector selector;
    private ConfigurationHandlerRemove handler;

    @Before
    public void init(){
        artifacts = new ArrayList<>();
        artifact = new Artifact();
        identifier = new ArtifactIdentifier();
        identifier.setFilename("name");
        artifact.setArtifactIdentifier(identifier);
        handler = new ConfigurationHandlerRemove(antennaContextMock);
        selectors = new ArrayList<>();
        selector = new ArtifactSelector(identifier);
        selectors.add(selector);
        
        when(configMock.getRemoveArtifact())
                .thenReturn(selectors);
    }

    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(configMock, atLeast(0)).getRemoveArtifact();
    }

    @Test
    public void test() {
        artifacts.add(artifact);
        handler.process(artifacts);
        assertThat(artifacts).isEmpty();
    }

    @Test
    public void testProprietary() {
        artifact.setProprietary(true);
        artifacts.add(artifact);
        handler.process(artifacts);
        assertThat(artifacts).isEmpty();
    }
}
