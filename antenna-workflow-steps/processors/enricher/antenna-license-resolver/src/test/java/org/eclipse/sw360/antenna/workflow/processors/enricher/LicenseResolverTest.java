/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors.enricher;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.xml.generated.*;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.ArtifactSelector;

public class LicenseResolverTest extends AntennaTestWithMockedContext {

    private List<Artifact> artifacts;
    private LicenseResolver licenseResolver;
    private ArtifactIdentifier artifactIdentifier1;
    private LicenseStatement configuredLicense;

    @Before
    public void init() throws AntennaConfigurationException {

        Artifact artifact0 = new Artifact();

        Artifact artifact1 = new Artifact();
        artifactIdentifier1 = new ArtifactIdentifier();
        artifactIdentifier1.setFilename("aopalliance-1.0.jar");
        MavenCoordinates mavenCoordinates1 = new MavenCoordinates();
        mavenCoordinates1.setArtifactId("aopalliance");
        mavenCoordinates1.setGroupId("aopalliance");
        mavenCoordinates1.setVersion("1.0");
        artifactIdentifier1.setMavenCoordinates(mavenCoordinates1);
        artifactIdentifier1.setHash("0235ba8b489512805ac1");
        artifact1.setArtifactIdentifier(artifactIdentifier1);

        Artifact artifact2 = new Artifact();

        artifacts = Stream.of(artifact0, artifact1, artifact2).collect(Collectors.toList());

        ArtifactSelector selector = new ArtifactSelector(artifactIdentifier1);

        License license1 = new License();
        license1.setName("license1");

        License license2 = new License();
        license2.setName("license2");

        Map<ArtifactSelector, LicenseInformation> configuredLicenses = new HashMap<>();
        configuredLicense = new LicenseStatement();
        configuredLicense.setLeftStatement(license1);
        configuredLicense.setRightStatement(license2);
        configuredLicense.setOp(LicenseOperator.AND);
        configuredLicenses.put(selector, configuredLicense);
        licenseResolver = new LicenseResolver();

        when(configMock.getFinalLicenses()).thenReturn(configuredLicenses);

        licenseResolver.setAntennaContext(antennaContextMock);
        licenseResolver.configure(Collections.emptyMap());
    }

    @After
    public void after() {
        verify(configMock).getFinalLicenses();
    }

    @Test
    public void licenseResolverTest() {

        licenseResolver.process(artifacts);

        assertThat(artifacts.size()).isEqualTo(3);
        assertThat(artifacts.get(0)
                .getFinalLicenses())
                .isNotEqualTo(configuredLicense);
        assertThat(artifacts.get(1)
                .getFinalLicenses())
                .isEqualTo(configuredLicense);
    }

}
