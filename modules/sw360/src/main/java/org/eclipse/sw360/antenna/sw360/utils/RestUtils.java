/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.*;

public class RestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static HttpEntity<String> getHttpEntity(Map<String, Object> resourceMap, HttpHeaders authBearerHeader) throws AntennaException {
        try {
            String jsonBody = objectMapper.writeValueAsString(resourceMap);
            return new HttpEntity<>(jsonBody, authBearerHeader);
        } catch (JsonProcessingException e) {
            throw new AntennaException("Error when attempting to serialise the request body.", e);
        }
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Project sw360Project, HttpHeaders header) throws AntennaException {
        Map<String, Object> project = new HashMap<>();
        project.put(SW360Attributes.PROJECT_NAME, sw360Project.getName());
        project.put(SW360Attributes.PROJECT_VERSION, sw360Project.getVersion());
        project.put(SW360Attributes.PROJECT_DESCRIPTION, sw360Project.getDescription());
        project.put(SW360Attributes.PROJECT_PROJECT_TYPE, sw360Project.getProjectType());
        project.put(SW360Attributes.PROJECT_BUSINESS_UNIT, sw360Project.getBusinessUnit());
        project.put(SW360Attributes.PROJECT_CLEARING_TEAM, sw360Project.getClearingTeam());
        project.put(SW360Attributes.PROJECT_VISIBILITY, sw360Project.getVisibility());
        return getHttpEntity(project, header);
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Component sw360Component, HttpHeaders header) throws AntennaException {
        Map<String, Object> component = new HashMap<>();
        component.put(SW360Attributes.COMPONENT_COMPONENT_NAME, sw360Component.getName());
        component.put(SW360Attributes.COMPONENT_COMPONENT_TYPE, sw360Component.getComponentType().toString());
        component.put(SW360Attributes.COMPONENT_HOMEPAGE, sw360Component.getHomepage());
        return getHttpEntity(component, header);
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Release sw360Release, HttpHeaders header) throws AntennaException {
        String componentId;
        if (sw360Release.get_Links() != null &&
                sw360Release.get_Links().getSelfComponent() != null) {
            componentId = SW360HalResourceUtility.getLastIndexOfLinkObject(sw360Release.get_Links().getSelfComponent()).orElse("");
        } else if (sw360Release.getComponentId() != null &&
                !sw360Release.getComponentId().isEmpty()) {
            componentId = sw360Release.getComponentId();
        } else {
            throw new AntennaException("No componentId found for release [" + sw360Release.getName() + "]");
        }

        Map<String, Object> release = new HashMap<>();
        release.put(SW360Attributes.RELEASE_COMPONENT_ID, componentId);
        release.put(SW360Attributes.RELEASE_ID, sw360Release.getReleaseId());
        release.put(SW360Attributes.RELEASE_NAME, sw360Release.getName());
        release.put(SW360Attributes.RELEASE_VERSION, sw360Release.getVersion());
        Optional.ofNullable(sw360Release.getCpeid()).ifPresent(cpeId -> release.put(SW360Attributes.RELEASE_CPE_ID, cpeId));
        Optional.ofNullable(sw360Release.getDownloadurl()).ifPresent(downloadurl -> release.put(SW360Attributes.RELEASE_SOURCES, downloadurl));
        Optional.ofNullable(sw360Release.getClearingState()).ifPresent(clearingState -> release.put(SW360Attributes.RELEASE_CLEARINGSTATE, clearingState));
        Optional.ofNullable(sw360Release.getMainLicenseIds()).ifPresent(mainLicenseIds -> release.put(SW360Attributes.RELEASE_MAIN_LICENSE_IDS, mainLicenseIds));
        Optional.ofNullable(convertSW360ExternalIdsToMapOfStrings(sw360Release)).ifPresent(externalIDs -> release.put(SW360Attributes.RELEASE_EXTERNAL_IDS, externalIDs));
        return getHttpEntity(release, header);
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360License sw360License, HttpHeaders header) throws AntennaException {
        String shortName = sw360License.getShortName() == null ? "" : sw360License.getShortName();
        String fullName = sw360License.getFullName() == null ? shortName : sw360License.getFullName();

        Map<String, Object> license = new HashMap<>();
        license.put(SW360Attributes.LICENSE_FULL_NAME, fullName);
        license.put(SW360Attributes.LICENSE_SHORT_NAME, shortName);
        license.put(SW360Attributes.LICENSE_TEXT, sw360License.getText());
        return getHttpEntity(license, header);
    }

    private static Map<String, String> convertSW360ExternalIdsToMapOfStrings(SW360Release sw360Release) {
        Map<String, String> externalIds = new HashMap<>();

        if(sw360Release.getCoordinates() != null) {
            externalIds.putAll(sw360Release.getCoordinates());
        }
        if(sw360Release.getFinalLicense() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_FLICENSES, sw360Release.getFinalLicense());
        }
        if(sw360Release.getDeclaredLicense() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_DLICENSES, sw360Release.getDeclaredLicense());
        }
        if(sw360Release.getObservedLicense() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_OLICENSES, sw360Release.getObservedLicense());
        }
        if(sw360Release.getReleaseTagUrl() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_OREPO, sw360Release.getReleaseTagUrl());
        }
        if(sw360Release.getSoftwareHeritageId() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_SWHID, sw360Release.getSoftwareHeritageId());
        }
        if(sw360Release.getHashes() != null) {
            externalIds.putAll(convertSetOfStringsOfHashesToMapOfStrings(sw360Release.getHashes()));
        }
        if(sw360Release.getChangeStatus() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_CHANGESTATUS, sw360Release.getChangeStatus());
        }
        if(sw360Release.getCopyrights() != null) {
            externalIds.put(SW360Attributes.RELEASE_EXTERNAL_ID_COPYRIGHTS, sw360Release.getCopyrights());
        }

        if(externalIds.isEmpty()) {
            return null;
        }
        return externalIds;
    }

    private static Map<String, String> convertSetOfStringsOfHashesToMapOfStrings(Set<String> stringSet) {
        Map<String, String> setMap = new HashMap<>();
        int i = 1;
        Iterator<String> it = stringSet.iterator();
        while(it.hasNext()){
            setMap.put(SW360Attributes.RELEASE_EXTERNAL_ID_HASHES + i, it.next());
            i++;
        }
        return setMap;
    }
}
