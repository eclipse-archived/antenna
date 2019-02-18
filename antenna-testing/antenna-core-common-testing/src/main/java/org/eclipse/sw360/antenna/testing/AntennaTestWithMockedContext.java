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
package org.eclipse.sw360.antenna.testing;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class AntennaTestWithMockedContext {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @Mock
    protected AntennaContext antennaContextMock = mock(AntennaContext.class);
    @Mock
    protected ToolConfiguration toolConfigMock = mock(ToolConfiguration.class);
    @Mock
    protected Configuration configMock = mock(Configuration.class);
    @Mock
    protected IProcessingReporter reporterMock = mock(IProcessingReporter.class);
    @Mock
    protected Workflow workflowMock = mock(Workflow.class);

    @Before
    public void baseBefore() throws Exception {
        when(antennaContextMock.getToolConfiguration())
                .thenReturn(toolConfigMock);
        when(antennaContextMock.getConfiguration())
                .thenReturn(configMock);
        when(antennaContextMock.getProcessingReporter())
                .thenReturn(reporterMock);

        when(toolConfigMock.getWorkflow())
                .thenReturn(workflowMock);
    }

    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(antennaContextMock, atLeast(0)).getToolConfiguration();
        verify(antennaContextMock, atLeast(0)).getConfiguration();
        verify(antennaContextMock, atLeast(0)).getProcessingReporter();

        // assert that there were no unexpected interactions with the mocked objects
        verifyNoMoreInteractions(antennaContextMock);
        verifyNoMoreInteractions(toolConfigMock);
        verifyNoMoreInteractions(configMock);
        verifyNoMoreInteractions(reporterMock);
        verifyNoMoreInteractions(workflowMock);
    }
}
