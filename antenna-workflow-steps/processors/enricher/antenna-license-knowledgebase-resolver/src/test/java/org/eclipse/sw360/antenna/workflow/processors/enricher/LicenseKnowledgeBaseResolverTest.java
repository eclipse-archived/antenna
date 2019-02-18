/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors.enricher;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ConfiguredLicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseClassification;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LicenseKnowledgeBaseResolverTest {

    @Mock
    ILicenseManagementKnowledgeBase knowledgeBaseMock = mock(ILicenseManagementKnowledgeBase.class);

    LicenseKnowledgeBaseResolver knowledgeBaseResolver = new LicenseKnowledgeBaseResolver(knowledgeBaseMock);

    // License 1
    private static final String LICENSE_ID = "license id";
    private static final String LICENSE_NAME = "configured license name";
    private static final String LICENSE_TEXT = "configured license text";
    private static final LicenseThreatGroup LICENSE_THREAT_GROUP = LicenseThreatGroup.UNKNOWN;
    private static final LicenseClassification LICENSE_CLASSIFICATION = LicenseClassification.NOT_CLASSIFIED;
    
    // License 2
    private static final String KB_LICENSE_ID = "license kb id";
    private static final String KB_LICENSE_NAME = "knowlegdebase license name";
    private static final String KB_LICENSE_TEXT = "knowledgebase license text";
    private static final LicenseThreatGroup KB_LICENSE_THREAT_GROUP = LicenseThreatGroup.NON_STANDARD;
    private static final LicenseClassification KB_LICENSE_CLASSIFICATION = LicenseClassification.NOT_COVERED;

    @Before
    public void before() throws AntennaConfigurationException {
        when(knowledgeBaseMock.getLicenseNameForId(LICENSE_ID))
                .thenReturn(LICENSE_NAME);
        when(knowledgeBaseMock.getTextForId(LICENSE_ID))
                .thenReturn(LICENSE_TEXT);
        when(knowledgeBaseMock.getThreatGroupForId(LICENSE_ID))
                .thenReturn(LICENSE_THREAT_GROUP);
        when(knowledgeBaseMock.getClassificationById(LICENSE_ID))
                .thenReturn(LICENSE_CLASSIFICATION);

        when(knowledgeBaseMock.getLicenseNameForId(KB_LICENSE_ID))
                .thenReturn(KB_LICENSE_NAME);
        when(knowledgeBaseMock.getTextForId(KB_LICENSE_ID))
                .thenReturn(KB_LICENSE_TEXT);
        when(knowledgeBaseMock.getThreatGroupForId(KB_LICENSE_ID))
                .thenReturn(KB_LICENSE_THREAT_GROUP);
        when(knowledgeBaseMock.getClassificationById(KB_LICENSE_ID))
                .thenReturn(KB_LICENSE_CLASSIFICATION);
    }

    @Test
    public void testArtifactWithOnlyKnownLicenseId() {
        License license = new License();
        license.setName(LICENSE_ID);
        
        Artifact artifact = new Artifact();
        artifact.addFact(new ConfiguredLicenseInformation(license));

        knowledgeBaseResolver.process(Collections.singletonList(artifact));

        final List<License> finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        assertEquals(1, finalLicenses.size());
        License l = finalLicenses.stream().findAny().get();
        assertEquals(LICENSE_ID, l.getName());
        assertEquals(LICENSE_NAME, l.getLongName());
        assertEquals(LICENSE_TEXT, l.getText());
        assertEquals(LICENSE_THREAT_GROUP, l.getThreatGroup());
        assertEquals(LICENSE_CLASSIFICATION, l.getClassification());
    }
    
    @Test
    public void testArtifactWithFullyConfiguredLicense() {
        License license = new License();
        license.setName(LICENSE_ID);
        license.setLongName(LICENSE_NAME);
        license.setText(LICENSE_TEXT);
        license.setThreatGroup(LICENSE_THREAT_GROUP);
        license.setClassification(LICENSE_CLASSIFICATION);
        
        Artifact artifact = new Artifact();
        artifact.addFact(new ConfiguredLicenseInformation(license));

        knowledgeBaseResolver.process(Collections.singletonList(artifact));


        final List<License> finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        assertEquals(1, finalLicenses.size());
        License l = finalLicenses.stream().findAny().get();
        assertEquals(LICENSE_ID, l.getName());
        assertEquals(LICENSE_NAME, l.getLongName());
        assertEquals(LICENSE_TEXT, l.getText());
        assertEquals(LICENSE_THREAT_GROUP, l.getThreatGroup());
        assertEquals(LICENSE_CLASSIFICATION, l.getClassification());
    }
    
    @Test
    public void testArtifactWithUnknownLicense() {
        String licenseName = "Unknown license";
        License license = new License();
        license.setName(licenseName);
        
        Artifact artifact = new Artifact();
        artifact.addFact(new ConfiguredLicenseInformation(license));

        knowledgeBaseResolver.process(Collections.singletonList(artifact));

        final List<License> finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        assertEquals(1, finalLicenses.size());
        License l = finalLicenses.stream().findAny().get();
        assertEquals(licenseName, l.getName());
        assertNull(l.getLongName());
        assertNull(l.getText());
        assertNull(l.getThreatGroup());
        assertNull(l.getClassification());
    }
}
