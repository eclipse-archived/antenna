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

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        artifacts = new ArrayList<>();
        artifacts.add(artifact);
    }

    @Test
    public void validateCoordinatesTestFalse() {
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void validateCoordinatesTest() {
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void validateCoordinatesTestEmptyMvn() {
        artifact.addFact(new MavenCoordinates(null,null,null));
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(1);
    }

    @Test
    public void validateCoordinatesTestMvn() {
        artifact.addFact(new MavenCoordinates("test", "test", "test"));
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void validateCoordinatesTestP2() {
        artifact.addFact(new BundleCoordinates("test", "test"));
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

    @Test
    public void validateCoordinatesTestP2andMvn() {
        artifact.addFact(new MavenCoordinates("test", "test", "test"));
        artifact.addFact(new BundleCoordinates("test", "test"));
        assertThat(validator.evaluate(artifacts).getEvaluationResults().size()).isEqualTo(0);
    }

}
