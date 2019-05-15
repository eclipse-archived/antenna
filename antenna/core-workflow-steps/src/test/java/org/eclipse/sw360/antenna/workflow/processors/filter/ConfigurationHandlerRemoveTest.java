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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConfigurationHandlerRemoveTest extends AntennaTestWithMockedContext {
    private List<Artifact> artifacts;
    private Artifact artifact;
    private ConfigurationHandlerRemove handler;

    @Before
    public void init(){
        artifacts = new ArrayList<>();
        artifact = new Artifact();
        artifact.addFact(new ArtifactFilename("name"));
        handler = new ConfigurationHandlerRemove(antennaContextMock);
        List<ArtifactSelector> selectors = new ArrayList<>();
        ArtifactSelector selector = new ArtifactFilename("name");
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
        artifact.setFlag(Artifact.IS_PROPRIETARY_FLAG_KEY, true);
        artifacts.add(artifact);
        handler.process(artifacts);
        assertThat(artifacts).isEmpty();
    }
}
