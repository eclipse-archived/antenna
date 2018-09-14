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
package org.eclipse.sw360.antenna.testing.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import static org.junit.Assume.assumeTrue;

public class AntennaTestingUtils {

    private static final String testUrl = "https://www.eclipse.org";

    private AntennaTestingUtils() {
        // only static methods
    }

    private static Optional<Exception> checkInternetConnection(String url) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection)
                    new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(e);
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static void assumeToBeConnectedToTheInternet() {
        checkInternetConnection(testUrl)
                .ifPresent(e -> assumeTrue("Can not reach the internet (due to " + e.getClass().getSimpleName() + ")", false));
    }
}
