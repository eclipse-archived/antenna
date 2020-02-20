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

/**
 * <p>
 * An interface for components that can produce HTTP requests to be executed.
 * </p>
 * <p>
 * Implementations of this interface are used to generate requests in a way
 * independent on a concrete HTTP library. An implementation is passed a
 * {@link RequestBuilder} object and can use it to set all the properties
 * required for a specific request. The information provided by the
 * implementation is then collected to construct the actual request
 * representation.
 * </p>
 */
@FunctionalInterface
public interface RequestProducer {
    /**
     * Defines a request to be executed by configuring the
     * {@code RequestBuilder} passed as argument.
     *
     * @param builder the builder to define the request
     */
    void produceRequest(RequestBuilder builder);
}
