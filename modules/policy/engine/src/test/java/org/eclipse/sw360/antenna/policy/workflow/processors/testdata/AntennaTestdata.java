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
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.util.Arrays;
import java.util.Collection;

public class AntennaTestdata {
    public static final String UNKNOWNARTIFACTID = "UA";
    public static final String EPLVSGPLID = "EPLvsGPL";

    public static final String SINGLERULESETCLASS = TestSingleRuleset.class.getName();
    public static final String COMPARERULESETCLASS = TestCompareRuleset.class.getName();

    public static String TESTRULESETSLIST = SINGLERULESETCLASS + ", " + COMPARERULESETCLASS;

    public static final Artifact UNKNOWN_ARTIFACT = createUnknownArtifact();
    public static final Artifact SIMILAR_ARTIFACT = createSimilarArtifact();
    public static final Artifact EXACT_ARTIFACT = createExactArtifact();
    public static final Artifact EPL_ARTIFACT = createEPLArtifact();
    public static final Artifact GPL_ARTIFACT = createGPLArtifact();
    public static final Artifact NONASSERTED_ARTIFACT = createArtifactUnknownLicense();

    public static Collection<Artifact> TESTMATCHSTATEARTIFACTS = Arrays.asList(UNKNOWN_ARTIFACT, SIMILAR_ARTIFACT,
            EXACT_ARTIFACT);
    public static Collection<Artifact> TESTLICENSEARTIFACTS = Arrays.asList(EPL_ARTIFACT, GPL_ARTIFACT,
            NONASSERTED_ARTIFACT);

    private static Artifact createUnknownArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.setMatchState(MatchState.UNKNOWN);
        return artifact;
    }

    private static Artifact createSimilarArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.setMatchState(MatchState.SIMILAR);
        return artifact;
    }

    private static Artifact createExactArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.setMatchState(MatchState.EXACT);
        return artifact;
    }

    private static Artifact createEPLArtifact() {
        Artifact artifact = new Artifact("Testdata");
        artifact.addFact(new DeclaredLicenseInformation(LicenseSupport.mapLicenses(Arrays.asList("EPL-2.0"))));
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
