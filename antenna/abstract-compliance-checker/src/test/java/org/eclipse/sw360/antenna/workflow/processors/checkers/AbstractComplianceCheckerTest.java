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
package org.eclipse.sw360.antenna.workflow.processors.checkers;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
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

import static org.junit.Assert.*;
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
    public void setUp() throws AntennaConfigurationException {
        complianceChecker = new AbstractComplianceChecker() {
            @Override
            public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
                return null;
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
    public void testSimpleExecutionWithoutFail() throws Exception {

        if(failureExpected) {
            try {
                complianceChecker.execute(evaluation);
            } catch (AntennaException e) {
                return; // success
            }
            fail("this should not be reached");
        } else {
            complianceChecker.execute(evaluation);
            verify(reporterMock, atMost(0))
                    .add(eq(MessageType.PROCESSING_FAILURE), anyString());
        }
    }

    @Test
    public void makeStringForEvaluationResultsForArtifactTestEmpty() {

        String artifactName = "ArtifactName";
        Set<IEvaluationResult> failCausingResultsForArtifact = new HashSet<>();

        String result = complianceChecker.makeStringForEvaluationResultsForArtifact(artifactName, failCausingResultsForArtifact);

        assertTrue(result.contains(artifactName));
    }

    private Set<Artifact> mkSingletonArtifact(String name){
        final Artifact artifact = new Artifact("forTest");
        final MavenCoordinates.MavenCoordinatesBuilder mavenCoordinates = new MavenCoordinates.MavenCoordinatesBuilder();
        mavenCoordinates.setVersion("1.0");
        mavenCoordinates.setGroupId(name + "GroupId");
        mavenCoordinates.setArtifactId(name + "ArtifactId");
        artifact.addFact(mavenCoordinates.build());
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

    @Test
    public void makeStringForEvaluationResultsForArtifactTest() {
        String artifactName = "ArtifactName";
        Set<IEvaluationResult> failCausingResultsForArtifact = new HashSet<>();
        failCausingResultsForArtifact.add(mkResult("first result"));
        failCausingResultsForArtifact.add(mkResult("second result"));
        failCausingResultsForArtifact.add(mkResult("3rd result"));
        failCausingResultsForArtifact.add(mkResult("4th result"));
        failCausingResultsForArtifact.add(mkResult("5th result"));

        String result = complianceChecker.makeStringForEvaluationResultsForArtifact(artifactName, failCausingResultsForArtifact);

        assertTrue(result.contains(artifactName));
        long numberOfMatches = failCausingResultsForArtifact.stream()
                .map(evaluationResult -> result.contains(evaluationResult.getDescription()))
                .filter(b -> b)
                .count();
        assertEquals(3, numberOfMatches);

        assertTrue(result.contains("and 2 fail causing results more"));
    }

    @Test
    public void makeStringForEvaluationResultsTest() {
        String header = "Evaluation failed";
        Set<IEvaluationResult> failCausingResults = new HashSet<>();
        failCausingResults.add(mkResult("first result", mkSingletonArtifact("Artifact1")));
        failCausingResults.add(mkResult("second result", mkSingletonArtifact("Artifact1")));
        failCausingResults.add(mkResult("3rd result", mkSingletonArtifact("Artifact1")));
        failCausingResults.add(mkResult("4th result", mkSingletonArtifact("Artifact1")));
        failCausingResults.add(mkResult("5th result", mkSingletonArtifact("Artifact1")));
        failCausingResults.add(mkResult("another (2) result", mkSingletonArtifact("Artifact2")));
        failCausingResults.add(mkResult("another (3) result", mkSingletonArtifact("Artifact3")));
        failCausingResults.add(mkResult("another (4) result", mkSingletonArtifact("Artifact4")));
        failCausingResults.add(mkResult("another (5) result", mkSingletonArtifact("Artifact5")));

        String result = complianceChecker.makeStringForEvaluationResults(header, failCausingResults);

        assertTrue(result.contains(header));
        assertTrue(result.contains("Artifact1"));
        assertTrue(result.contains("and 2 artifacts more"));
        assertTrue(result.contains("and 2 fail causing results more"));
    }
}
