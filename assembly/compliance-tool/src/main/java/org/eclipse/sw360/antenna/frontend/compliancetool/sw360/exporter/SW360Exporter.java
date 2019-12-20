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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SW360Exporter {
    private SW360Configuration configuration;
    private SW360ConnectionConfiguration connectionConfiguration;
    private SW360ComponentClientAdapter componentClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;

    public SW360Exporter(File propertiesFile) {
        configuration = new SW360Configuration(propertiesFile);

        connectionConfiguration = configuration.getConnectionConfiguration();

        componentClientAdapter = connectionConfiguration.getSW360ComponentClientAdapter();
        releaseClientAdapter = connectionConfiguration.getSW360ReleaseClientAdapter();
    }

    public void execute() {
        HttpHeaders headers = connectionConfiguration.getHttpHeaders();

        List<SW360SparseComponent> components = componentClientAdapter.getComponents(headers);

        List<SW360SparseRelease> sw360SparseReleases = getReleasesFromComponents(components, headers);

        List<SW360Release> sw360ReleasesNotApproved = getNonApprovedReleasesFromSpareReleases(sw360SparseReleases, headers);

        List<SW360Release> sw360ReleasesSortedByDate = sortReleasesByDate(sw360ReleasesNotApproved);

        SW360ExporterCSVWriter
                .writeReleasesToCsvFile(sw360ReleasesSortedByDate, configuration.getCsvFileName());
    }

    private List<SW360SparseRelease> getReleasesFromComponents(List<SW360SparseComponent> components, HttpHeaders headers) {
        return components.stream()
                .map(this::getIdFromHalResource)
                .filter(id -> !id.equals(""))
                .map(id -> componentClientAdapter.getComponentById(id, headers))
                .map(component -> component.orElse(null))
                .filter(Objects::nonNull)
                .flatMap(component -> component.get_Embedded().getReleases().stream())
                .collect(Collectors.toList());
    }

    private List<SW360Release> getNonApprovedReleasesFromSpareReleases(List<SW360SparseRelease> sw360SparseReleases, HttpHeaders headers) {
        return sw360SparseReleases.stream()
                .map(this::getIdFromHalResource)
                .filter(id -> !id.equals(""))
                .map(id -> releaseClientAdapter.getReleaseById(id, headers))
                .map(Optional::get)
                .filter(this::needsApproval)
                .collect(Collectors.toList());
    }

    private List<SW360Release> sortReleasesByDate(List<SW360Release> sw360ReleasesNotApproved) {
        sw360ReleasesNotApproved.sort(new Comparator<SW360Release>() {
            DateFormat f = new SimpleDateFormat("yyyy-mm-dd");

            @Override
            public int compare(SW360Release t, SW360Release t1) {
                try {
                    return f.parse(t.getCreatedOn()).compareTo(f.parse(t1.getCreatedOn()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return sw360ReleasesNotApproved;
    }

    private boolean needsApproval(SW360Release sw360Release) {
        return sw360Release.getClearingState() == null ||
                !Optional.of(sw360Release.getClearingState())
                        .map(clearingState -> clearingState.equals(ArtifactClearingState.ClearingState.OSM_APPROVED.toString()))
                        .orElse(true);
    }

    private <T extends SW360HalResource<?, ?>> String getIdFromHalResource(T halResource) {
        return SW360HalResourceUtility.getLastIndexOfSelfLink(halResource.get_Links().getSelf()).orElse("");
    }
}
