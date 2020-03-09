/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.knowledgebase.LicenseKnowledgeBaseFactory;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ConfiguredLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LicenseKnowledgeBaseResolverTest extends AntennaTestWithMockedContext {

    @Mock
    ILicenseManagementKnowledgeBase knowledgeBaseMock = mock(ILicenseManagementKnowledgeBase.class);
    @Mock
    LicenseKnowledgeBaseFactory supplierMock = mock(LicenseKnowledgeBaseFactory.class);

    LicenseKnowledgeBaseResolver knowledgeBaseResolver;

    // License 1
    private static final String LICENSE_ID = "license id";
    private static final String LICENSE_NAME = "configured license name";
    private static final String LICENSE_TEXT = "configured license text";
    private static final String LICENSE_THREAT_GROUP = "configured threat group";
    private static final String LICENSE_CLASSIFICATION = "configured classification";
    
    // License 2
    private static final String KB_LICENSE_ID = "license kb id";
    private static final String KB_LICENSE_NAME = "knowlegdebase license name";
    private static final String KB_LICENSE_TEXT = "knowledgebase license text";
    private static final String KB_LICENSE_THREAT_GROUP = "any threat group";
    private static final String KB_LICENSE_CLASSIFICATION = "any classification";

    @Before
    public void before() {
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

        when(supplierMock.get())
                .thenReturn(Arrays.asList(knowledgeBaseMock));

        knowledgeBaseResolver = new LicenseKnowledgeBaseResolver(knowledgeBaseMock, supplierMock);
    }

    @Test(expected = ConfigurationException.class)
    public void testProcessorWithoutConfiguration() {
        LicenseKnowledgeBaseResolver resolver = new LicenseKnowledgeBaseResolver(knowledgeBaseMock, supplierMock);

        resolver.configure(Collections.EMPTY_MAP);
    }

    @Test
    public void testProcessorWithConfiguredManager() {
        ILicenseManagementKnowledgeBase o = mockLicenseManagementKnowledgeBase("SPDX", 0);
        when(supplierMock.get()).thenReturn(Arrays.asList(o));

        LicenseKnowledgeBaseResolver resolver = new LicenseKnowledgeBaseResolver(null, supplierMock);

        Map<String, String> config = new HashMap<>();
        config.put("chosen.license.manager", "SPDX");
        resolver.configure(config);

        assertThat(resolver.getKnowledgeBase().getId()).isEqualTo("SPDX");
        assertThat(resolver.getKnowledgeBase().getPriority()).isEqualTo(0);
    }

    @Test
    public void testProcessorWithMultipleManagers() {
        ILicenseManagementKnowledgeBase o1 = mockLicenseManagementKnowledgeBase("SPDX", 0);
        ILicenseManagementKnowledgeBase o2 = mockLicenseManagementKnowledgeBase("CSV", 200);
        ILicenseManagementKnowledgeBase o3 = mockLicenseManagementKnowledgeBase("TXT", 100);
        ILicenseManagementKnowledgeBase o4 = mockLicenseManagementKnowledgeBase("JSON", 500);
        when(supplierMock.get()).thenReturn(Arrays.asList(o1, o2, o3, o4));

        LicenseKnowledgeBaseResolver resolver = new LicenseKnowledgeBaseResolver(null, supplierMock);

        Map<String, String> config = new HashMap<>();
        config.put("chosen.license.manager", "NOT_AVAILABLE");
        resolver.configure(config);

        assertThat(resolver.getKnowledgeBase().getId()).isEqualTo("JSON");
        assertThat(resolver.getKnowledgeBase().getPriority()).isEqualTo(500);
    }

    @Test
    public void testArtifactWithOnlyKnownLicenseId() {
        License license = new License();
        license.setId(LICENSE_ID);
        
        Artifact artifact = new Artifact();
        artifact.addFact(new ConfiguredLicenseInformation(license));

        knowledgeBaseResolver.process(Collections.singletonList(artifact));

        final List<License> finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        assertThat(finalLicenses.size()).isEqualTo(1);
        assertThat(finalLicenses.stream()
                .findAny())
                .hasValueSatisfying(l -> {
                    assertThat(l.getId()).isEqualTo(LICENSE_ID);
                    assertThat(l.getCommonName()).isEqualTo(LICENSE_NAME);
                    assertThat(l.getText()).isEqualTo(LICENSE_TEXT);
                    assertThat(l.getThreatGroup()).hasValue(LICENSE_THREAT_GROUP);
                    assertThat(l.getClassification()).hasValue(LICENSE_CLASSIFICATION);
        });
    }
    
    @Test
    public void testArtifactWithFullyConfiguredLicense() {
        License license = new License();
        license.setId(LICENSE_ID);
        license.setCommonName(LICENSE_NAME);
        license.setText(LICENSE_TEXT);
        license.setThreatGroup(LICENSE_THREAT_GROUP);
        license.setClassification(LICENSE_CLASSIFICATION);
        
        Artifact artifact = new Artifact();
        artifact.addFact(new ConfiguredLicenseInformation(license));

        knowledgeBaseResolver.process(Collections.singletonList(artifact));


        final List<License> finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        assertThat(finalLicenses.size()).isEqualTo(1);
        assertThat(finalLicenses.stream()
                .findAny())
                .hasValueSatisfying(l -> {
                    assertThat(l.getId()).isEqualTo(LICENSE_ID);
                    assertThat(l.getCommonName()).isEqualTo(LICENSE_NAME);
                    assertThat(l.getText()).isEqualTo(LICENSE_TEXT);
                    assertThat(l.getThreatGroup()).hasValue(LICENSE_THREAT_GROUP);
                    assertThat(l.getClassification()).hasValue(LICENSE_CLASSIFICATION);
        });
    }
    
    @Test
    public void testArtifactWithUnknownLicense() {
        String licenseName = "Unknown license";
        License license = new License();
        license.setId(licenseName);
        
        Artifact artifact = new Artifact();
        artifact.addFact(new ConfiguredLicenseInformation(license));

        knowledgeBaseResolver.process(Collections.singletonList(artifact));

        final List<License> finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        assertThat(finalLicenses.size()).isEqualTo(1);
        assertThat(finalLicenses.stream()
                .findAny()).hasValueSatisfying(l -> {
                    assertThat(l.getId()).isEqualTo(licenseName);
                    assertThat(l.getCommonName()).isNull();
                    assertThat(l.getText()).isNull();
                    assertThat(l.getThreatGroup()).isNotPresent();
                    assertThat(l.getClassification()).isNotPresent();
        });
    }

    private ILicenseManagementKnowledgeBase mockLicenseManagementKnowledgeBase(String id, int priority) {
        ILicenseManagementKnowledgeBase o = mock(ILicenseManagementKnowledgeBase.class);
        when(o.getId()).thenReturn(id);
        when(o.getPriority()).thenReturn(priority);
        return o;
    }
}
