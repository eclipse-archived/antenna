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
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ArtifactLicenseUtilsTest {

    private License mit;
    private License apache;
    private License epl;

    @Before
    public void setUp() {
        mit = new License.Builder().setLicenseId("MIT").build();

        apache = new License.Builder().setLicenseId("Apache-2.0").build();

        epl = new License.Builder().setLicenseId("EPL-2.0").build();
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
                    .get())
                .isEqualTo(mit);
    }

    @Test
    public void testEffectiveLicenseWithAndInObservedAsFinal() {
        LicenseInformation observedLicenseStatement = epl.and(apache);

        Artifact artifact = new Artifact("Test")
                .addFact(new DeclaredLicenseInformation(apache))
                .addFact(new ObservedLicenseInformation(observedLicenseStatement));

        LicenseInformation finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact);

        assertThat(finalLicenses.getLicenses().size())
                .isEqualTo(2);
        assertThat((int) finalLicenses.getLicenses()
                .stream()
                .filter(apache::equals)
                .count())
                .isEqualTo(1);
    }

    @Test
    public void testEffectiveLicenseWithOrInObserved() {
        LicenseInformation observedLicenseStatement = epl.or(apache);

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
        assertThat(finalLicenses.getLicenses().contains(epl))
                .isTrue();
    }
}
