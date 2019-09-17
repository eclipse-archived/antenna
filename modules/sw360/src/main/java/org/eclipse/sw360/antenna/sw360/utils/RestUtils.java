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
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class RestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static HttpEntity<String> getHttpEntity(Map<String, Object> resourceMap, HttpHeaders authBearerHeader) throws ExecutionException {
        try {
            String jsonBody = objectMapper.writeValueAsString(resourceMap);
            return new HttpEntity<>(jsonBody, authBearerHeader);
        } catch (JsonProcessingException e) {
            throw new ExecutionException("Error when attempting to serialise the request body.", e);
        }
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Project sw360Project, HttpHeaders header) throws ExecutionException {
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

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Component sw360Component, HttpHeaders header) throws ExecutionException {
        Map<String, Object> component = new HashMap<>();
        component.put(SW360Attributes.COMPONENT_COMPONENT_NAME, sw360Component.getName());
        component.put(SW360Attributes.COMPONENT_COMPONENT_TYPE, sw360Component.getComponentType().toString());
        component.put(SW360Attributes.COMPONENT_HOMEPAGE, sw360Component.getHomepage());
        return getHttpEntity(component, header);
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Release sw360Release, HttpHeaders header) throws ExecutionException {
        try {
            String jsonBody = objectMapper.writeValueAsString(sw360Release);
            return new HttpEntity<>(jsonBody, header);
        } catch (JsonProcessingException e) {
            throw new ExecutionException("Error when attempting to serialise the request body.", e);
        }
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360License sw360License, HttpHeaders header) throws ExecutionException {
        String shortName = sw360License.getShortName() == null ? "" : sw360License.getShortName();
        String fullName = sw360License.getFullName() == null ? shortName : sw360License.getFullName();

        Map<String, Object> license = new HashMap<>();
        license.put(SW360Attributes.LICENSE_FULL_NAME, fullName);
        license.put(SW360Attributes.LICENSE_SHORT_NAME, shortName);
        license.put(SW360Attributes.LICENSE_TEXT, sw360License.getText());
        return getHttpEntity(license, header);
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Attachment sw360Attachment, HttpHeaders header) throws ExecutionException {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("filename", sw360Attachment.getFilename());
        attachment.put("attachmentType", sw360Attachment.getAttachmentType());
        attachment.put("checkStatus", "NOTCHECKED");
        return getHttpEntity(attachment, header);
    }

    public static HttpHeaders deepCopyHeaders(HttpHeaders header) {
        return header.entrySet()
                .stream()
                .collect(HttpHeaders::new,
                        (h, e) -> e.getValue().forEach( v -> h.add(e.getKey(), v)),
                        (h1, h2) -> {throw new UnsupportedOperationException("Unsupported operation");});
    }
}
