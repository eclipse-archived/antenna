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
package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LicenseResolverTest extends AntennaTestWithMockedContext {

    private List<Artifact> artifacts;
    private LicenseResolver licenseResolver;
    private LicenseStatement configuredLicense;

    @Before
    public void init() {

        Artifact artifact0 = new Artifact();

        Artifact artifact1 = new Artifact();
        artifact1.addFact(new ArtifactFilename("aopalliance-1.0.jar", "0235ba8b489512805ac1"));
        artifact1.addFact(new MavenCoordinates("aopalliance", "aopalliance", "1.0"));

        Artifact artifact2 = new Artifact();

        artifacts = Stream.of(artifact0, artifact1, artifact2).collect(Collectors.toList());

        ArtifactSelector selector = new ArtifactFilename("aopalliance-1.0.jar", "0235ba8b489512805ac1");

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
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(0)))
                .isNotEqualTo(configuredLicense);
        assertThat(ArtifactLicenseUtils.getFinalLicenses(artifacts.get(1)))
                .isEqualTo(configuredLicense);
    }

}
