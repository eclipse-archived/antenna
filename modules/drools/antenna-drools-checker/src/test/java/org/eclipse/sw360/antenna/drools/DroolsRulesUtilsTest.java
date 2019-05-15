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
package org.eclipse.sw360.antenna.drools;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DroolsRulesUtilsTest {

    @Test
    public void testIsLicenseInArtifact() {
        Artifact artifact = new Artifact();
        License license = new License();
        license.setName("Any-License-Id");
        artifact.addFact(new DeclaredLicenseInformation(license));

        assertThat(DroolsRulesUtils.isLicenseInFinalLicenses(artifact, "Any-License-Id"))
                .isTrue();
    }

    @Test
    public void testIsThreatGroupInArtifact() {
        Artifact artifact = new Artifact();
        License license = new License();
        license.setThreatGroup(LicenseThreatGroup.STRICT_COPYLEFT);
        artifact.addFact(new DeclaredLicenseInformation(license));

        assertThat(DroolsRulesUtils.isThreatGroupInArtifact(artifact, LicenseThreatGroup.STRICT_COPYLEFT))
                .isTrue();
    }

    @Test
    public void testIsThreatGroupInArtifactWithNull() {
        Artifact artifact = new Artifact();
        License license = new License();
        artifact.addFact(new DeclaredLicenseInformation(license));

        assertThat(DroolsRulesUtils.isThreatGroupInArtifact(artifact, null))
                .isTrue();
    }

    @Test
    public void testIsLicenseTypeInArtifact() {
        License license = new License();
        license.setName("AnyLicense");
        Artifact artifact = new Artifact()
                .addFact(new OverriddenLicenseInformation(license));

        assertThat(DroolsRulesUtils.isLicenseTypeInArtifact(artifact, OverriddenLicenseInformation.class))
                .isTrue();
    }

    @Test
    public void testIsArtifactsSecurityIssuesRisky() {
        Issue issue = new Issue();
        issue.setSeverity(7.0);
        issue.setStatus(SecurityIssueStatus.OPEN);
        Artifact artifact = new Artifact()
                .addFact(new ArtifactIssues(Stream.of(issue).collect(Collectors.toList())));

        assertThat(DroolsRulesUtils.isArtifactsSecurityIssuesRisky(artifact, 6.0, SecurityIssueStatus.OPEN))
                .isTrue();
    }

    @Test
    public void testMatchingCoordinatesInArtifact() {
        Artifact artifact = new Artifact();
        artifact.addFact(new MavenCoordinates("runtime", "org.eclipse.launcher", "1.0.0"));

        assertThat(DroolsRulesUtils.matchingCoordinatesInArtifact(artifact, new MavenCoordinates("*", "org.eclipse.*", "*")))
                .isTrue();
    }

    @Test
    public void testIsMissingLicenseInformationInArtifact() {
        Artifact artifact = new Artifact();
        artifact.addFact(new MissingLicenseInformation(Arrays.asList(MissingLicenseReasons.NOT_DECLARED, MissingLicenseReasons.NOT_SUPPORTED)));

        assertThat(DroolsRulesUtils.isMissingLicenseInformationInArtifact(artifact, MissingLicenseReasons.NOT_DECLARED))
                .isTrue();
    }
}
