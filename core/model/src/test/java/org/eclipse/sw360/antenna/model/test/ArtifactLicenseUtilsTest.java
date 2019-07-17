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
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ConfiguredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.artifact.facts.OverriddenLicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class ArtifactLicenseUtilsTest {

    private License mit;
    private License apache;
    private License epl;

    @Before
    public void setUp() {
        mit = new License();
        mit.setName("MIT");

        apache = new License();
        apache.setName("Apache-2.0");

        epl = new License();
        epl.setName("EPL-2.0");
    }

    @Test
    public void testConfiguredLicenseAsFinal() {
        Artifact artifact = new Artifact("Test")
                .addFact(new ConfiguredLicenseInformation(mit))
                .addFact(new OverriddenLicenseInformation(epl));

        LicenseInformation finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact);

        assertThat(finalLicenses.getLicenses().size())
                .isEqualTo(1);
        assertThat(finalLicenses.getLicenses()
                    .stream()
                    .findAny()
                    .get()
                    .getName())
                .isEqualTo(mit.getName());
    }

    @Test
    public void testEffectiveLicenseWithAndInObservedAsFinal() {
        LicenseStatement observedLicenseStatement = new LicenseStatement();
        observedLicenseStatement.setLeftStatement(epl);
        observedLicenseStatement.setOp(LicenseOperator.AND);
        observedLicenseStatement.setRightStatement(apache);

        Artifact artifact = new Artifact("Test")
                .addFact(new DeclaredLicenseInformation(apache))
                .addFact(new ObservedLicenseInformation(observedLicenseStatement));

        LicenseInformation finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact);

        assertThat(finalLicenses.getLicenses().size())
                .isEqualTo(2);
        assertThat(finalLicenses.getLicenses()
                    .stream()
                    .filter(l -> l.getName().equals(apache.getName()))
                    .collect(Collectors.toList())
                    .size())
                .isEqualTo(1);
    }

    @Test
    public void testEffectiveLicenseWithOrInObserved() {
        LicenseStatement observedLicenseStatement = new LicenseStatement();
        observedLicenseStatement.setLeftStatement(epl);
        observedLicenseStatement.setOp(LicenseOperator.OR);
        observedLicenseStatement.setRightStatement(apache);

        Artifact artifact = new Artifact("Test")
                .addFact(new DeclaredLicenseInformation(apache))
                .addFact(new ObservedLicenseInformation(observedLicenseStatement));

        LicenseInformation finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact);

        assertThat(finalLicenses.getLicenses().size())
                .isEqualTo(2);
    }

    @Test
    public void testDeclaredLicenseAsFinal() {
        Artifact artifact = new Artifact("Test")
                .addFact(new DeclaredLicenseInformation(epl))
                .addFact(new ObservedLicenseInformation(mit));

        LicenseInformation finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact);

        assertThat(finalLicenses.getLicenses().size())
                .isEqualTo(2);
        assertThat(finalLicenses.getLicenses().get(0).getName())
                .isEqualTo(epl.getName());
    }
}
