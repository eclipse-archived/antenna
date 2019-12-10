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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SW360Release extends SW360HalResource<SW360ReleaseLinkObjects, SW360ReleaseEmbedded> {

    private static final String OVERRIDDEN_LICENSES_KEY = "overridden_license";
    private static final String DECLARED_LICENSE_KEY = "declared_license";
    private static final String OBSERVED_LICENSES_KEY = "observed_license";
    private static final String RELEASE_TAG_URL_KEY = "release_tag";
    private static final String SOFTWARE_HERITAGE_ID_KEY = "swh";
    private static final String HASHES_PREFIX = "hash_";
    private static final String CHANGESTATUS_KEY = "change_status";
    private static final String COPYRIGHTS_KEY = "copyrights";
    private static final String CLEARINGSTATE_KEY = "clearingState";

    @JsonIgnore
    private boolean isProprietary;
    private String name;
    private String version;
    private String cpeId;
    private String downloadurl;
    private final Map<String, String> externalIds = new HashMap<>();
    @JsonSerialize
    private final Map<String, String> additionalData = new HashMap<>();

    @JsonIgnore
    public String getReleaseId() {
        return Optional.ofNullable(get_Links())
                .map(SW360ReleaseLinkObjects::getSelf)
                .flatMap(SW360HalResourceUtility::getLastIndexOfSelfLink)
                .orElse(null);
    }

    public SW360Release setReleaseId(String releaseId) {
        get_Links().setSelf(new Self(releaseId)); // TODO
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getComponentId() {
        return Optional.ofNullable(get_Links())
                .map(SW360ReleaseLinkObjects::getSelfComponent)
                .flatMap(SW360HalResourceUtility::getLastIndexOfSelfLink)
                .orElse(null);
    }

    public SW360Release setComponentId(String componentId) {
        get_Links().setSelfComponent(new Self(componentId)); // TODO
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getName() {
        return name;
    }

    public SW360Release setName(String name) {
        this.name = name;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getVersion() {
        return version;
    }

    public SW360Release setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCpeId() {
        return cpeId;
    }

    public SW360Release setCpeId(String cpeId) {
        this.cpeId = cpeId;
        return this;
    }

    @JsonIgnore
    public boolean isSetMainLicenseIds() {
        return !get_Embedded().getLicenses().isEmpty();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<String> getMainLicenseIds() {
        return Optional.ofNullable(get_Embedded().getLicenses())
                .map(lics -> lics
                        .stream()
                        .map(SW360SparseLicense::getShortName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public SW360Release setMainLicenseIds(Set<String> mainLicenseIds) {
        if (mainLicenseIds.size() > 0) {
            List<SW360SparseLicense> licenses = mainLicenseIds.stream()
                    .map(licenseId -> new SW360SparseLicense()
                            .setShortName(licenseId))
                    .collect(Collectors.toList());
            get_Embedded().setLicenses(licenses);
        }
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    @JsonIgnore
    public Map<String, String> getCoordinates() {
        return externalIds.entrySet().stream()
                .filter(e -> Coordinate.Types.all.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public SW360Release setCoordinates(Map<String, String> coordinates) {
        externalIds.putAll(coordinates);
        return this;
    }

    @JsonIgnore
    public String getOverriddenLicense() {
        return additionalData.get(OVERRIDDEN_LICENSES_KEY);
    }

    public SW360Release setOverriddenLicense(String overriddenLicense) {
        additionalData.put(OVERRIDDEN_LICENSES_KEY, overriddenLicense);
        return this;
    }

    @JsonIgnore
    public String getDeclaredLicense() {
        return additionalData.get(DECLARED_LICENSE_KEY);
    }

    public SW360Release setDeclaredLicense(String declaredLicense) {
        additionalData.put(DECLARED_LICENSE_KEY, declaredLicense);
        return this;
    }

    @JsonIgnore
    public String getObservedLicense() {
        return additionalData.get(OBSERVED_LICENSES_KEY);
    }

    public SW360Release setObservedLicense(String observedLicense) {
        additionalData.put(OBSERVED_LICENSES_KEY, observedLicense);
        return this;
    }

    @JsonIgnore
    public String getReleaseTagUrl() {
        return externalIds.get(RELEASE_TAG_URL_KEY);
    }

    public SW360Release setReleaseTagUrl(String releaseTagUrl) {
        externalIds.put(RELEASE_TAG_URL_KEY, releaseTagUrl);
        return this;
    }

    @JsonIgnore
    public String getSoftwareHeritageId() {
        return externalIds.get(SOFTWARE_HERITAGE_ID_KEY);
    }

    public SW360Release setSoftwareHeritageId(String softwareHeritageId) {
        externalIds.put(SOFTWARE_HERITAGE_ID_KEY, softwareHeritageId);
        return this;
    }

    @JsonIgnore
    public Set<String> getHashes() {
        return externalIds.entrySet().stream()
                .filter(e -> e.getKey().startsWith(HASHES_PREFIX))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    private void dropAllHashes() {
        externalIds.keySet().stream()
                .filter(s -> s.startsWith(HASHES_PREFIX))
                .forEach(externalIds::remove);
    }

    public SW360Release setHashes(Set<String> hashes) {
        dropAllHashes();

        int i = 1;
        for (String hash : hashes) {
            if (hash != null && !hash.isEmpty()) {
                externalIds.put(HASHES_PREFIX + i, hash);
                i++;
            }
        }
        return this;
    }

    @JsonIgnore
    public String getClearingState() {
        return additionalData.get(CLEARINGSTATE_KEY);
    }

    public SW360Release setClearingState(String clearingState) {
        additionalData.put(CLEARINGSTATE_KEY, clearingState);
        return this;
    }

    @JsonIgnore
    public String getChangeStatus() {
        return additionalData.get(CHANGESTATUS_KEY);
    }


    public SW360Release setChangeStatus(String changeStatus) {
        additionalData.put(CHANGESTATUS_KEY, changeStatus);
        return this;
    }

    @JsonIgnore
    public String getCopyrights() {
        return additionalData.get(COPYRIGHTS_KEY);
    }

    public SW360Release setCopyrights(String copyrights) {
        additionalData.put(COPYRIGHTS_KEY, copyrights);
        return this;
    }

    @JsonIgnore
    public boolean isProprietary() {
        return isProprietary;
    }

    @JsonIgnore
    public SW360Release setProprietary(boolean proprietary) {
        isProprietary = proprietary;
        return this;
    }

    public Map<String,String> getExternalIds() {
        return new HashMap<>(externalIds);
    }

    public SW360Release setExternalIds(Map<String, String> externalIds) {
        this.externalIds.putAll(externalIds);
        return this;
    }

    public SW360Release setAdditionalData(Map<String, String> additionalData) {
        this.additionalData.putAll(additionalData);
        return this;
    }

    public boolean shareIdentifier(SW360Release releaseCompare) {
        return this.name.equals(Optional.of(releaseCompare.getName()).orElse(""))
                && this.version.equals(Optional.of(releaseCompare.getVersion()).orElse(""));
    }

    public SW360Release mergeWith(SW360Release releaseWithPrecedence) {
        name = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getName);
        version = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getVersion);
        cpeId = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getCpeId);
        downloadurl = getDominantGetterFromVariableMergeOrNull(releaseWithPrecedence, SW360Release::getDownloadurl);
        if (releaseWithPrecedence.isSetMainLicenseIds()) {
            setMainLicenseIds(releaseWithPrecedence.getMainLicenseIds());
        }
        Self releaseIdWithPrecedence = releaseWithPrecedence.get_Links().getSelf();
        if (releaseIdWithPrecedence != null && !releaseIdWithPrecedence.getHref().isEmpty()) {
            get_Links().setSelf(releaseIdWithPrecedence);
        }
        Self componentIdWithPrecedence = releaseWithPrecedence.get_Links().getSelfComponent();
        if (componentIdWithPrecedence != null && !componentIdWithPrecedence.getHref().isEmpty()) {
            get_Links().setSelfComponent(componentIdWithPrecedence);
        }
        externalIds.putAll(releaseWithPrecedence.externalIds);
        additionalData.putAll(releaseWithPrecedence.additionalData);

        return this;
    }

    private <T> T getDominantGetterFromVariableMergeOrNull(SW360Release release, Function<SW360Release, T> getter) {
        return Optional.ofNullable(getter.apply(release))
                .orElse(getter.apply(this));
    }

    @Override
    public SW360ReleaseLinkObjects createEmptyLinks() {
        return new SW360ReleaseLinkObjects();
    }

    @Override
    public SW360ReleaseEmbedded createEmptyEmbedded() {
        return new SW360ReleaseEmbedded();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SW360Release release = (SW360Release) o;
        return Objects.equals(name, release.name) &&
                Objects.equals(version, release.version) &&
                Objects.equals(cpeId, release.cpeId) &&
                Objects.equals(downloadurl, release.downloadurl) &&
                Objects.equals(externalIds, release.externalIds) &&
                Objects.equals(additionalData, release.additionalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, version, cpeId, downloadurl, externalIds, additionalData);
    }
}
