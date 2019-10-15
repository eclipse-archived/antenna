/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.validators.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SourceValidatorTest extends AntennaTestWithMockedContext {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private SourceValidator validator;
    private File jar;

    private SourceValidatorTestTools sourceValidatorTestTools;

    @Before
    public void before() throws Exception {
        sourceValidatorTestTools = new SourceValidatorTestTools(tmpFolder.getRoot().toPath());
        jar = sourceValidatorTestTools.writeJar();

        validator = new SourceValidator();
        validator.setAntennaContext(antennaContextMock);
    }

    @After
    public void after() {
        Mockito.verify(configMock, Mockito.atLeast(1)).getValidForMissingSources();
        Mockito.verify(configMock, Mockito.atLeast(1)).getValidForIncompleteSources();
    }

    private void configure(Map<String,String> configMap) {
        configure(configMap, Collections.emptyList(), Collections.emptyList());
    }

    private void configure(Map<String,String> configMap, List<Coordinate> validForMissingSources, List<Coordinate> validForIncompleteSources) {
        when(configMock.getValidForMissingSources()).thenReturn(validForMissingSources.stream()
                .map(ArtifactCoordinates::new)
                .collect(Collectors.toList()));
        when(configMock.getValidForIncompleteSources()).thenReturn(validForIncompleteSources.stream()
                .map(ArtifactCoordinates::new)
                .collect(Collectors.toList()));
        validator.configure(configMap);
    }

    private Coordinate mkArtifactIdentifier() {
        return new Coordinate(Coordinate.Types.MAVEN, "org", "source-validator-test-artifact", "1.0");
    }

    private Artifact mkArtifact(File sourceJar) {
        Artifact artifact = new Artifact();
        artifact.addCoordinate(mkArtifactIdentifier());

        artifact.addFact(new ArtifactFile(jar.toPath()));
        if(sourceJar != null) {
            artifact.addFact(new ArtifactSourceFile(sourceJar.toPath()));
        }

        return artifact;
    }

    private Artifact setupForTest(int percentage) throws IOException {
        return setupForTest(Collections.EMPTY_MAP, percentage);
    }

    private Artifact setupForTest(Map<String,String> configMap, int percentage) throws IOException {
        return setupForTest(configMap, percentage, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }
    private Artifact setupForTest(Map<String,String> configMap, int percentage, List<Coordinate> validForMissingSources, List<Coordinate> validForIncompleteSources) throws IOException {
        configure(configMap, validForMissingSources, validForIncompleteSources);
        File sourceJar = sourceValidatorTestTools.writeSourceJar(percentage);
        return mkArtifact(sourceJar);
    }

    @Test
    public void testParsingOfThreshold() throws NoSuchFieldException, IllegalAccessException {
        configure(Collections.singletonMap("threshold", "40"));
        Field f = SourceValidator.class.getDeclaredField("threshold");
        f.setAccessible(true);
        final int threshold = (int) f.get(validator);
        assertThat(threshold == 40);
    }

    @Test
    public void testWithCompleteSources() throws IOException {
        Artifact artifact = setupForTest(100);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void testWithNearlyCompleteSources() throws IOException {
        Artifact artifact = setupForTest(95);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void testWithNonCompleteSources() throws IOException {
        Artifact artifact = setupForTest(0);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.WARN);
    }

    @Test
    public void testWithNonCompleteSourcesAndFail() throws IOException {
        Artifact artifact = setupForTest(Collections.singletonMap("incompleteSourcesSeverity", "FAIL"), 0);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.FAIL);
    }

    @Test
    public void testWithHalfCompleteSourcesAndLowThreshold() throws IOException {
        Artifact artifact = setupForTest(Collections.singletonMap("threshold", "40"), 50);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(0  );
    }

    @Test
    public void testWithHalfCompleteSources() throws IOException {
        Artifact artifact = setupForTest(50);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void testWithHalfCompleteSourcesAndWhitelist() throws IOException {
        Artifact artifact = setupForTest(Collections.emptyMap(), 50, Collections.emptyList(), Collections.singletonList(mkArtifactIdentifier()));

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void testWithHalfCompleteSourcesAndWhitelistInATransitiveWay() throws IOException {
        Artifact artifact = setupForTest(Collections.emptyMap(), 50, Collections.singletonList(mkArtifactIdentifier()), Collections.emptyList());

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void testWithoutSourceJar() {
        configure(Collections.emptyMap());
        Artifact artifact = mkArtifact(null);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.FAIL);
    }

    @Test
    public void testWithoutSourceJarAndWarn()  {
        configure(Collections.singletonMap("missingSourcesSeverity", "WARN"));
        Artifact artifact = mkArtifact(null);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.WARN);
    }

    @Test
    public void testWithoutSourceJarAndWhitelist() {
        configure(Collections.emptyMap(), Collections.singletonList(mkArtifactIdentifier()), Collections.emptyList());
        Artifact artifact = mkArtifact(null);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.INFO);
    }

    @Test
    public void testWithoutSourceJarAndWhitelistInAWrongWay() {
        configure(Collections.emptyMap(), Collections.emptyList(), Collections.singletonList(mkArtifactIdentifier()));
        Artifact artifact = mkArtifact(null);

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.FAIL);
    }


    @Test
    public void testWithoutJar() {
        Artifact artifact = new Artifact();
        artifact.addCoordinate(mkArtifactIdentifier());
        configure(Collections.emptyMap());

        final IPolicyEvaluation evaluate = validator.evaluate(Collections.singleton(artifact));

        assertThat(evaluate).isNotNull();
        assertThat(evaluate.getEvaluationResults().size()).isEqualTo(1);
        assertThat(evaluate.getEvaluationResults().stream()
                .findFirst()
                .get()
                .getSeverity()).isEqualTo(IEvaluationResult.Severity.FAIL);
    }
}
