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
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.reporting.IncompleteSourcesFailure;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class IncompleteSourcesTest {

    @Test
    public void test() {
        IncompleteSourcesFailure failure = new IncompleteSourcesFailure();
        failure.addMissingSources("testSource1.java");
        failure.addMissingSources("testSource2.java");
        failure.addMissingSources("testSource3.java");
        List<String> missingSources = new ArrayList<>();
        missingSources.add("testSource1.java");
        failure.setMissingSources(missingSources);
        assertThat(failure.getMissingSources()).isEqualTo(missingSources);
        assertThat(failure.getMissingSources().contains("testSource1.java")).isTrue();
    }
}
