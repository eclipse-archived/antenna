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
package org.eclipse.sw360.antenna.knowledgebase;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVBasedLicenseKnowledgeBaseTest {

    IProcessingReporter iProcessingReporter = Mockito.mock(IProcessingReporter.class);

    @Test
    public void test() {
        CSVBasedLicenseKnowledgeBase knowledgeBase = new CSVBasedLicenseKnowledgeBase();
        knowledgeBase.init(iProcessingReporter, StandardCharsets.UTF_8);
        String licenseNameForId = knowledgeBase.getLicenseNameForId("AFL-1.1");
        assertThat(licenseNameForId).isEqualTo("Academic Free License v1.1");
        assertThat(knowledgeBase.getLicenseIdForAlias("AFL1")).isEqualTo("AFL-1.1");
        assertThat(knowledgeBase.getLicenseIdForAlias("AFL-1.1")).isNull();
    }



}
