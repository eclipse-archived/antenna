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
package org.eclipse.sw360.antenna.policy.testing;

import cucumber.api.java.en.Given;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.util.ArtifactUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GivenSteps {
    private static final String LICENSES_PROP = "licenses";
    private static final String PROPRIETARY_PROP = "proprietary";
    private static final String COORDINATES_PROP = "coordinates";

    private ScenarioState state;

    public GivenSteps(ScenarioState state) {
        this.state = state;
    }

    @Given("^an artifact with$")
    public void an_artifact_with(List<List<String>> artifact_facts) {
        an_artifact_with("Default Name", artifact_facts);
    }

    @Given("^an artifact called \"([^\"]*)\" with$")
    public void an_artifact_with(String name, List<List<String>> artifact_facts) {
        Artifact artifact = new Artifact();
        mapArtifactFacts(artifact_facts, artifact);
        state.artifacts.put(name, artifact);
    }

    private void mapArtifactFacts(List<List<String>> artifact_facts, Artifact artifact) {
        artifact_facts.forEach(row -> mapProperty(row, artifact));
    }

    private void mapProperty(List<String> row, Artifact artifact) {
        switch (getRowPropertyType(row)) {
            case LICENSES_PROP:
                addLicenseInformation(row, artifact);
                break;
            case PROPRIETARY_PROP:
                artifact.setProprietary("true".equals(row.get(1)));
                break;
            case COORDINATES_PROP:
                addCoordinates(row, artifact);
                break;
            default:
                throw new IllegalArgumentException("Configuration Error: "
                        + getRowPropertyType(row) + " is an unknown property for an artifact");
        }
    }

    private String getRowPropertyType(List<String> row) {
        return row.get(0);
    }

    private void addLicenseInformation(List<String> row, Artifact artifact) {
        Optional<String> threatGroup = Optional.empty();
        if (row.size() >= 4) {
            threatGroup = Optional.ofNullable(row.get(3));
        }
        artifact.addFact(parseLicenseExpression(row.get(1), row.get(2), threatGroup));
    }

    private ArtifactLicenseInformation parseLicenseExpression(String licenseExpression, String expressionType,
            Optional<String> threatGroup) {
        if (licenseExpression.contains(" OR ")) {
            return createLicenseInformation(expressionType,
                    createLicenseExpression(LicenseOperator.OR, licenseExpression.split(" OR ")));
        } else if (licenseExpression.contains(" AND ")) {
            return createLicenseInformation(expressionType,
                    createLicenseExpression(LicenseOperator.AND, licenseExpression.split(" AND ")));
        }
        return createLicenseInformation(expressionType, createLicense(licenseExpression, threatGroup));
    }

    private LicenseInformation createLicenseExpression(LicenseOperator or, String... licenseExpressionParts) {
        return LicenseSupport.mapLicenses(Arrays.asList(licenseExpressionParts), or);
    }

    private static final String UNKNOWN_THREAT_GROUP = "unknown";
    private static final String LIBERAL_THREAT_GROUP = "liberal";
    private static final String STRICT_COPYLEFT_THREAT_GROUP = "strict copyleft";
    private static final String HIGH_RISK_THREAT_GROUP = "high risk";
    private static final String FREEWARE_THREAT_GROUP = "freeware";
    private static final String NON_STANDARD_THREAT_GROUP = "non standard";

    private static final Map<String, LicenseThreatGroup> THREAT_GROUP_MAP = Stream.of(new Object[][] {
            { UNKNOWN_THREAT_GROUP, LicenseThreatGroup.UNKNOWN },
            { LIBERAL_THREAT_GROUP, LicenseThreatGroup.LIBERAL },
            { STRICT_COPYLEFT_THREAT_GROUP, LicenseThreatGroup.STRICT_COPYLEFT },
            { HIGH_RISK_THREAT_GROUP, LicenseThreatGroup.HIGH_RISK },
            { FREEWARE_THREAT_GROUP, LicenseThreatGroup.FREEWARE },
            { NON_STANDARD_THREAT_GROUP, LicenseThreatGroup.NON_STANDARD }})
            .collect(Collectors.toMap(data -> (String) data[0], data -> (LicenseThreatGroup) data[1]));

    private License createLicense(String licenseExpression, Optional<String> threatGroup) {
        License license = new License();
        license.setName(licenseExpression);
        threatGroup.ifPresent(tg -> license.setThreatGroup(THREAT_GROUP_MAP.get(tg)));
        return license;
    }

    private static final String DECLARED_LICENSE_EXPRESSION = "Declared";
    private static final String OBSERVED_LICENSE_EXPRESSION = "Observed";
    private static final String OVERWRITTEN_LICENSE_EXPRESSION = "Overridden";
    private static final String CONFIGURED_LICENSE_EXPRESSION = "Configured";

    private ArtifactLicenseInformation createLicenseInformation(String expressionType,
            LicenseInformation licenseExpression) {
        switch (expressionType) {
            case DECLARED_LICENSE_EXPRESSION:
                return new DeclaredLicenseInformation(licenseExpression);
            case OBSERVED_LICENSE_EXPRESSION:
                return new ObservedLicenseInformation(licenseExpression);
            case OVERWRITTEN_LICENSE_EXPRESSION:
                return new OverriddenLicenseInformation(licenseExpression);
            case CONFIGURED_LICENSE_EXPRESSION:
                return new ConfiguredLicenseInformation(licenseExpression);
            default:
                throw new IllegalArgumentException("Configuration Error: " + expressionType
                        + " is not a defined license expression type");
        }
    }

    private void addCoordinates(List<String> row, Artifact artifact) {
        artifact.addFact(ArtifactUtils.createArtifactCoordinatesFromPurl(row.get(1)));
    }
}
