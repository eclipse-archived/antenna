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
 * A runtime exception thrown by the Antenna HTTP library if an error during
 * internal processing occurs.
 * </p>
 * <p>
 * This exception class is used to report errors that are not related to I/O
 * problems (which are represented by {@code IOException} exceptions).
 * Typical examples are problems with the serialization or de-serialization of
 * JSON payload. Such problems should normally not occur when using the library
 * in its normal mode; therefore, this is a runtime exception.
 * </p>
 */
public class HttpExecutionException extends RuntimeException {
    public HttpExecutionException(String message) {
        super(message);
    }

    public HttpExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpExecutionException(Throwable cause) {
        super(cause);
    }
}
