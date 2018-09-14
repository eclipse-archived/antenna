/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class RestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static HttpEntity<String> getHttpEntity(Map<String, Object> resourceMap, HttpHeaders authBearerHeader) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(resourceMap);
        return new HttpEntity<>(jsonBody, authBearerHeader);
    }

    public static HttpEntity<String> convertSW360ProjectToHttpEntity(SW360Project sw360Project, HttpHeaders header) throws JsonProcessingException {
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
}
