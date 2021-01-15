/*
 * Copyright (c) Bosch.IO GmbH 2021.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
/*
 * Copyright (c) Bosch.IO GmbH 2021.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import org.eclipse.sw360.antenna.workflow.stubs.DefaultPolicyEvaluation;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AntennaWorkflowTest extends AntennaTestWithMockedContext {

    private AntennaWorkflowConfiguration antennaWFConfigMock;

    /**
     * Creates a mock of an {@code AntennaWFConfig} with all workflow steps.
     * @param analyzer Analyzer to be returned by the getAnalyzers method
     */
    private void createAntennaWFConfigMock(DummyAnalyzer analyzer) {
        antennaWFConfigMock = mock(AntennaWorkflowConfiguration.class);
        when(antennaWFConfigMock.getAnalyzers()).thenReturn(Collections.singleton(analyzer));
        when(antennaWFConfigMock.getProcessors()).thenReturn(Collections.emptySet());
        when(antennaWFConfigMock.getGenerators()).thenReturn(Collections.emptySet());
        when(antennaWFConfigMock.getOutputHandlers()).thenReturn(Collections.emptyList());
    }

    @Test (expected = ExecutionException.class)
    public void failCausingResultWithNoArtifactsInAnalyzerBreaksBuild() {
        DummyAnalyzer analyzer = mock(DummyAnalyzer.class);
        WorkflowStepResult workflowStepResult = new WorkflowStepResult(Collections.emptySet());
        workflowStepResult.addFailCausingResults("Test",
                Collections.singleton(new DefaultPolicyEvaluation.DefaultEvaluationResult("", "", IEvaluationResult.Severity.FAIL, new Artifact())));
        when(analyzer.yield()).thenReturn(workflowStepResult);
        when(analyzer.getWorkflowItemName()).thenReturn("");
        createAntennaWFConfigMock(analyzer);

        AntennaWorkflow workflow = new AntennaWorkflow(antennaWFConfigMock);

        workflow.execute();
    }

    @Test
    public void noArtifactsInAnalyzerBreaksBuild() {
        DummyAnalyzer analyzer = mock(DummyAnalyzer.class);
        WorkflowStepResult workflowStepResult = new WorkflowStepResult(Collections.emptySet());
        when(analyzer.yield()).thenReturn(workflowStepResult);
        when(analyzer.getWorkflowItemName()).thenReturn("");
        createAntennaWFConfigMock(analyzer);

        AntennaWorkflow workflow = new AntennaWorkflow(antennaWFConfigMock);

        Map<String, IAttachable> result = workflow.execute();
        assertThat(result).isEmpty();
    }

    /**
     * Dummy class to create a dummy analyzer usable
     * for mocking purposes.
     */
    class DummyAnalyzer extends AbstractAnalyzer {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public WorkflowStepResult yield() throws ExecutionException {
            return null;
        }
    }
}