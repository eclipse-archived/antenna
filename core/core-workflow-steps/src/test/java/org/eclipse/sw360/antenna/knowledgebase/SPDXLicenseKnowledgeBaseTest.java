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
package org.eclipse.sw360.antenna.knowledgebase;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SPDXLicenseKnowledgeBaseTest {

    IProcessingReporter reportMock = mock(IProcessingReporter.class);
    SPDXLicenseKnowledgeBase spdxLicenseKnowledgeBase;

    @Before
    public void setUp() {
        spdxLicenseKnowledgeBase = new SPDXLicenseKnowledgeBase();
        spdxLicenseKnowledgeBase.init(reportMock, StandardCharsets.UTF_8);
    }

    @Test
    public void testKnowledgeBaseProperties() {
        assertThat(spdxLicenseKnowledgeBase.getId()).isEqualTo("SPDXLicenseKnowledgeBase");
        assertThat(spdxLicenseKnowledgeBase.getPriority()).isEqualTo(0);
        assertThat(spdxLicenseKnowledgeBase.isRunnable()).isTrue();
    }

    @Test
    public void testSimpleSpdxKnowledgeBase() {
        String licenseNameForId = spdxLicenseKnowledgeBase.getLicenseNameForId("Apache-2.0");
        assertThat(licenseNameForId).isEqualTo("Apache License 2.0");

        assertThat(spdxLicenseKnowledgeBase.getLicenseIdForAlias("Apache-2.0")).isEqualTo("Apache-2.0");
        assertThat(spdxLicenseKnowledgeBase.getTextForId("Apache-2.0")).isNotEmpty();
        assertThat(spdxLicenseKnowledgeBase.getTextForId("Apache-2.0"))
                .startsWith("Apache License");
        assertThat(spdxLicenseKnowledgeBase.getThreatGroupForId("Apache-2.0")).isEqualTo("Unknown");
        assertThat(spdxLicenseKnowledgeBase.getClassificationById("Apache-2.0")).isEmpty();
    }

    @Test
    public void testUnknownSpdxLicense() {
        String licenseNameForId = spdxLicenseKnowledgeBase.getLicenseNameForId("Unknown-License");
        verify(reportMock, times(1))
                .add(eq("Unknown-License"), eq(MessageType.MISSING_LICENSE_INFORMATION), anyString());
        verify(reportMock, times(1))
                .add(eq("Unknown-License"), eq(MessageType.UNKNOWN_LICENSE), anyString());
        assertThat(licenseNameForId).isEqualTo("Unknown-License");

        String licenseText = spdxLicenseKnowledgeBase.getTextForId("Unknown-License");
        verify(reportMock, times(1))
                .add(eq("Unknown-License"), eq(MessageType.MISSING_LICENSE_TEXT), anyString());
        assertThat(licenseText).isNull();

        String licenseAlias = spdxLicenseKnowledgeBase.getLicenseIdForAlias("Unknown-License");
        verify(reportMock, times(2))
                .add(eq("Unknown-License"), eq(MessageType.MISSING_LICENSE_INFORMATION), anyString());
        assertThat(licenseAlias).isEqualTo("Unknown-License");
    }

}
