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
package org.eclipse.sw360.antenna.http.api;

import java.nio.file.Path;

/**
 * <p>
 * An interface that allows the definition of HTTP requests to be executed.
 * </p>
 * <p>
 * This interface provides methods to set the single properties of an HTTP
 * request. This can be done in a convenient way using method chaining. A
 * client only has to set the properties that are relevant for a specific
 * request; as a minimum, the request URI must be set. Note that there is no
 * {@code build()} method to conclude the definition of the request; this is
 * not necessary as the HTTP client can figure out itself when to collect the
 * properties that have been defined.
 * </p>
 */
public interface RequestBuilder {
    /**
     * Sets the HTTP method to be used for the request. If no method is set
     * explicitly, the default is GET.
     *
     * @param method the HTTP method for the request
     * @return this request builder
     */
    RequestBuilder method(Method method);

    /**
     * Set the URI for the request.
     *
     * @param uri the request URI
     * @return this request builder
     */
    RequestBuilder uri(String uri);

    /**
     * Sets a request header.
     *
     * @param name  the header name
     * @param value the header value
     * @return this request builder
     */
    RequestBuilder header(String name, String value);

    /**
     * Sets the request body as a string. Based on the media type, the content
     * header is set.
     *
     * @param body      the request body as string
     * @param mediaType the media type of the content
     * @return this request builder
     */
    RequestBuilder bodyString(String body, String mediaType);

    /**
     * Sets the request body as a file. This can be used to upload files to a
     * server. Based on the media type, the content header is set.
     *
     * @param path      the path to the file to be uploaded
     * @param mediaType the media type of the content
     * @return this request builder
     */
    RequestBuilder bodyFile(Path path, String mediaType);

    /**
     * Sets the request body as an object that is serialized to JSON. This
     * method uses an internal JSON object mapper to generate a JSON
     * representation from the object passed in. It also automatically sets a
     * correct {@code Content-Type} header.
     *
     * @param payload the object to be used as request payload
     * @return this request builder
     */
    RequestBuilder bodyJson(Object payload);

    /**
     * Adds a part of a multi-part request to this builder. When using this
     * method, a multi-part request is generated. It has to be called for each
     * part. The single parts are defined via {@code RequestProducer} objects
     * that are passed new {@code RequestBuilder} instances for the definition
     * of the parts. Note that for the definition of a part only a subset of
     * the methods provided by the {@code RequestBuilder} interface makes
     * sense. When using this method to define a multi-part request, the other
     * methods for setting the request body should not be used; they do not
     * have any effect then.
     *
     * @param name         the name of the part
     * @param partProducer the producer for the request part
     * @return this request builder
     */
    RequestBuilder bodyPart(String name, RequestProducer partProducer);

    /**
     * An enumeration class for the HTTP methods supported by the HTTP client.
     */
    enum Method {
        GET, POST, PUT, PATCH
    }
}
