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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class UriCheck {
    private UriCheck() {
        // Utility class
    }

    public static boolean isRepositoryUriValid(URI repository) {
        if ("file".equals(repository.getScheme())) {
            return localRepositoryFileExists(repository);
        } else if ("http".equals(repository.getScheme()) || "https".equals(repository.getScheme())) {
            return isValidURL(repository);
        } else {
            System.err.println("Repository protocol of " + repository.toString() + "unknown. Please use a local repositories (file URI) or http(s) based repositories.");
            return false;
        }
    }

    private static boolean localRepositoryFileExists(URI repository) {
        if(!new File(repository).isDirectory()) {
            System.err.println("Local Repository " + repository.toString() + " does not exist.");
            return false;
        }
        return true;
    }

    private static boolean isValidURL(URI repository) {
        try {
            new URL(repository.toString());
        } catch (MalformedURLException e) {
            System.err.println("HTTP(S) based repositories url " + repository.toString() + " is malformed.");
            return false;
        }
        return true;
    }
}
