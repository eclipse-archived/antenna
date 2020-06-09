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

import org.eclipse.sw360.antenna.csvreader.CSVArtifactMapper;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SW360Exporter {
    /**
     * The configuration property defining the encoding of the CSV file.
     */
    public static final String PROP_ENCODING = "encoding";

    /**
     * The configuration property defining the delimiter to be used in the CSV
     * file.
     */
    public static final String PROP_DELIMITER = "delimiter";

    /**
     * The configuration property defining the base directory for the exporter.
     * Relative paths are resolved against this directory.
     */
    public static final String PROP_BASEDIR = "basedir";

    /**
     * The configuration property that controls whether source files that are
     * not referenced by any of the current components should be removed. If
     * set to <strong>true</strong>, obsolete source attachments are cleaned up
     * automatically.
     */
    public static final String PROP_REMOVE_SOURCES = "removeUnreferencedSources";

    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Exporter.class);

    private final SW360Configuration configuration;
    private final SourcesExporter sourcesExporter;
    private SW360Connection connection;

    public SW360Exporter(SW360Configuration configuration) {
        this(configuration, new SourcesExporter(configuration.getSourcesPath()));
    }

    SW360Exporter(SW360Configuration configuration, SourcesExporter sourcesExporter) {
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
        this.sourcesExporter = sourcesExporter;
    }

    public void execute() {
        LOGGER.debug("{} has started.", SW360Exporter.class.getName());
        connection = configuration.getConnection();

        Collection<SW360SparseComponent> components = connection.getComponentAdapter().getComponents();

        Collection<SW360SparseRelease> sw360SparseReleases = getReleasesFromComponents(components);

        Collection<SW360Release> sw360ReleasesNotApproved = getNonApprovedReleasesFromSpareReleases(sw360SparseReleases);

        Collection<SourcesExporter.ReleaseWithSources> nonApprovedReleasesWithSources =
                sourcesExporter.downloadSources(connection.getReleaseAdapterAsync(), sw360ReleasesNotApproved);

        List<Artifact> artifacts = nonApprovedReleasesWithSources.stream()
                .sorted(releasesComparator().reversed())
                .map(this::releaseAsArtifact)
                .collect(Collectors.toList());

        File csvFile =  configuration.getCsvFilePath()
                .toFile();

        CSVArtifactMapper csvArtifactMapper = new CSVArtifactMapper(csvFile.toPath(),
                Charset.forName(configuration.getProperties().get(PROP_ENCODING)),
                configuration.getProperties().get(PROP_DELIMITER).charAt(0),
                Paths.get(configuration.getProperties().get(PROP_BASEDIR)));

        csvArtifactMapper.writeArtifactsToCsvFile(artifacts);

        if (Boolean.parseBoolean(configuration.getProperties().get(PROP_REMOVE_SOURCES))) {
            sourcesExporter.removeUnreferencedFiles(nonApprovedReleasesWithSources);
        }

        LOGGER.info("The SW360Exporter was executed from the base directory: {} " +
                        "with the csv file written to the path: {} " +
                        "and the source files written to the folder: {}. ",
                configuration.getBaseDir().toAbsolutePath(),
                csvFile.toPath(),
                configuration.getTargetDir().toAbsolutePath());
    }

    private Artifact releaseAsArtifact(SourcesExporter.ReleaseWithSources release) {
        Artifact artifact = ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(release.getRelease(),
                new Artifact("SW360"));
        release.getSourceAttachmentPaths().forEach(path ->
                artifact.addFact(new ArtifactSourceFile(path)));
        return artifact;
    }

    private Collection<SW360SparseRelease> getReleasesFromComponents(Collection<SW360SparseComponent> components) {
        return components.stream()
                .map(this::getIdFromHalResource)
                .filter(id -> !id.equals(""))
                .map(id -> connection.getComponentAdapter().getComponentById(id))
                .map(component -> component.orElse(null))
                .filter(Objects::nonNull)
                .flatMap(component -> component.getEmbedded().getReleases().stream())
                .collect(Collectors.toList());
    }

    private Collection<SW360Release> getNonApprovedReleasesFromSpareReleases(Collection<SW360SparseRelease> sw360SparseReleases) {
        return sw360SparseReleases.stream()
                .map(this::getIdFromHalResource)
                .filter(id -> !id.equals(""))
                .map(id -> connection.getReleaseAdapter().getReleaseById(id))
                .map(Optional::get)
                .filter(sw360Release -> !isApproved(sw360Release))
                .collect(Collectors.toList());
    }

    private boolean isApproved(SW360Release sw360Release) {
        return Optional.ofNullable(sw360Release.getClearingState())
                        .map(clearingState -> ArtifactClearingState.ClearingState.valueOf(clearingState) != ArtifactClearingState.ClearingState.INITIAL)
                        .orElse(false) &&
                Optional.ofNullable(sw360Release.getSw360ClearingState())
                        .map(sw360ClearingState -> sw360ClearingState.equals(SW360ClearingState.APPROVED) ||
                                sw360ClearingState.equals(SW360ClearingState.REPORT_AVAILABLE))
                        .orElse(false);
    }

    private <T extends SW360HalResource<?, ?>> String getIdFromHalResource(T halResource) {
        return SW360HalResourceUtility.getLastIndexOfSelfLink(halResource.getLinks().getSelf()).orElse("");
    }

    private static Comparator<SourcesExporter.ReleaseWithSources> releasesComparator() {
        return Comparator.comparing(releaseWithSources -> releaseWithSources.getRelease().getCreatedOn());
    }
}
