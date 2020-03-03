/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
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
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Map;

public class RestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private RestUtils() {}

    public static HttpEntity<String> getHttpEntity(Map<String, Object> resourceMap, HttpHeaders authBearerHeader) {
        try {
            String jsonBody = (resourceMap != null) ? objectMapper.writeValueAsString(resourceMap) : null;
            return new HttpEntity<>(jsonBody, authBearerHeader);
        } catch (JsonProcessingException e) {
            throw new SW360ClientException("Error when attempting to serialise the request body.", e);
        }
    }
    public static <T extends SW360HalResource<?, ?>> HttpEntity<String> convertSW360ResourceToHttpEntity(T itemToConvert, HttpHeaders header) {
        try {
            String jsonBody = objectMapper.writeValueAsString(itemToConvert);
            return new HttpEntity<>(jsonBody, header);
        } catch (JsonProcessingException e) {
            throw new SW360ClientException("Error when attempting to serialise the request body.", e);
        }
    }

    public static HttpHeaders deepCopyHeaders(HttpHeaders header) {
        return header.entrySet()
                .stream()
                .collect(HttpHeaders::new,
                        (h, e) -> e.getValue().forEach( v -> h.add(e.getKey(), v)),
                        (h1, h2) -> {throw new UnsupportedOperationException("Unsupported operation");});
    }
}
