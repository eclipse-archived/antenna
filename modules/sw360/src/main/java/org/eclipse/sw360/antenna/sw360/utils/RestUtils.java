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
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class RestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static HttpEntity<String> getHttpEntity(Map<String, Object> resourceMap, HttpHeaders authBearerHeader) {
        try {
            String jsonBody = objectMapper.writeValueAsString(resourceMap);
            return new HttpEntity<>(jsonBody, authBearerHeader);
        } catch (JsonProcessingException e) {
            throw new ExecutionException("Error when attempting to serialise the request body.", e);
        }
    }
    public static <T> HttpEntity<String> convertSW360ResourceToHttpEntity(T itemToConvert, HttpHeaders header) {
        try {
            String jsonBody = objectMapper.writeValueAsString(itemToConvert);
            return new HttpEntity<>(jsonBody, header);
        } catch (JsonProcessingException e) {
            throw new ExecutionException("Error when attempting to serialise the request body.", e);
        }
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360License sw360License, HttpHeaders header) {
        String shortName = sw360License.getShortName() == null ? "" : sw360License.getShortName();
        String fullName = sw360License.getFullName() == null ? shortName : sw360License.getFullName();

        Map<String, Object> license = new HashMap<>();
        license.put(SW360Attributes.LICENSE_FULL_NAME, fullName);
        license.put(SW360Attributes.LICENSE_SHORT_NAME, shortName);
        license.put(SW360Attributes.LICENSE_TEXT, sw360License.getText());
        return getHttpEntity(license, header);
    }

    public static HttpEntity<String> convertSW360ResourceToHttpEntity(SW360Attachment sw360Attachment, HttpHeaders header) {
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
