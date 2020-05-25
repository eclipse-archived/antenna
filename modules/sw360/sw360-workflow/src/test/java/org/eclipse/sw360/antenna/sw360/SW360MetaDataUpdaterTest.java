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

import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadRequest;
import org.eclipse.sw360.antenna.sw360.client.adapter.AttachmentUploadResult;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360Visibility;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360ProjectType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SW360MetaDataUpdaterTest {
    private SW360MetaDataUpdater metaDataUpdater;
    private final SW360Connection connection = mock(SW360Connection.class);
    private final SW360ProjectClientAdapter projectClientAdapter = mock(SW360ProjectClientAdapter.class);
    private final SW360LicenseClientAdapter licenseClientAdapter = mock(SW360LicenseClientAdapter.class);
    private final SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);

    public void setUp(boolean uploadSources, boolean updateReleases) {
        when(connection.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);
        when(connection.getProjectAdapter())
                .thenReturn(projectClientAdapter);
        when(connection.getLicenseAdapter())
                .thenReturn(licenseClientAdapter);

        metaDataUpdater = new SW360MetaDataUpdater(connection, updateReleases, uploadSources);
    }

    @Test
    public void testGetLicensesWithExistingLicense() {
        final String licenseName = "licenseName";
        final SW360License license = new SW360License()
                .setShortName(licenseName);
        License licenseAntenna = new License();
        licenseAntenna.setId(licenseName);
        when(licenseClientAdapter.isLicenseOfArtifactAvailable(licenseName))
                .thenReturn(true);
        when(licenseClientAdapter.getSW360LicenseByAntennaLicense(licenseName))
                .thenReturn(Optional.of(license));
        setUp(true, false);

        final Set<SW360License> licenses = metaDataUpdater.getLicenses(Collections.singletonList(licenseAntenna));

        assertThat(licenses).hasSize(1);

        verify(licenseClientAdapter, times(1)).isLicenseOfArtifactAvailable(licenseName);
        verify(licenseClientAdapter, times(1)).getSW360LicenseByAntennaLicense(licenseName);
    }

    @Test
    public void testGetLicensesWithNonExistingLicense() {
        final String licenseName = "licenseName";
        final SW360License license = new SW360License()
                .setShortName(licenseName);
        License licenseAntenna = new License();
        licenseAntenna.setId(licenseName);
        when(licenseClientAdapter.isLicenseOfArtifactAvailable(licenseName))
                .thenReturn(false);
        when(licenseClientAdapter.getSW360LicenseByAntennaLicense(licenseName))
                .thenReturn(Optional.of(license));
        setUp(true, false);

        final Set<SW360License> licenses = metaDataUpdater.getLicenses(Collections.singletonList(licenseAntenna));

        assertThat(licenses).hasSize(0);

        verify(licenseClientAdapter, times(1)).isLicenseOfArtifactAvailable(licenseName);
    }

    @Test
    public void testGetOrCreateRelease() {
        final SW360Release release = new SW360Release();
        release.setName("test-component");
        release.setVersion("0.1-alpha");
        final SW360Release newRelease = new SW360Release();
        when(releaseClientAdapter.getSparseReleaseByExternalIds(any())).thenReturn(Optional.empty());
        when(releaseClientAdapter.getSparseReleaseByNameAndVersion(release.getName(), release.getVersion()))
                .thenReturn(Optional.empty());
        when(releaseClientAdapter.createRelease(release)).thenReturn(newRelease);
        setUp(true, true);

        assertThat(metaDataUpdater.getOrCreateRelease(release)).isEqualTo(newRelease);
        verify(releaseClientAdapter, never()).updateRelease(any());
    }

    @Test
    public void testGetOrCreateReleaseFoundByExternalIDs() {
        SW360SparseRelease sparseRelease = new SW360SparseRelease();
        SW360Release foundRelease = new SW360Release();
        SW360Release queryRelease = new SW360Release();
        SW360Release patchedRelease = new SW360Release();
        Map<String, String> extIDs = Collections.singletonMap("foo", "bar");
        final String copyright = "(C) Test copyright";
        queryRelease.setExternalIds(extIDs);
        foundRelease.setCopyrights(copyright);
        when(releaseClientAdapter.getSparseReleaseByExternalIds(extIDs)).thenReturn(Optional.of(sparseRelease));
        when(releaseClientAdapter.enrichSparseRelease(sparseRelease)).thenReturn(Optional.of(foundRelease));
        when(releaseClientAdapter.updateRelease(any()))
                .thenAnswer((Answer<SW360Release>) invocationOnMock -> {
                    SW360Release rel = invocationOnMock.getArgument(0);
                    assertThat(rel.getExternalIds()).isEqualTo(extIDs);
                    assertThat(rel.getCopyrights()).isEqualTo(copyright);
                    return patchedRelease;
                });
        setUp(true, true);

        assertThat(metaDataUpdater.getOrCreateRelease(queryRelease)).isEqualTo(patchedRelease);
    }

    @Test
    public void testGetOrCreateReleaseFoundByNameAndVersion() {
        SW360SparseRelease sparseRelease = new SW360SparseRelease();
        SW360Release foundRelease = new SW360Release();
        SW360Release queryRelease = new SW360Release();
        queryRelease.setExternalIds(Collections.singletonMap("id", "42"));
        foundRelease.setExternalIds(Collections.singletonMap("id2", "47"));
        queryRelease.setName("theComponent");
        queryRelease.setVersion("100.0");
        when(releaseClientAdapter.getSparseReleaseByExternalIds(queryRelease.getExternalIds())).thenReturn(Optional.empty());
        when(releaseClientAdapter.getSparseReleaseByNameAndVersion(queryRelease.getName(), queryRelease.getVersion()))
                .thenReturn(Optional.of(sparseRelease));
        when(releaseClientAdapter.enrichSparseRelease(sparseRelease)).thenReturn(Optional.of(foundRelease));
        setUp(true, false);

        assertThat(metaDataUpdater.getOrCreateRelease(queryRelease)).isEqualTo(queryRelease);
        assertThat(queryRelease.getExternalIds()).containsKey("id2");
        verify(releaseClientAdapter, never()).updateRelease(any());
    }

    @Test
    public void testCreateProjectWithProjectPresent() {
        final String projectName = "projectName";
        final String projectVersion = "projectVersion";
        final String projectId = "12345";
        SW360Project project = new SW360Project();
        project.getLinks().setSelf(new Self("https://sw360.org/projects/" + projectId));
        when(projectClientAdapter.getProjectByNameAndVersion(projectName, projectVersion))
                .thenReturn(Optional.of(project));
        setUp(true, false);

        metaDataUpdater.createProject(projectName, projectVersion, Collections.emptySet());

        verify(projectClientAdapter, never()).createProject(any());
        verify(projectClientAdapter, times(1)).addSW360ReleasesToSW360Project(projectId, Collections.emptySet());
    }

    @Test
    public void testCreateProjectWithProjectNotPresent() {
        final String projectName = "projectName";
        final String projectVersion = "projectVersion";
        final String projectId = "12345";
        final SW360Project newProject = new SW360Project();
        newProject.setName(projectName);
        newProject.setVersion(projectVersion);
        newProject.getLinks().setSelf(new Self("http://some.link/" + projectId));
        when(projectClientAdapter.getProjectByNameAndVersion(projectName, projectVersion))
                .thenReturn(Optional.empty());
        when(projectClientAdapter.createProject(any()))
                .thenReturn(newProject);
        setUp(true, false);

        metaDataUpdater.createProject(projectName, projectVersion, Collections.emptySet());

        ArgumentCaptor<SW360Project> captor = ArgumentCaptor.forClass(SW360Project.class);
        verify(projectClientAdapter, times(1)).createProject(captor.capture());
        verify(projectClientAdapter, times(1)).addSW360ReleasesToSW360Project(projectId, Collections.emptySet());
        SW360Project sampleProject = captor.getValue();
        assertThat(sampleProject.getName()).isEqualTo(projectName);
        assertThat(sampleProject.getVersion()).isEqualTo(projectVersion);
        assertThat(sampleProject.getDescription()).isEqualTo(projectName + " " + projectVersion);
        assertThat(sampleProject.getProjectType()).isEqualTo(SW360ProjectType.PRODUCT);
        assertThat(sampleProject.getVisibility()).isEqualTo(SW360Visibility.BUISNESSUNIT_AND_MODERATORS);
    }

    @Test
    public void testIsUploadSources() {
        setUp(true, false);
        assertThat(metaDataUpdater.isUploadSources()).isEqualTo(true);
    }

    @Test
    public void testUploadAttachments() {
        final SW360Release release = new SW360Release();
        Path uploadPath = Paths.get("upload.doc");
        SW360AttachmentType attachmentType = SW360AttachmentType.SOURCE;
        Map<Path, SW360AttachmentType> attachments = Collections.singletonMap(uploadPath, attachmentType);
        AttachmentUploadRequest expRequest = AttachmentUploadRequest.builder(release)
                .addAttachment(uploadPath, attachmentType)
                .build();
        AttachmentUploadResult result = new AttachmentUploadResult(release);
        when(releaseClientAdapter.uploadAttachments(expRequest))
                .thenReturn(result);

        setUp(true, false);

        final SW360Release releaseWithAttachment = metaDataUpdater.uploadAttachments(release, attachments);

        assertThat(releaseWithAttachment).isEqualTo(release);
        verify(releaseClientAdapter).uploadAttachments(expRequest);
    }
}