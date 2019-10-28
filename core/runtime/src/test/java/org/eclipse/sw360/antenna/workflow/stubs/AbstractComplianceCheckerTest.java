/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.stubs;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class AbstractComplianceCheckerTest extends AntennaTestWithMockedContext {

    @Mock
    private IPolicyEvaluation evaluation = Mockito.mock(IPolicyEvaluation.class);

    private IEvaluationResult.Severity failOn;
    private Collection<IEvaluationResult.Severity> failedArtifactFailures;
    private boolean failureExpected;
    private AbstractComplianceChecker complianceChecker;

    @Parameterized.Parameters(name = "{index}: FailOn={0} with input={1} and expect={2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {IEvaluationResult.Severity.FAIL, Arrays.asList(IEvaluationResult.Severity.FAIL),                                                                   true},
                {IEvaluationResult.Severity.FAIL, Arrays.asList(IEvaluationResult.Severity.FAIL, IEvaluationResult.Severity.WARN),                                  true},
                {IEvaluationResult.Severity.FAIL, Arrays.asList(IEvaluationResult.Severity.FAIL, IEvaluationResult.Severity.WARN, IEvaluationResult.Severity.INFO), true},
                {IEvaluationResult.Severity.FAIL, Arrays.asList(                                 IEvaluationResult.Severity.WARN),                                  false},
                {IEvaluationResult.Severity.FAIL, Arrays.asList(                                 IEvaluationResult.Severity.WARN, IEvaluationResult.Severity.INFO), false},
                {IEvaluationResult.Severity.FAIL, Arrays.asList(                                                                  IEvaluationResult.Severity.INFO), false},
                {IEvaluationResult.Severity.FAIL, Arrays.asList(),                                                                                                  false},
                {IEvaluationResult.Severity.WARN, Arrays.asList(IEvaluationResult.Severity.FAIL),                                                                   true},
                {IEvaluationResult.Severity.WARN, Arrays.asList(IEvaluationResult.Severity.FAIL, IEvaluationResult.Severity.WARN),                                  true},
                {IEvaluationResult.Severity.WARN, Arrays.asList(IEvaluationResult.Severity.FAIL, IEvaluationResult.Severity.WARN, IEvaluationResult.Severity.INFO), true},
                {IEvaluationResult.Severity.WARN, Arrays.asList(                                 IEvaluationResult.Severity.WARN),                                  true},
                {IEvaluationResult.Severity.WARN, Arrays.asList(                                 IEvaluationResult.Severity.WARN, IEvaluationResult.Severity.INFO), true},
                {IEvaluationResult.Severity.WARN, Arrays.asList(                                                                  IEvaluationResult.Severity.INFO), false},
                {IEvaluationResult.Severity.WARN, Arrays.asList(),                                                                                                  false},
                {IEvaluationResult.Severity.INFO, Arrays.asList(IEvaluationResult.Severity.FAIL),                                                                   true},
                {IEvaluationResult.Severity.INFO, Arrays.asList(IEvaluationResult.Severity.FAIL, IEvaluationResult.Severity.WARN),                                  true},
                {IEvaluationResult.Severity.INFO, Arrays.asList(IEvaluationResult.Severity.FAIL, IEvaluationResult.Severity.WARN, IEvaluationResult.Severity.INFO), true},
                {IEvaluationResult.Severity.INFO, Arrays.asList(                                 IEvaluationResult.Severity.WARN),                                  true},
                {IEvaluationResult.Severity.INFO, Arrays.asList(                                 IEvaluationResult.Severity.WARN, IEvaluationResult.Severity.INFO), true},
                {IEvaluationResult.Severity.INFO, Arrays.asList(                                                                  IEvaluationResult.Severity.INFO), true},
                {IEvaluationResult.Severity.INFO, Arrays.asList(),                                                                                                  false}
        });
    }

    public AbstractComplianceCheckerTest(IEvaluationResult.Severity failOn, Collection<IEvaluationResult.Severity> failedArtifactFailures, boolean successExpected) {
        this.failOn = failOn;
        this.failedArtifactFailures = failedArtifactFailures;
        this.failureExpected = successExpected;
    }

    @Before
    public void setUp() {
        complianceChecker = new AbstractComplianceChecker() {
            @Override
            public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
                return evaluation;
            }

            @Override
            public String getRulesetDescription() {
                return "";
            }
        };
        complianceChecker.setAntennaContext(antennaContextMock);
        complianceChecker.configure(Collections.singletonMap(AbstractComplianceChecker.FAIL_ON_KEY, failOn.value()));

        Set<Artifact> failedArtifacts = new HashSet<>();
        failedArtifacts.add(Mockito.mock(Artifact.class));
        Set<IEvaluationResult> results = new HashSet<>();
        failedArtifactFailures.forEach(f -> {
            IEvaluationResult result = Mockito.mock(IEvaluationResult.class);
            when(result.getFailedArtifacts()).thenReturn(failedArtifacts);
            when(result.getSeverity()).thenReturn(f);
            when(result.getId()).thenReturn("TestId");
            results.add(result);
        });

        when(evaluation.getEvaluationResults())
                .thenReturn(results);
    }

    @After
    public void assertThatOnlyExpectedMethodsAreCalled() {
        verify(reporterMock, atLeast(0)).add(any(), anyString());
    }

    @Test
    public void testSimpleExecutionWithoutFail() {
        WorkflowStepResult workflowStepResult = new WorkflowStepResult(complianceChecker.process(Collections.EMPTY_SET));

        complianceChecker.postProcessResult(workflowStepResult);
        if(failureExpected) {
            assertFalse(workflowStepResult.getFailCausingResults().getValue().isEmpty());
        } else {
            assertNull(workflowStepResult.getFailCausingResults());
            verify(reporterMock, atMost(0))
                    .add(eq(MessageType.PROCESSING_FAILURE), anyString());
        }
    }

    private Set<Artifact> mkSingletonArtifact(String name){
        final Artifact artifact = new Artifact("forTest");
        artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, name + "GroupId", name + "ArtifactId", "1.0"));
        return Collections.singleton(artifact);
    }

    private IEvaluationResult mkResult(String description) {
        return mkResult(description, Collections.emptySet());
    }

    private IEvaluationResult mkResult(String description, Set<Artifact> failedArtifacts) {
        return new IEvaluationResult() {
            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public Severity getSeverity() {
                return Severity.INFO;
            }

            @Override
            public Set<Artifact> getFailedArtifacts() {
                return failedArtifacts;
            }
        };
    }
}
