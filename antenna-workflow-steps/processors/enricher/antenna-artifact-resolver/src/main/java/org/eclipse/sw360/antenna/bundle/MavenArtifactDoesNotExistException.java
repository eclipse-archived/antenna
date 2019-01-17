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

package org.eclipse.sw360.antenna.bundle;

/**
 * This is a wrapper class for maven's ArtifactDoesNotExistException to allow maven to be an optional dependency
 */
public class MavenArtifactDoesNotExistException extends Exception {

    public MavenArtifactDoesNotExistException(String message) {
        super(message);
    }

    public MavenArtifactDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
