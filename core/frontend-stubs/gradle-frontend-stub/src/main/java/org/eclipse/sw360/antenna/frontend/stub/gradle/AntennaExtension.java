/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.stub.gradle;

public class AntennaExtension {
    private String pomPath;

    public String getPomPath() {
        return pomPath;
    }

    public void setPomPath(String pomPath) {
        this.pomPath = pomPath;
    }
}
