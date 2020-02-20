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

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MultipartBody;
import org.eclipse.sw360.antenna.http.api.RequestBuilder;
import org.eclipse.sw360.antenna.http.api.RequestProducer;

/**
 * <p>
 * A specialized {@code RequestBuilder} implementation for constructing a part
 * of a multi-part request.
 * </p>
 * <p>
 * An instance of this class is created by {@link RequestBuilderImpl} when the
 * {@link org.eclipse.sw360.antenna.http.api.RequestBuilder#bodyPart(String, RequestProducer)}
 * method is called. It can be used to define the content and headers of the
 * new part. Note that other methods that are not useful in this context
 * throw an {@code UnsupportedOperationException}.
 * </p>
 */
class MultipartRequestBuilder extends RequestBuilderImpl {
    /**
     * The name of the part under construction.
     */
    private final String partName;

    /**
     * Creates a new instance of {@code MultiPartRequestBuilder} to build a
     * body part of a multi-part request.
     *
     * @param mapper the JSON object mapper
     * @param name   the name of this part
     */
    public MultipartRequestBuilder(ObjectMapper mapper, String name) {
        super(mapper);
        partName = name;
    }

    /**
     * {@inheritDoc} This implementation throws an exception, as the URI cannot
     * be set for a body part.
     */
    @Override
    public RequestBuilder uri(String uri) {
        throw new UnsupportedOperationException("This property cannot be changed for a body part!");
    }

    /**
     * {@inheritDoc} This implementation throws an exception, as the method
     * cannot be set for a body part.
     */
    @Override
    public RequestBuilder method(Method method) {
        throw new UnsupportedOperationException("This property cannot be changed for a body part!");
    }

    /**
     * {@inheritDoc} This implementation throws an exception, as it is not
     * supported to have a body part consisting itself of multiple parts.
     */
    @Override
    public RequestBuilder bodyPart(String name, RequestProducer partProducer) {
        throw new UnsupportedOperationException("This operation is not available for a body part!");
    }

    /**
     * Adds the body part that was configured using this builder to the given
     * {@code MultipartBody.Builder}. This method is called by the parent
     * request builder in order to construct the final request body from all
     * parts.
     *
     * @param multipartBuilder the {@code MultipartBody.Builder}
     */
    public void addMultipartBody(MultipartBody.Builder multipartBuilder) {
        multipartBuilder.addFormDataPart(partName, getFileName(), getBody());
    }
}
