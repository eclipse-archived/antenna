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
package org.eclipse.sw360.antenna.sw360.rest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class SW360AttachmentAwareClient<T extends SW360HalResource<?,?>> extends SW360Client {
    private static final String ATTACHMENTS_ENDPOINT = "/attachments";

    public SW360AttachmentAwareClient(boolean proxyUse, String proxyHost, int proxyPort) {
        super(proxyUse, proxyHost, proxyPort);
    }

    public abstract Class<T> getHandledClassType();

    private HttpEntity<String> buildJsonPart(SW360Attachment sw360Attachment) throws AntennaException {
        HttpHeaders jsonHeader = new HttpHeaders();
        jsonHeader.setContentType(MediaType.APPLICATION_JSON);
        return RestUtils.convertSW360ResourceToHttpEntity(sw360Attachment, jsonHeader);
    }

    public T uploadAndAttachAttachment(T itemToModify, Path fileToAttach, String kindToAttach, HttpHeaders header) throws AntennaException {
        if (!Files.exists(fileToAttach)) {
            throw new AntennaException("The file=[" + fileToAttach + "], which should be attached to release, does not exist");
        }

        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

        SW360Attachment sw360Attachment = new SW360Attachment(fileToAttach, kindToAttach);
        multipartRequest.add("attachment", buildJsonPart(sw360Attachment));
        multipartRequest.add("file", new FileSystemResource(fileToAttach));

        HttpHeaders newHeaders = RestUtils.deepCopyHeaders(header);
        newHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, newHeaders);

        return uploadAndAttachAttachment(itemToModify, fileToAttach, requestEntity);
    }

    private T uploadAndAttachAttachment(T itemToModify, Path fileToAttach, HttpEntity<MultiValueMap<String, Object>> requestEntity) throws AntennaException {
        final String self = itemToModify.get_Links().getSelf().getHref();
        ResponseEntity<T> response = restTemplate.postForEntity(self + ATTACHMENTS_ENDPOINT, requestEntity, getHandledClassType());

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new AntennaException("Body was null"));
        } else {
            throw new AntennaException("Request to get attach " + fileToAttach + " to " + self + " failed with "
                    + response.getStatusCode());
        }
    }
}
