/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.sw360.antenna.http.api.HttpExecutionException;
import org.eclipse.sw360.antenna.http.api.RequestBuilder;
import org.eclipse.sw360.antenna.http.api.RequestProducer;

import java.nio.file.Path;

/**
 * <p>
 * Implementation of the {@code RequestBuilder} interface based on the builder
 * class of OkHttpClient.
 * </p>
 */
class RequestBuilderImpl implements RequestBuilder {
    /**
     * The mapper for doing JSON serialization.
     */
    private final ObjectMapper mapper;

    /**
     * The internal builder to delegate method calls to.
     */
    private final Request.Builder requestBuilder;

    /**
     * A builder for adding headers to the request.
     */
    private final Headers.Builder headersBuilder;

    /**
     * The name of the HTTP method to be invoked.
     */
    private String httpMethod;

    /**
     * The body of the request.
     */
    private RequestBody body;

    /**
     * Stores the name of a file to be uploaded. This field is only defined if
     * a body of type file has been set.
     */
    private String fileName;

    /**
     * A builder for constructing a multi-part request. This is used only if
     * the {@code bodyPart()} method is invoked.
     */
    private MultipartBody.Builder multipartBuilder;

    /**
     * Creates a new instance of {@code RequestBuilderImpl} to build a new
     * request.
     *
     * @param mapper the JSON object mapper
     */
    public RequestBuilderImpl(ObjectMapper mapper) {
        this.mapper = mapper;
        requestBuilder = new Request.Builder();
        headersBuilder = new Headers.Builder();
        httpMethod = Method.GET.name();
    }

    @Override
    public RequestBuilder method(Method method) {
        httpMethod = method.name();
        return this;
    }

    @Override
    public RequestBuilder uri(String uri) {
        requestBuilder.url(uri);
        return this;
    }

    @Override
    public RequestBuilder header(String name, String value) {
        headersBuilder.add(name, value);
        return this;
    }

    @Override
    public RequestBuilder bodyString(String str, String mediaType) {
        body = RequestBody.create(str, MediaType.parse(mediaType));
        return this;
    }

    @Override
    public RequestBuilder bodyFile(Path path, String mediaType) {
        body = RequestBody.create(path.toFile(), MediaType.parse(mediaType));
        Path fileNamePath = path.getFileName();
        fileName = (fileNamePath != null) ? fileNamePath.toString() : null;
        return this;
    }

    @Override
    public RequestBuilder bodyJson(Object payload) {
        try {
            return bodyString(mapper.writeValueAsString(payload), "application/json");
        } catch (JsonProcessingException e) {
            throw new HttpExecutionException(e);
        }
    }

    @Override
    public RequestBuilder bodyPart(String name, RequestProducer partProducer) {
        MultipartRequestBuilder builder = new MultipartRequestBuilder(mapper, name);
        partProducer.produceRequest(builder);
        if (multipartBuilder == null) {
            multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        }
        builder.addMultipartBody(multipartBuilder);
        return this;
    }

    /**
     * Returns the final request that has been configured so far.
     *
     * @return the request constructed by this builder
     */
    public Request build() {
        RequestBody requestBody = (multipartBuilder != null) ? multipartBuilder.build() : getBody();
        return requestBuilder.method(httpMethod, requestBody)
                .headers(getHeaders())
                .build();
    }

    /**
     * Returns the headers that have been defined using this builder.
     *
     * @return the request headers
     */
    Headers getHeaders() {
        return headersBuilder.build();
    }

    /**
     * Returns the request body that has been defined using this builder.
     *
     * @return the request body (may be <strong>null</strong>)
     */
    RequestBody getBody() {
        return body;
    }

    /**
     * Returns the name of a file to be uploaded or <strong>null</strong> if
     * this is not a request to upload a file.
     *
     * @return the name of a file to be uploaded
     */
    String getFileName() {
        return fileName;
    }
}
