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
package org.eclipse.sw360.antenna.frontend;

import org.eclipse.sw360.antenna.model.xml.generated.Workflow;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.util.TemplateRenderer;
import org.eclipse.sw360.antenna.workflow.AntennaWorkflowConfiguration;
import org.eclipse.sw360.antenna.workflow.WorkflowFileLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BasicConfigurationTest extends AntennaTestWithMockedContext {

    private Workflow workflow;

    @Before
    public void initializeTestProject() throws Exception {
        TemplateRenderer tr = new TemplateRenderer(Collections.emptyMap());
        workflow = WorkflowFileLoader.loadWorkflowFromClassPath(Optional.empty(), tr);

        when(toolConfigMock.getWorkflow()).thenReturn(workflow);
        when(toolConfigMock.getAntennaTargetDirectory()).thenReturn(temporaryFolder.newFolder("target").toPath());
    }

    @After
    public void after(){
        temporaryFolder.delete();

        verify(antennaContextMock, atLeast(0)).getToolConfiguration();
        verify(antennaContextMock, atLeast(0)).getConfiguration();
        verify(antennaContextMock, atLeast(0)).getProcessingReporter();
        verify(toolConfigMock, atLeast(0)).getWorkflow();

        // antenna-source-validator
        verify(configMock, atLeast(0)).getValidForMissingSources();
        verify(configMock, atLeast(0)).getValidForIncompleteSources();

        // antenna-security-issue-validator
        verify(configMock, atLeast(0)).getSecurityIssues();
        verify(configMock, atLeast(0)).getSuppressedSecurityIssues();

        // antenna-artifact-resolver
        verify(toolConfigMock, atLeast(0)).getDependenciesDirectory();
        verify(configMock, atLeast(0)).getIgnoreForSourceResolving();

        // antenna-license-resolver
        verify(configMock, atLeast(0)).getFinalLicenses();

        // antenna-child-jar-resolver
        verify(toolConfigMock, atLeast(0)).getAntennaTargetDirectory();

        // antenna-license-knowledgebase-resolver
        verify(toolConfigMock, atLeast(0)).getEncoding();

        // antenna-source-zip-generator
        verify(configMock, atLeast(0)).getPrefereP2();
    }

    @Test
    public void testThatParsingWorks() {
        assertNotNull(workflow);

//        assertNotNull(workflow.getAnalyzers());
//        assertTrue(workflow.getAnalyzers().getStep().size() > 0);

        assertNotNull(workflow.getProcessors());
        assertTrue(workflow.getProcessors().getStep().size() > 0);

        assertNotNull(workflow.getGenerators());
        assertTrue(workflow.getGenerators().getStep().size() > 0);
    }

    @Test
    public void testWorkflowInstantiation()
            throws Exception{
        AntennaWorkflowConfiguration twc = new AntennaWorkflowConfiguration(antennaContextMock);

        assertNotNull(twc);

        if (workflow.getAnalyzers() != null && workflow.getAnalyzers().getStep().size() > 0) {
            assertEquals(twc.getAnalyzers().size(), workflow.getAnalyzers().getStep()
                    .stream()
                    .filter(s ->!s.isDeactivated())
                    .collect(Collectors.toList())
                    .size());
        }
        assertEquals(twc.getProcessors().size(), workflow.getProcessors().getStep()
                .stream()
                .filter(s ->!s.isDeactivated())
                .collect(Collectors.toList())
                .size());
        assertEquals(twc.getGenerators().size(), workflow.getGenerators().getStep()
                .stream()
                .filter(s ->!s.isDeactivated())
                .collect(Collectors.toList())
                .size());
        if (workflow.getOutputHandlers() != null && workflow.getOutputHandlers().getStep().size() > 0) {
            assertEquals(twc.getOutputHandlers().size(), workflow.getOutputHandlers().getStep()
                    .stream()
                    .filter(s ->!s.isDeactivated())
                    .collect(Collectors.toList())
                    .size());
        }
    }
}
