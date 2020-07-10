/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.utils.ArtifactToComponentUtils;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360MetaDataReceiverTest {
    private SW360MetaDataReceiver metaDataReceiver;
    private SW360Connection connection = mock(SW360Connection.class);
    private SW360LicenseClientAdapter licenseClientAdapter = mock(SW360LicenseClientAdapter.class);
    private SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);
    private SW360ComponentClientAdapter componentClientAdapter = mock(SW360ComponentClientAdapter.class);

    public void setUp() {
        when(connection.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);
        when(connection.getComponentAdapter())
                .thenReturn(componentClientAdapter);
        when(connection.getLicenseAdapter())
                .thenReturn(licenseClientAdapter);

        metaDataReceiver = new SW360MetaDataReceiver(connection);
    }

    @Test
    public void testFindReleaseForArtifactPresentComponent() {
        final String version = "version";
        final Artifact artifact = new Artifact()
                .addFact(new ArtifactCoordinates(new Coordinate("name", version)));
        final String componentName = ArtifactToComponentUtils.createComponentName(artifact);
        final SW360Component sw360Component = new SW360Component();
        final SW360Release release = new SW360Release();
        when(componentClientAdapter.getComponentByName(componentName))
                .thenReturn(Optional.of(sw360Component));
        when(releaseClientAdapter.getReleaseByVersion(sw360Component, version))
                .thenReturn(Optional.of(release));
        setUp();
        final Optional<SW360Release> releaseForArtifact = metaDataReceiver.findReleaseForArtifact(artifact);

        assertThat(releaseForArtifact).isPresent();
        assertThat(releaseForArtifact).hasValue(release);
        verify(componentClientAdapter, times(1)).getComponentByName(componentName);
        verify(releaseClientAdapter, times(1)).getReleaseByVersion(sw360Component, version);
    }

    @Test
    public void testFindReleaseForArtifactNotPresentComponent() {
        final String version = "version";
        final Artifact artifact = new Artifact()
                .addFact(new ArtifactCoordinates(new Coordinate("name", version)));
        final String componentName = ArtifactToComponentUtils.createComponentName(artifact);
        when(componentClientAdapter.getComponentByName(componentName))
                .thenReturn(Optional.empty());
        setUp();
        final Optional<SW360Release> releaseForArtifact = metaDataReceiver.findReleaseForArtifact(artifact);

        assertThat(releaseForArtifact).isNotPresent();
        verify(componentClientAdapter, times(1)).getComponentByName(componentName);
        verify(releaseClientAdapter, never()).getReleaseByVersion(any(), any());
    }

    @Test
    public void testFindReleaseForArtifactWithNoCoordinate() {
        setUp();

        final Optional<SW360Release> releaseForArtifact = metaDataReceiver.findReleaseForArtifact(new Artifact());

        assertThat(releaseForArtifact).isNotPresent();
        verify(componentClientAdapter, never()).getComponentByName(any());
    }

    @Test
    public void testGetLicenseDetails() {
        SW360SparseLicense sparseLicense = new SW360SparseLicense();
        SW360License sw360License = new SW360License();
        when(licenseClientAdapter.enrichSparseLicense(sparseLicense))
                .thenReturn(sw360License);
        setUp();

        Optional<SW360License> licenseDetails = metaDataReceiver.getLicenseDetails(sparseLicense);

        assertThat(licenseDetails).hasValue(sw360License);
        verify(licenseClientAdapter, times(1)).enrichSparseLicense(sparseLicense);
    }

    @Test
    public void testGetLicenseDetailsException() {
        SW360SparseLicense license = new SW360SparseLicense().setShortName("foo");
        when(licenseClientAdapter.enrichSparseLicense(license))
                .thenThrow(new SW360ClientException("License lookup failed"));
        setUp();

        Optional<SW360License> licenseDetails = metaDataReceiver.getLicenseDetails(license);
        assertThat(licenseDetails).isEmpty();
    }

    @Test
    public void testDownloadAttachment() {
        final Path downloadPath = Paths.get("");
        final SW360Release release = new SW360Release();
        final SW360SparseAttachment attachment = new SW360SparseAttachment().setFilename("attachmentFile.file");
        final Path attachmentPath = downloadPath.resolve(attachment.getFilename());
        when(releaseClientAdapter.downloadAttachment(release, attachment, downloadPath))
                .thenReturn(Optional.of(attachmentPath));
        setUp();

        final Optional<Path> downloadedAttachmentPath = metaDataReceiver.downloadAttachment(release, attachment, downloadPath);

        assertThat(downloadedAttachmentPath).isPresent();
        assertThat(downloadedAttachmentPath).hasValue(attachmentPath);
        verify(releaseClientAdapter, times(1)).downloadAttachment(release, attachment, downloadPath);
    }
}