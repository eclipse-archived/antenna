/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.drools;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.api.IEvaluationResult.Severity.WARN;

public class DroolsEvaluationResultReaderTest {

    private static final String POLICY_ID = "Dummy";
    private static final String POLICY_DESC = "This is a dummy policy.";
    private static final IEvaluationResult.Severity POLICY_SEVERITY = WARN;

    private static final String EXP_MSG_IS_NOT_FILE = "is not a file";
    private static final String EXP_MSG_DOES_NOT_FILE = "does not exist";

    private static final String POLICIES_FILENAME = "../../../../../policies/policies.xml";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testNotExistingPolicyFile() {
        Path p = Paths.get("not/existing/file");

        thrown.expect(ExecutionException.class);
        thrown.expectMessage(EXP_MSG_DOES_NOT_FILE);

        DroolsEvaluationResultReader.getEvaluationResult(p);
    }

    @Test
    public void testIsNotPolicyFile() throws IOException {
        Path p = testFolder.newFolder("rootOfFile").toPath();

        thrown.expect(ExecutionException.class);
        thrown.expectMessage(EXP_MSG_IS_NOT_FILE);

        DroolsEvaluationResultReader.getEvaluationResult(p);
    }

    @Test
    public void testWithValidPolicyFile() throws URISyntaxException {
        Path p = Paths.get(getClass().getResource(POLICIES_FILENAME).toURI());
        List<IEvaluationResult> r = DroolsEvaluationResultReader.getEvaluationResult(p);

        assertThat(r).hasSize(2);
        assertThat(r.stream()
            .map(IEvaluationResult::getId)
            .anyMatch(i -> i.equals(POLICY_ID))).isEqualTo(true);
        assertThat(r.stream()
                .map(IEvaluationResult::getDescription)
                .anyMatch(d -> d.contains(POLICY_DESC))).isEqualTo(true);
        assertThat(r.stream()
                .map(IEvaluationResult::getSeverity)
                .anyMatch(s -> s.equals(POLICY_SEVERITY))).isEqualTo(true);
    }
}
