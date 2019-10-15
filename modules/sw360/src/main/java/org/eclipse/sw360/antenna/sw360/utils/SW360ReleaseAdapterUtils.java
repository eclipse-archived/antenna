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
package org.eclipse.sw360.antenna.sw360.utils;

import com.github.packageurl.PackageURL;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SW360ReleaseAdapterUtils {

    public static SW360Release prepareRelease(SW360Component component, Set<String> sw360LicenseIds, Artifact artifact) {
        SW360Release release = new SW360Release();
        String componentId = SW360HalResourceUtility.getLastIndexOfSelfLink(component.get_Links()).orElse("");

        SW360ReleaseAdapterUtils.setVersion(release, artifact);
        SW360ReleaseAdapterUtils.setCPEId(release, artifact);
        release.setName(component.getName());
        release.setComponentId(componentId);
        release.setMainLicenseIds(sw360LicenseIds);

        SW360ReleaseAdapterUtils.setPackageUrls(release, artifact);
        SW360ReleaseAdapterUtils.setOverriddenLicense(release, artifact);
        SW360ReleaseAdapterUtils.setDeclaredLicense(release, artifact);
        SW360ReleaseAdapterUtils.setObservedLicense(release, artifact);
        SW360ReleaseAdapterUtils.setSources(release, artifact);
        SW360ReleaseAdapterUtils.setOriginalRepo(release, artifact);
        SW360ReleaseAdapterUtils.setSwhId(release, artifact);
        SW360ReleaseAdapterUtils.setHashes(release, artifact);
        SW360ReleaseAdapterUtils.setClearingStatus(release, artifact);
        SW360ReleaseAdapterUtils.setChangeStatus(release, artifact);
        SW360ReleaseAdapterUtils.setCopyrights(release, artifact);

        return release;
    }

    public static boolean isValidRelease(SW360Release release) {
        if (release.getName() == null || release.getName().isEmpty()) {
            return false;
        }
        return release.getVersion() != null && !release.getVersion().isEmpty();
    }

    public static String createSW360ReleaseVersion(Artifact artifact) {
        return SW360ComponentAdapterUtils.createComponentVersion(artifact);
    }

    public static void setVersion(SW360Release release, Artifact artifact) {
        final String version = SW360ReleaseAdapterUtils.createSW360ReleaseVersion(artifact);
        if (!version.isEmpty()) {
            release.setVersion(version);
        }
    }

    private static void setCPEId(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactCPE.class)
                .ifPresent(release::setCpeId);
    }

    private static void setPackageUrls(SW360Release sw360Release, Artifact artifact) {
        Map<String, String> packageURLS = artifact.askForAll(ArtifactCoordinates.class).stream()
                .map(ArtifactCoordinates::getPurl)
                .collect(Collectors.toMap(PackageURL::getType, PackageURL::canonicalize));

        sw360Release.setPurls(packageURLS);
    }

    private static void setOverriddenLicense(SW360Release release, Artifact artifact) {
        artifact.askForGet(OverriddenLicenseInformation.class)
                .ifPresent(licenseInformation -> release.setOverriddenLicense(licenseInformation.evaluate()));
    }

    private static void setDeclaredLicense(SW360Release release, Artifact artifact) {
        artifact.askForGet(DeclaredLicenseInformation.class)
                .ifPresent(licenseInformation -> release.setDeclaredLicense(licenseInformation.evaluate()));
    }

    private static void setObservedLicense(SW360Release release, Artifact artifact) {
        artifact.askForGet(ObservedLicenseInformation.class)
                .ifPresent(licenseInformation -> release.setObservedLicense(licenseInformation.evaluate()));
    }

    private static void setSources(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactSourceUrl.class)
                .ifPresent(release::setDownloadurl);
    }

    private static void setOriginalRepo(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactReleaseTagURL.class)
                .ifPresent(release::setReleaseTagUrl);
    }

    private static void setSwhId(SW360Release release, Artifact artifact) {
        artifact.askForGet(ArtifactSoftwareHeritageID.class)
                .ifPresent(release::setSoftwareHeritageId);
    }

    private static void setHashes(SW360Release release, Artifact artifact) {
        Set<String> hashList = artifact.askForAll(ArtifactFilename.class)
                .stream()
                .map(ArtifactFilename::getArtifactFilenameEntries)
                .flatMap(Collection::stream)
                .map(ArtifactFilename.ArtifactFilenameEntry::getHash)
                .collect(Collectors.toSet());
        release.setHashes(hashList);
    }

    private static void setClearingStatus(SW360Release release, Artifact artifact) {
        Optional<ArtifactClearingState.ClearingState> cs = artifact.askForGet(ArtifactClearingState.class);
        cs.ifPresent(clearingState -> release.setClearingState(clearingState.toString()));
    }

    private static void setChangeStatus(SW360Release release, Artifact artifact) {
        Optional<ArtifactChangeStatus.ChangeStatus> cs = artifact.askForGet(ArtifactChangeStatus.class);
        cs.ifPresent(changeStatus -> release.setChangeStatus(changeStatus.toString()));
    }

    private static void setCopyrights(SW360Release release, Artifact artifact) {
        Optional<CopyrightStatement> cs = artifact.askFor(CopyrightStatement.class);
        cs.ifPresent(copyrightStatement -> release.setCopyrights(copyrightStatement.toString()));
    }
}