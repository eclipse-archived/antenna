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

package org.eclipse.sw360.antenna.droolstesting;

import cucumber.api.java.en.Given;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.lang.reflect.Array;
import java.util.*;

import static org.eclipse.sw360.antenna.droolstesting.Mappings.*;

public class GivenSteps {
    private ScenarioState state;

    public GivenSteps(ScenarioState state) {
        this.state = state;
    }

    @Given("^an artifact with$")
    public void an_artifact_with(List<List<String>> artifact_facts) {
        Artifact artifact = new Artifact();
        mapArtifactFacts(artifact_facts, artifact);
        state.artifacts.put("DefaultName", artifact);
    }

    @Given("^an artifact called \"([^\"]*)\" with$")
    public void an_artifact_with(String name, List<List<String>> artifact_facts) {
        Artifact artifact = new Artifact();
        mapArtifactFacts(artifact_facts, artifact);
        state.artifacts.put(name, artifact);
    }

    private void mapArtifactFacts(List<List<String>> artifact_facts, Artifact artifact) {
        artifact_facts.forEach(row -> mapRowKeys(artifact, row));
    }

    private void mapRowKeys(Artifact artifact, List<String> row) {
        if (DATA_KEYS.containsKey(row.get(0))) {
            mapDataKeys(artifact, row);
        } else {
            throw new RuntimeException(row.get(0) + " unknown, must be one of " + String.join(", ", DATA_KEYS.keySet()));
        }
    }

    private void mapDataKeys(Artifact artifact, List<String> row) {
        switch (DATA_KEYS.get(row.get(0))) {
            case LICENSE:
                addLicenseInformation(artifact, row);
                break;
            case LICENSE_ALTERNATIVES:
                addLicenseAlternativeInformation(artifact, row);
                break;
            case LICENSE_MISSING:
                applyMissingLicenseInformation(artifact, row);
                break;
            case PROPRIETARY:
                artifact.setProprietary("true".equals(row.get(1)));
                break;
            case MATCH_STATE:
                addMatchState(artifact, row);
                break;
            case COORDINATES:
                addCoordinates(artifact, row);
                break;
            case SECURITY_ISSUE:
                addSecurityIssue(artifact, row);
                break;
        }
    }

    private void addLicenseInformation(Artifact artifact, List<String> row) {
        License license = new License();
        license.setName(row.get(1));
        if (row.size() >= 4) {
            license.setThreatGroup(THREAT_GROUP_MAP.get(row.get(3)));
        }
        artifact.addFact(LICENSE_GROUP.getOrDefault(row.get(2), ConfiguredLicenseInformation::new).apply(license));
    }

    private void addLicenseAlternativeInformation(Artifact artifact, List<String> row) {
        String licenseAlternatives = row.get(1);
        if (licenseAlternatives.contains(" OR ")) {
            artifact.addFact(LICENSE_GROUP.getOrDefault(row.get(2), ConfiguredLicenseInformation::new)
                    .apply(LicenseSupport.mapLicenses(Arrays.asList(licenseAlternatives.split(" OR ")), LicenseOperator.OR)));
        } else if (licenseAlternatives.contains(" AND ")) {
            artifact.addFact(LICENSE_GROUP.getOrDefault(row.get(2), ConfiguredLicenseInformation::new)
                    .apply(LicenseSupport.mapLicenses(Arrays.asList(licenseAlternatives.split(" AND ")), LicenseOperator.AND)));
        }
    }

    private void applyMissingLicenseInformation(Artifact artifact, List<String> row) {
        if (MISSING_LICENSE_REASONS_MAP.containsKey(row.get(1))) {
            addMissingLicenseInformation(artifact, row);
        } else {
            throw new RuntimeException(row.get(1) + " unknown, must be one of " + String.join(", ", MISSING_LICENSE_REASONS_MAP.keySet()));
        }
    }

    private void addMissingLicenseInformation(Artifact artifact, List<String> row) {
        List<MissingLicenseReasons> missingLicenseReasons = new ArrayList<>();
        missingLicenseReasons.add(MISSING_LICENSE_REASONS_MAP.get(row.get(1)));
        artifact.askFor(MissingLicenseInformation.class)
                .ifPresent(missingInformation -> missingLicenseReasons.addAll(missingInformation.getMissingLicenseReasons()));
        artifact.addFact(new MissingLicenseInformation(missingLicenseReasons));
    }

    private void addMatchState(Artifact artifact, List<String> row) {
        if (MATCH_STATE_MAP.containsKey(row.get(1))) {
            artifact.setMatchState(MATCH_STATE_MAP.get(row.get(1)));
        } else {
            throw new RuntimeException(row.get(1) + " unknown, must be one of " + String.join(", ", MATCH_STATE_MAP.keySet()));
        }
    }

    private void addCoordinates(Artifact artifact, List<String> row) {
        if (COORDINATES_FACTORY.containsKey(row.get(1))) {
            artifact.addFact(COORDINATES_FACTORY.get(row.get(1)).apply(row));
        } else {
            throw new RuntimeException(row.get(1) + " unknown, must be one of " + String.join(", ", COORDINATES_FACTORY.keySet()));
        }
    }

    private void addSecurityIssue(Artifact artifact, List<String> row) {
        Issue securityIssue = new Issue();
        if (SECURITY_ISSUES.containsKey(row.get(1))) {
            securityIssue.setStatus(SECURITY_ISSUES.get(row.get(1)));
        } else {
            throw new RuntimeException(row.get(1) + " unknown, must be one of " + String.join(", ", SECURITY_ISSUES.keySet()));
        }
        if (row.size() >= 3) {
            try {
                securityIssue.setSeverity(Double.parseDouble(row.get(2)));
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Security issue must be parseable to a double value");
            }
        }
        artifact.addFact(new ArtifactIssues(Collections.singletonList(securityIssue)));
    }
}
