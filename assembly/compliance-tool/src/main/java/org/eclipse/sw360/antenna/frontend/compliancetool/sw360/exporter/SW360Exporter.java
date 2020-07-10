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
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToReleaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
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
     * The configuration property that controls whether source files that are
     * not referenced by any of the current components should be removed. If
     * set to <strong>true</strong>, obsolete source attachments are cleaned up
     * automatically.
     */
    public static final String PROP_REMOVE_SOURCES = "removeUnreferencedSources";

    /**
     * A {@code Comparator} for sorting {@code ReleaseWithSources} objects.
     * This comparator is used to sort the list of releases before it is
     * written to the output CSV file.
     */
    static final Comparator<ReleaseWithSources> RELEASES_COMPARATOR = createReleaseComparator();

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

        Collection<ReleaseWithSources> nonApprovedReleasesWithSources =
                sourcesExporter.downloadSources(connection.getReleaseAdapterAsync(), sw360ReleasesNotApproved);

        List<Artifact> artifacts = nonApprovedReleasesWithSources.stream()
                .sorted(RELEASES_COMPARATOR)
                .map(this::releaseAsArtifact)
                .collect(Collectors.toList());

        File csvFile =  configuration.getCsvFilePath()
                .toFile();

        CSVArtifactMapper csvArtifactMapper = new CSVArtifactMapper(csvFile.toPath(),
                Charset.forName(configuration.getProperty(PROP_ENCODING)),
                configuration.getProperty(PROP_DELIMITER).charAt(0),
                configuration.getBaseDir());

        csvArtifactMapper.writeArtifactsToCsvFile(artifacts);

        if (Boolean.parseBoolean(configuration.getProperty(PROP_REMOVE_SOURCES))) {
            sourcesExporter.removeUnreferencedFiles(nonApprovedReleasesWithSources);
        }

        LOGGER.info("The SW360Exporter was executed with the following configuration:");
        configuration.logConfiguration(LOGGER);
    }

    private Artifact releaseAsArtifact(ReleaseWithSources release) {
        Artifact artifact = ArtifactToReleaseUtils.convertToArtifactWithoutSourceFile(release.getRelease(),
                new Artifact("SW360"));
        addSourceAttachment(release, artifact);
        return artifact;
    }

    private Collection<SW360SparseRelease> getReleasesFromComponents(Collection<SW360SparseComponent> components) {
        return components.stream()
                .map(SW360HalResource::getId)
                .filter(Objects::nonNull)
                .map(id -> connection.getComponentAdapter().getComponentById(id))
                .map(component -> component.orElse(null))
                .filter(Objects::nonNull)
                .flatMap(component -> component.getEmbedded().getReleases().stream())
                .collect(Collectors.toList());
    }

    private Collection<SW360Release> getNonApprovedReleasesFromSpareReleases(Collection<SW360SparseRelease> sw360SparseReleases) {
        return sw360SparseReleases.stream()
                .map(SW360HalResource::getId)
                .filter(Objects::nonNull)
                .map(id -> connection.getReleaseAdapter().getReleaseById(id))
                .map(Optional::get)
                .filter(sw360Release -> !ComplianceFeatureUtils.isApproved(sw360Release))
                .collect(Collectors.toList());
    }

    /**
     * Adds the single source attachment to the given artifact if it exists.
     * A release in SW360 may be assigned multiple source attachments, but for
     * the workflow of the compliance tool, we support only a single one. So if
     * multiple source attachments exist, they are all downloaded, but no
     * source path is added to the artifact; during curation, the correct
     * source artifact must be selected explicitly.
     *
     * @param release  the release
     * @param artifact the artifact to be updated
     */
    private static void addSourceAttachment(ReleaseWithSources release, Artifact artifact) {
        if (release.getSourceAttachmentPaths().size() == 1) {
            release.getSourceAttachmentPaths().forEach(path ->
                    artifact.addFact(new ArtifactSourceFile(path)));
        }
    }

    /**
     * Creates a {@code Comparator} for sorting a list of
     * {@code ReleaseWithSources} objects.
     *
     * @return the {@code Comparator}
     */
    private static Comparator<ReleaseWithSources> createReleaseComparator() {
        Comparator<ReleaseWithSources> cCreatedAsc = Comparator.comparing(rel -> rel.getRelease().getCreatedOn());
        Comparator<ReleaseWithSources> cCreated = cCreatedAsc.reversed();
        return cCreated.thenComparing(rel -> rel.getRelease().getName())
                .thenComparing(rel -> rel.getRelease().getVersion());
    }
}
