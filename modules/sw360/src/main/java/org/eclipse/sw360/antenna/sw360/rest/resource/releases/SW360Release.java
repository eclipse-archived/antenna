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

import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SW360Release extends SW360HalResource<SW360ReleaseLinkObjects, SW360ReleaseEmbedded> {
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
    private Map<String, String> additionalData;


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
        return externalIds;
    }

    public SW360Release setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
        return this;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public SW360Release setAdditionalData(Map<String,String> additionalData) {
        this.additionalData = additionalData;
        return this;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public boolean shareIdentifier(SW360Release releaseCompare) {
        return this.componentId.equals(releaseCompare.getComponentId())
                && this.name.equals(releaseCompare.getName())
                && this.version.equals(releaseCompare.getVersion());
    }

    public SW360Release mergeWith(SW360Release releaseWithPrecedence) {
        cpeid = Optional.of(releaseWithPrecedence.getCpeid()).orElse(cpeid);
        downloadurl = Optional.of(releaseWithPrecedence.getDownloadurl()).orElse(downloadurl);
        mainLicenseIds = Optional.of(releaseWithPrecedence.getMainLicenseIds()).orElse(mainLicenseIds);
        coordinates = Optional.of(releaseWithPrecedence.getCoordinates()).orElse(coordinates);
        finalLicense = Optional.of(releaseWithPrecedence.getFinalLicense()).orElse(finalLicense);
        declaredLicense = Optional.of(releaseWithPrecedence.getDeclaredLicense()).orElse(declaredLicense);
        observedLicense = Optional.of(releaseWithPrecedence.getObservedLicense()).orElse(observedLicense);
        releaseTagUrl = Optional.of(releaseWithPrecedence.getReleaseTagUrl()).orElse(releaseTagUrl);
        softwareHeritageId = Optional.of(releaseWithPrecedence.getSoftwareHeritageId()).orElse(softwareHeritageId);
        hashes = Optional.of(releaseWithPrecedence.getHashes()).orElse(hashes);
        clearingState = Optional.of(releaseWithPrecedence.getClearingState()).orElse(clearingState);
        changeStatus = Optional.of(releaseWithPrecedence.getChangeStatus()).orElse(changeStatus);
        copyrights = Optional.of(releaseWithPrecedence.getCopyrights()).orElse(copyrights);
        externalIds = Optional.of(releaseWithPrecedence.getExternalIds()).orElse(externalIds);
        additionalData = Optional.of(releaseWithPrecedence.getAdditionalData()).orElse(additionalData);

        return this;
    }
}
