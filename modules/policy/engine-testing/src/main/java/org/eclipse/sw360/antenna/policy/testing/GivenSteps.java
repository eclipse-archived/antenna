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
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.license.LicenseOperator;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GivenSteps {
    private ScenarioState state;

    public GivenSteps(ScenarioState state) {
        this.state = state;
    }

    @Given("^an artifact$")
    public void an_artifact() {
        an_artifact_with("Default Name", Collections.emptyList());
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

    private static final String LICENSES_PROP = "licenses";
    private static final String PROPRIETARY_PROP = "proprietary";
    private static final String COORDINATES_PROP = "coordinates";
    private static final String SOURCES_PROP = "sources";

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
            case SOURCES_PROP:
                addSourceInfo(row, artifact);
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

    private LicenseInformation createLicenseExpression(LicenseOperator operator, String... licenseExpressionParts) {
        return LicenseSupport.mapLicenses(Arrays.asList(licenseExpressionParts), operator);
    }

    private License createLicense(String licenseExpression, Optional<String> threatGroup) {
        License license = new License();
        String[] licenseText = licenseExpression.split("::");
        license.setName(licenseText[0]);
        if (licenseText.length > 1) {
            license.setText(licenseText[1]);
        }
        threatGroup.ifPresent(license::setThreatGroup);
        return license;
    }

    private static final String DECLARED_LICENSE_EXPRESSION = "Declared";
    private static final String OBSERVED_LICENSE_EXPRESSION = "Observed";
    private static final String OVERWRITTEN_LICENSE_EXPRESSION = "Overwritten";
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
        artifact.addCoordinate(new Coordinate(row.get(1)));
    }

    private static final String SOFTWARE_HERITAGE_PROTOCOL = "swh";
    private static final String FILE_PROTOCOL = "file";
    private static final String URL_PROTOCOL = "http";

    private static final String FOLDERPREFIX = "AntennaPolicy";

    private void addSourceInfo(List<String> row, Artifact artifact) {
        String sourceUrl = row.get(1);
        if (sourceUrl.startsWith(SOFTWARE_HERITAGE_PROTOCOL)) {
            artifact.addFact(new ArtifactSoftwareHeritageID.Builder(sourceUrl).build());
        } else if (sourceUrl.startsWith(FILE_PROTOCOL)) {
            try {
                boolean createFile = true;
                if (row.size() == 3) {
                    createFile = Boolean.parseBoolean(row.get(2));
                }
                String fileref = sourceUrl.split(":")[1];
                Path tempfolder = Files.createTempDirectory(FOLDERPREFIX);
                Path testfile = Paths.get(tempfolder.toString(), fileref);
                if (createFile) {
                    testfile = Files.createFile(testfile);
                }
                state.resourcesToDelete.add(tempfolder);
                artifact.addFact(new ArtifactSourceFile(testfile));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (sourceUrl.startsWith(URL_PROTOCOL)) {
            artifact.addFact(new ArtifactSourceUrl(sourceUrl));
        } else {
            throw new IllegalStateException("Cannot interpret sources information");
        }
    }
}
