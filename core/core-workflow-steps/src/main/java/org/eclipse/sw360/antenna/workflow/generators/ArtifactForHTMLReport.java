/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ArtifactForHTMLReport {
    private final String identifier;
    private final LicenseInformation license;
    private final String ankerTag;
    private final Collection<String> copyright;
    private final String artifact;

    public ArtifactForHTMLReport(Artifact artifact) {
        this.identifier = artifact.askFor(ArtifactCoordinates.class)
                .map(ArtifactCoordinates::getMainCoordinate)
                .map(Coordinate::canonicalize)
                .orElse("");
        this.ankerTag = UUID.randomUUID().toString();
        this.license = ArtifactLicenseUtils.getFinalLicenses(artifact);
        this.copyright = Arrays.asList(artifact.askFor(CopyrightStatement.class)
                .map(CopyrightStatement::get)
                .map(s -> s.split("(\r\n|\n)"))
                .orElse(new String[0]));
        this.artifact = artifact.askFor(ArtifactFilename.class)
                .map(ArtifactFilename::getFilenames)
                .map(this::concatNames)
                .orElse(artifact.askFor(ArtifactPathnames.class)
                    .map(ArtifactPathnames::get)
                    .map(this::concatNames)
                    .orElse(""));
    }

    private String concatNames(Collection<String> names) {
        return names.stream()
                .filter(StringUtils::isNotBlank)
                .filter(s -> !s.equals("null"))
                .map(FilenameUtils::getName)
                .collect(Collectors.joining(", "));
    }

    public ArtifactForHTMLReport(String identifier, LicenseInformation licenseInformation,
            Collection<String> copyright, String artifactName) {
        this.identifier = identifier;
        this.license = licenseInformation;
        this.ankerTag = UUID.randomUUID().toString();
        this.copyright = copyright;
        this.artifact = artifactName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LicenseInformation getLicense() {
        return license;
    }

    public Collection<String> getCopyright() {
        return copyright;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getAnkerTag() {
        return ankerTag;
    }
}
