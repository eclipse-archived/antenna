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
package org.eclipse.sw360.antenna.workflow.processors;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;
import org.junit.rules.TemporaryFolder;

public class CoordinatesValidatorTest extends AntennaTestWithMockedContext {
    private Artifact artifact;
    private List<Artifact> artifacts;
    private CoordinatesValidator validator;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void init() throws Exception {
        validator = new CoordinatesValidator();
        validator.setAntennaContext(antennaContextMock);
        validator.configure(Collections.emptyMap());
        artifact = new Artifact();
        artifact.setProprietary(false);
        artifacts = new ArrayList<>();
        artifacts.add(artifact);
    }

    @Test
    public void validateCoordinatesTestFalse() {
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void validateCoordinatesTest() {
        artifact.getArtifactIdentifier().setMavenCoordinates(new MavenCoordinates());
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void validateCoordinatesTestMvn() {
        MavenCoordinates mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setArtifactId("test");
        mavenCoordinates.setGroupId("test");
        mavenCoordinates.setVersion("test");
        artifact.getArtifactIdentifier().setMavenCoordinates(mavenCoordinates);
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void validateCoordinatesTestP2() {
        BundleCoordinates bundleCoordinates = new BundleCoordinates();
        bundleCoordinates.setSymbolicName("test");
        bundleCoordinates.setBundleVersion("test");
        artifact.getArtifactIdentifier().setBundleCoordinates(bundleCoordinates);
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void validateCoordinatesTestP2andMvn() {
        MavenCoordinates mavenCoordinates = new MavenCoordinates();
        mavenCoordinates.setArtifactId("test");
        mavenCoordinates.setGroupId("test");
        mavenCoordinates.setVersion("test");
        artifact.getArtifactIdentifier().setMavenCoordinates(mavenCoordinates);
        BundleCoordinates bundleCoordinates = new BundleCoordinates();
        bundleCoordinates.setSymbolicName("test");
        bundleCoordinates.setBundleVersion("test");
        artifact.getArtifactIdentifier().setBundleCoordinates(bundleCoordinates);
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

}
