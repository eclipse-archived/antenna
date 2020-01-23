/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.eclipse.sw360.antenna.csvreader.CSVReader;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.SW360ReleaseAdapterUtils;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SW360Exporter {
    private SW360Configuration configuration;
    private SW360ConnectionConfiguration connectionConfiguration;

    public void setConfiguration(SW360Configuration configuration) {
        this.configuration = configuration;
    }

    public void execute() {
        connectionConfiguration = configuration.getConnectionConfiguration();
        HttpHeaders headers = connectionConfiguration.getHttpHeaders();

        Collection<SW360SparseComponent> components = connectionConfiguration.getSW360ComponentClientAdapter().getComponents(headers);

        Collection<SW360SparseRelease> sw360SparseReleases = getReleasesFromComponents(components, headers);

        Collection<SW360Release> sw360ReleasesNotApproved = getNonApprovedReleasesFromSpareReleases(sw360SparseReleases, headers);

        List<Artifact> artifacts = sw360ReleasesNotApproved.stream()
                .map(release -> SW360ReleaseAdapterUtils.convertToArtifactWithoutSourceFile(release, new Artifact("SW360")))
                .collect(Collectors.toList());

        File csvFile = configuration.getTargetDir()
                .resolve(configuration.getCsvFileName())
                .toFile();

        CSVReader csvReader = new CSVReader(csvFile.toPath(),
                Charset.forName(configuration.getProperties().get("encoding")),
                configuration.getProperties().get("delimiter").charAt(0),
                Paths.get(configuration.getProperties().get("basedir")));

        csvReader.writeArtifactsToCsvFile(artifacts);
    }

    private Collection<SW360SparseRelease> getReleasesFromComponents(Collection<SW360SparseComponent> components, HttpHeaders headers) {
        return components.stream()
                .map(this::getIdFromHalResource)
                .filter(id -> !id.equals(""))
                .map(id -> connectionConfiguration.getSW360ComponentClientAdapter().getComponentById(id, headers))
                .map(component -> component.orElse(null))
                .filter(Objects::nonNull)
                .flatMap(component -> component.get_Embedded().getReleases().stream())
                .collect(Collectors.toList());
    }

    private Collection<SW360Release> getNonApprovedReleasesFromSpareReleases(Collection<SW360SparseRelease> sw360SparseReleases, HttpHeaders headers) {
        return sw360SparseReleases.stream()
                .map(this::getIdFromHalResource)
                .filter(id -> !id.equals(""))
                .map(id -> connectionConfiguration.getSW360ReleaseClientAdapter().getReleaseById(id, headers))
                .map(Optional::get)
                .filter(sw360Release -> !isApproved(sw360Release))
                .sorted(Comparator.comparing(rel -> new Date(rel.getCreatedOn())))
                .collect(Collectors.toList());
    }

    private boolean isApproved(SW360Release sw360Release) {
        return sw360Release.getClearingState() == null ||
                Optional.of(sw360Release.getClearingState())
                        .map(clearingState -> clearingState.equals(ArtifactClearingState.ClearingState.OSM_APPROVED.toString()))
                        .orElse(false);
    }

    private <T extends SW360HalResource<?, ?>> String getIdFromHalResource(T halResource) {
        return SW360HalResourceUtility.getLastIndexOfSelfLink(halResource.get_Links().getSelf()).orElse("");
    }
}
