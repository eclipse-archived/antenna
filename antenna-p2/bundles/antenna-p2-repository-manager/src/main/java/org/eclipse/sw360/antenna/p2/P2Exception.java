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

package org.eclipse.sw360.antenna.p2;

public class P2Exception extends Exception {
    private static final long serialVersionUID = -1326543266347264517L;

    public P2Exception() {
        super();
    }

    public P2Exception(String message) {
        super(message);
    }

    public P2Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
