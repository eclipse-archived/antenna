/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReporterOutputFactoryTest {
    @Test
    public void defaultStringEqualsDefaultTest() {
        final ReporterOutput reporterOutput = ReporterOutputFactory.getReporterOutput("");

        assertThat(reporterOutput).isEqualTo(ReporterOutputFactory.DEFAULT_REPORTER_OUTPUT);
    }
}
