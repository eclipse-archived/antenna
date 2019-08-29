/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.rest.resource.releases;

import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360CoordinateKeysToArtifactCoordinates;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SW360Release extends SW360HalResource<SW360ReleaseLinkObjects, SW360ReleaseEmbedded> {
    private String releaseId;
    private String componentId;
    private String name;
    private String version;
    private String cpeid;
    private String downloadurl;
    private Set<String> mainLicenseIds;
    private Map<String, String> coordinates;

    private String finalLicense;
    private String declaredLicense;
    private String observedLicense;
    private String releaseTagUrl;
    private String softwareHeritageId;
    private Set<String> hashes;
    private String clearingState;
    private String changeStatus;
    private String copyrights;

    private Map<String, String> externalIds;

    public String getReleaseId() {
        return releaseId;
    }

    public SW360Release setReleaseId(String releaseId) {
        this.releaseId = releaseId;
        return this;
    }

    public String getComponentId() {
        return componentId;
    }

    public SW360Release setComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public SW360Release setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SW360Release setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCpeid() {
        return cpeid;
    }

    public SW360Release setCpeid(String cpeid) {
        this.cpeid = cpeid;
        return this;
    }

    public Set<String> getMainLicenseIds() {
        return mainLicenseIds;
    }

    public SW360Release setMainLicenseIds(Set<String> mainLicenseIds) {
        this.mainLicenseIds = mainLicenseIds;
        return this;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }


    public Map<String, String> getCoordinates() {
        return coordinates;
    }

    public SW360Release setCoordinates(Map<String, String> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public String getFinalLicense() {
        return finalLicense;
    }

    public SW360Release setFinalLicense(String finalLicense) {
        this.finalLicense = finalLicense;
        return this;
    }

    public String getDeclaredLicense() {
        return declaredLicense;
    }

    public SW360Release setDeclaredLicense(String declaredLicense) {
        this.declaredLicense = declaredLicense;
        return this;
    }

    public String getObservedLicense() {
        return observedLicense;
    }

    public SW360Release setObservedLicense(String observedLicense) {
        this.observedLicense = observedLicense;
        return this;
    }

    public String getReleaseTagUrl() {
        return releaseTagUrl;
    }

    public SW360Release setReleaseTagUrl(String releaseTagUrl) {
        this.releaseTagUrl = releaseTagUrl;
        return this;
    }

    public String getSoftwareHeritageId() {
        return softwareHeritageId;
    }

    public SW360Release setSoftwareHeritageId(String softwareHeritageId) {
        this.softwareHeritageId = softwareHeritageId;
        return this;
    }

    public Set<String> getHashes() {
        return hashes;
    }

    public SW360Release setHashes(Set<String> hashes) {
        this.hashes = hashes;
        return this;
    }

    public String getClearingState() {
        return clearingState;
    }

    public SW360Release setClearingState(String clearingState) {
        this.clearingState = clearingState;
        return this;
    }

    public String getChangeStatus() {
        return changeStatus;
    }


    public SW360Release setChangeStatus(String changeStatus) {
        this.changeStatus = changeStatus;
        return this;
    }

    public Map<String, String> getExternalIds() {
        return Optional.ofNullable(externalIds)
                .map(Map::entrySet)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(entry -> !entry.getValue().equals(""))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    public SW360Release setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
        mapExternalIdsOnVariable();
        return this;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public boolean shareIdentifier(SW360Release releaseCompare) {
        return this.name.equals(Optional.of(releaseCompare.getName()).orElse(""))
                && this.version.equals(Optional.of(releaseCompare.getVersion()).orElse(""));
    }

    private void mapExternalIdsOnVariable() {
        externalIds.forEach((key, value) -> {
            switch (key) {
                case SW360Attributes.RELEASE_EXTERNAL_ID_FLICENSES:
                    finalLicense = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_DLICENSES:
                    declaredLicense = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_OLICENSES:
                    observedLicense = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_OREPO:
                    releaseTagUrl = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_SWHID:
                    softwareHeritageId = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_CHANGESTATUS:
                    changeStatus = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_COPYRIGHTS:
                    copyrights = value;
                    break;
                case SW360Attributes.RELEASE_EXTERNAL_ID_CLEARINGSTATE:
                    clearingState = value;
                    break;
                default:
                    if (key.startsWith(SW360Attributes.RELEASE_EXTERNAL_ID_HASHES)) {
                        if(hashes == null) {
                            hashes = Collections.singleton(value);
                        } else {
                            hashes.add(value);
                        }
                    } else if (SW360CoordinateKeysToArtifactCoordinates.getValues().contains(key)) {
                        if(coordinates == null) {
                            coordinates = new HashMap<>();
                            coordinates.put(key, value);
                        } else {
                            coordinates.put(key, value);
                        }
                    }
            }
        });
    }

    public SW360Release mergeWith(SW360Release releaseWithPrecedence) {
        componentId = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getComponentId);
        cpeid = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getCpeid);
        downloadurl = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getDownloadurl);
        mainLicenseIds = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getMainLicenseIds);
        coordinates = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getCoordinates);
        finalLicense = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getFinalLicense);
        declaredLicense = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getDeclaredLicense);
        observedLicense = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getObservedLicense);
        releaseTagUrl = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getReleaseTagUrl);
        softwareHeritageId = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getSoftwareHeritageId);
        hashes = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getHashes);
        clearingState = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getClearingState);
        changeStatus = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getChangeStatus);
        copyrights =  getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getCopyrights);

        return this;
    }

    private <T> T getDominantGetterFromVariableMergeOrNull(SW360Release release, Function<SW360Release, T> getter) {
        return Optional.ofNullable(getter.apply(release))
                .orElse(getter.apply(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        SW360Release that = (SW360Release) o;
        return equalsInternallyCreatedSW360Release(that) &&
                Objects.equals(componentId, that.componentId) &&
                Objects.equals(externalIds, that.externalIds);
    }

    public boolean equalsInternallyCreatedSW360Release(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SW360Release that = (SW360Release) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(cpeid, that.cpeid) &&
                Objects.equals(downloadurl, that.downloadurl) &&
                Objects.equals(mainLicenseIds, that.mainLicenseIds) &&
                Objects.equals(coordinates, that.coordinates) &&
                Objects.equals(finalLicense, that.finalLicense) &&
                Objects.equals(declaredLicense, that.declaredLicense) &&
                Objects.equals(observedLicense, that.observedLicense) &&
                Objects.equals(releaseTagUrl, that.releaseTagUrl) &&
                Objects.equals(softwareHeritageId, that.softwareHeritageId) &&
                Objects.equals(hashes, that.hashes) &&
                Objects.equals(clearingState, that.clearingState) &&
                Objects.equals(changeStatus, that.changeStatus) &&
                Objects.equals(copyrights, that.copyrights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), componentId, name, version, cpeid, downloadurl, mainLicenseIds, coordinates, finalLicense, declaredLicense, observedLicense, releaseTagUrl, softwareHeritageId, hashes, clearingState, changeStatus, copyrights, externalIds);
    }
}
