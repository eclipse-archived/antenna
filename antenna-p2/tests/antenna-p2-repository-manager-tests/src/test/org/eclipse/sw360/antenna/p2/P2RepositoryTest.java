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

package org.eclipse.sw360.antenna.p2;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class P2RepositoryTest {
    @Mock
    private ArtifactDownloader artifactDownloader;
    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private IInstallableUnit installableUnit;

    @Mock
    private IInstallableUnit installableSourceUnit;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Test
    public void resolveArtifactsDownloadsSourcesAndJars() throws P2Exception, IOException {
        P2Artifact artifact = new P2Artifact("TestBundle", Version.createOSGi(0, 0, 1));

        mockInstallableUnit(installableUnit, "TestBundle");
        mockDownloadFiles("dummy", installableUnit);
        mockInstallableUnit(installableSourceUnit, "TestBundle.source");
        mockDownloadFiles("dummysource", installableSourceUnit);

        P2Repository repository = new P2Repository(metadataRepository, artifactDownloader);

        repository.resolveArtifact(artifact);

        verify(artifactDownloader, times(2)).downloadInstallableUnits(any());
    }

    @Test
    public void resolveArtifactsDoesNotFailIfNothingCouldBeFound() throws P2Exception, IOException {
        P2Artifact artifact = new P2Artifact("TestBundle", Version.createOSGi(0, 0, 1));

        mockInstallableUnit(installableUnit, "TestBundle");
        when(metadataRepository.queryRepository("TestBundle.source")).thenReturn(new HashSet<>());
        mockDownloadFiles("dummy", installableUnit);

        P2Repository repository = new P2Repository(metadataRepository, artifactDownloader);

        repository.resolveArtifact(artifact);

        verify(artifactDownloader).downloadInstallableUnits(any());
    }

    @Test
    public void resolveArtifactsDoesNotDownloadJarIfNotMissing() throws P2Exception, IOException {
        P2Artifact artifact = new P2Artifact("TestBundle", Version.createOSGi(0, 0, 1));
        artifact.setJarPath(Paths.get("dummypath"));

        when(metadataRepository.queryRepository("TestBundle.source")).thenReturn(new HashSet<>());

        P2Repository repository = new P2Repository(metadataRepository, artifactDownloader);

        repository.resolveArtifact(artifact);

        verify(artifactDownloader, never()).downloadInstallableUnits(any());
    }

    @Test
    public void resolveArtifactsDoesNotDownloadAnythingIfEverythingIsPresent() throws P2Exception, IOException {
        P2Artifact artifact = new P2Artifact("TestBundle", Version.createOSGi(0, 0, 1));
        artifact.setJarPath(Paths.get("dummypath"));
        artifact.setSourcePath(Paths.get("otherdummypath"));

        P2Repository repository = new P2Repository(metadataRepository, artifactDownloader);

        repository.resolveArtifact(artifact);

        verify(metadataRepository, never()).queryRepository(anyString());

        verify(artifactDownloader, never()).downloadInstallableUnits(any());
    }

    @Test(expected = P2Exception.class)
    public void resolveArtifactsThrowsAnErroIfSeveralArtifactsAreReturnedFromDownload() throws P2Exception, IOException {
        P2Artifact artifact = new P2Artifact("TestBundle", Version.createOSGi(0, 0, 1));
        artifact.setJarPath(Paths.get("dummypath"));

        mockInstallableUnit(installableSourceUnit, "TestBundle.source");
        List<File> downloadedFiles = new ArrayList<>();
        downloadedFiles.add(new File("dummy"));
        downloadedFiles.add(new File("dummy2"));
        when(artifactDownloader.downloadInstallableUnits(installableSourceUnit)).thenReturn(downloadedFiles);

        P2Repository repository = new P2Repository(metadataRepository, artifactDownloader);

        repository.resolveArtifact(artifact);  // throws exception, because we cannot handle multiple files for the same artifact
    }

    @Test
    public void resolveArtifactsFindsCorrectArtifactAmongSeveral() throws P2Exception, IOException {
        P2Artifact artifact = new P2Artifact("TestBundle", Version.createOSGi(0, 0, 1));
        artifact.setSourcePath(Paths.get("dummypath"));

        IInstallableUnit installableUnit2 = Mockito.mock(IInstallableUnit.class);
        IInstallableUnit installableUnit3 = Mockito.mock(IInstallableUnit.class);
        when(installableUnit.getId()).thenReturn("TestBundle");
        when(installableUnit.getVersion()).thenReturn(Version.createOSGi(0, 0, 1));
        when(installableUnit2.getId()).thenReturn("TestBundle");
        when(installableUnit2.getVersion()).thenReturn(Version.createOSGi(1, 0, 1));
        when(installableUnit3.getId()).thenReturn("TestBundle");
        when(installableUnit3.getVersion()).thenReturn(Version.createOSGi(0, 0, 1, "v2"));
        Set<IInstallableUnit> installableUnits = new HashSet<>();
        installableUnits.add(installableUnit);
        installableUnits.add(installableUnit2);
        installableUnits.add(installableUnit3);
        when(metadataRepository.queryRepository("TestBundle")).thenReturn(installableUnits);
        mockDownloadFiles("dummy", installableUnit);

        P2Repository repository = new P2Repository(metadataRepository, artifactDownloader);

        repository.resolveArtifact(artifact);

        verify(artifactDownloader).downloadInstallableUnits(installableUnit);
        verify(artifactDownloader, never()).downloadInstallableUnits(installableUnit2);
        verify(artifactDownloader, never()).downloadInstallableUnits(installableUnit3);
    }

    private void mockDownloadFiles(String dummy, IInstallableUnit installableUnit) throws IOException {
        List<File> downloadedFiles = new ArrayList<>();
        downloadedFiles.add(new File(dummy));
        when(artifactDownloader.downloadInstallableUnits(installableUnit)).thenReturn(downloadedFiles);
    }

    private void mockInstallableUnit(IInstallableUnit installableUnit, String testBundle) {
        when(installableUnit.getId()).thenReturn(testBundle);
        when(installableUnit.getVersion()).thenReturn(Version.createOSGi(0, 0, 1));
        Set<IInstallableUnit> installableUnits = new HashSet<>();
        installableUnits.add(installableUnit);
        when(metadataRepository.queryRepository(testBundle)).thenReturn(installableUnits);
    }

}
