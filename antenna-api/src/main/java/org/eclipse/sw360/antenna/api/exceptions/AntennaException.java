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
package org.eclipse.sw360.antenna.api.exceptions;

import java.util.function.Supplier;

public class AntennaException extends Exception {
    private static final long serialVersionUID = -1326543066347264517L;

    public AntennaException() {
        super();
    }

    public AntennaException(String message) {
        super(message);
    }

    public AntennaException(String message, Throwable cause) {
        super(message, cause);
    }
}
