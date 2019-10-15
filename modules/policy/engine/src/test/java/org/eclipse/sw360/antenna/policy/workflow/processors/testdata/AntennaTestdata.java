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
package org.eclipse.sw360.antenna.policy.workflow.processors.testdata;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.util.Arrays;
import java.util.Collection;

public class AntennaTestdata {
    public static final String UNKNOWN_ARTIFACT_ID = "UA";
    public static final String EPL_VS_GPL_ID = "EPLvsGPL";

    public static final String SINGLE_RULESET_CLASS = TestSingleRuleset.class.getName();
    public static final String COMPARE_RULESET_CLASS = TestCompareRuleset.class.getName();

    public static String TEST_RULESETS_LIST = SINGLE_RULESET_CLASS + ", " + COMPARE_RULESET_CLASS;

    public static final Artifact UNKNOWN_ARTIFACT = createUnknownArtifact();
    public static final Artifact GENERIC_ARTIFACT = createGenericArtifact();
    public static final Artifact EXACT_ARTIFACT = createExactArtifact();
    public static final Artifact EPL_ARTIFACT = createEPLArtifact();
    public static final Artifact GPL_ARTIFACT = createGPLArtifact();
    public static final Artifact NON_ASSERTED_ARTIFACT = createArtifactUnknownLicense();

    public static Collection<Artifact> TEST_MATCH_STATE_ARTIFACTS = Arrays.asList(UNKNOWN_ARTIFACT, GENERIC_ARTIFACT,
            EXACT_ARTIFACT);
    public static Collection<Artifact> TEST_LICENSE_ARTIFACTS = Arrays.asList(EPL_ARTIFACT, GPL_ARTIFACT,
            NON_ASSERTED_ARTIFACT);

    private static Artifact createUnknownArtifact() {
        Artifact artifact = new Artifact("Testdata");
        return artifact;
    }

    private static Artifact createGenericArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.addCoordinate(new Coordinate("pkg:generic/Testdata@1.0.0"));
        return artifact;
    }

    private static Artifact createExactArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.addCoordinate(new Coordinate("pkg:p2/Testdata@1.0.0"));
        return artifact;
    }

    private static Artifact createEPLArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.addFact(new DeclaredLicenseInformation(LicenseSupport.mapLicenses(Arrays.asList("EPL-1.0"))));
        return artifact;
    }

    private static Artifact createGPLArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.addFact(new DeclaredLicenseInformation(LicenseSupport.mapLicenses(Arrays.asList("GPL-2.0-or-later"))));
        return artifact;
    }

    private static Artifact createArtifactUnknownLicense() {
        Artifact artifact = new Artifact("Testdata");
        artifact.addFact(new DeclaredLicenseInformation(LicenseSupport.mapLicenses(Arrays.asList("NOASSERTION"))));
        return artifact;
    }
}
