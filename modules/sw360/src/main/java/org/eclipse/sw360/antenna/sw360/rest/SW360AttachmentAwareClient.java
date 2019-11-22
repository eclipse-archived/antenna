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

import org.apache.commons.lang3.Validate;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360Attachment;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentList;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360AttachmentType;
import org.eclipse.sw360.antenna.sw360.rest.resource.attachments.SW360SparseAttachment;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.sw360.antenna.sw360.rest.SW360ClientUtils.*;

public abstract class SW360AttachmentAwareClient<T extends SW360HalResource<?,?>> extends SW360Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360AttachmentAwareClient.class);
    private static final String ATTACHMENTS_ENDPOINT = "/attachments";

    public SW360AttachmentAwareClient(ProxySettings proxySettings) {
        super(proxySettings);
    }

    public abstract Class<T> getHandledClassType();

    private HttpEntity<String> buildJsonPart(SW360Attachment sw360Attachment) {
        HttpHeaders jsonHeader = new HttpHeaders();
        jsonHeader.setContentType(MediaType.APPLICATION_JSON);
        return RestUtils.convertSW360ResourceToHttpEntity(sw360Attachment, jsonHeader);
    }

    public T uploadAndAttachAttachment(T itemToModify, Path fileToAttach, SW360AttachmentType kindToAttach, HttpHeaders header) {
        if (!Files.exists(fileToAttach)) {
            LOGGER.warn("The file=[" + fileToAttach + "], which should be attached to release, does not exist");
            return itemToModify;
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

    private T uploadAndAttachAttachment(T itemToModify, Path fileToAttach, HttpEntity<MultiValueMap<String, Object>> requestEntity) {
        final String self = itemToModify.get_Links().getSelf().getHref();
        try {
            ResponseEntity<T> response = restTemplate.postForEntity(self + ATTACHMENTS_ENDPOINT, requestEntity, getHandledClassType());

            checkRestStatus(response);
            Validate.validState(response.getBody() != null);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.warn("Request to attach {} to {} failed with {}", fileToAttach, self, e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return itemToModify;
        } catch (ExecutionException e) {
            LOGGER.warn("Request to attach {} to {} failed with {}", fileToAttach, self, e.getMessage());
            return itemToModify;
        }
    }

    public List<SW360SparseAttachment> getItemAttachments(T itemToModify, HttpHeaders header) {
        final String self = itemToModify.get_Links().getSelf().getHref();
        try {
            ResponseEntity<Resource<SW360AttachmentList>> response = doRestGET(self + ATTACHMENTS_ENDPOINT, header,
                    new ParameterizedTypeReference<Resource<SW360AttachmentList>>() {});
            checkRestStatus(response);
            Validate.validState(response.getBody() != null);
            return getSw360SparseAttachments(response);
        } catch (HttpServerErrorException e) {
            LOGGER.warn("Request to get attachments from {} failed with {}", self, e.getStatusCode());
            LOGGER.debug("Error: ", e);
            return new ArrayList<>();
        } catch (ExecutionException e) {
            LOGGER.warn("Request to get attachments from {} failed with {}", self, e.getMessage());
            LOGGER.debug("Error: ", e);
            return new ArrayList<>();
        }
    }

    public Optional<Path> downloadAttachment(String itemHref, SW360SparseAttachment attachment, Path downloadPath, HttpHeaders header) {
        String attachmentId = attachment.getAttachmentId();
        String url = itemHref + "/attachments/" + attachmentId;
        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(header);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
            checkRestStatus(response);
            Validate.validState(response.getBody() != null);

            Optional<byte[]> body = Optional.of(response.getBody());

            return Optional.of(Files.write(downloadPath.resolve(attachment.getFilename()), body.get()));
        } catch (HttpClientErrorException e) {
            LOGGER.warn("Request to get attachment {} from {} failed with {}", attachmentId, itemHref, e.getStatusCode());
            LOGGER.debug("Error: ", e);
        } catch (ExecutionException e) {
            LOGGER.warn("Request to get attachment {} from {} failed with {}", attachmentId, itemHref, e.getMessage());
            LOGGER.debug("Error: ", e);
        } catch (IOException e) {
            LOGGER.warn("Request to write downloaded attachment {} to {} failed with {}", attachment.getFilename(), downloadPath, e.getMessage());
            LOGGER.debug("Error: ", e);
        }
        return Optional.empty();
    }
}
