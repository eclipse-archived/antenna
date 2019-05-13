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

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AntennaTestingUtils {

    private static final String testUrl = "https://www.eclipse.org";

    private AntennaTestingUtils() {
        // only static methods
    }


    public static void checkInternetConnectionAndAssume(BiConsumer<String, Boolean> assumer) {
        checkInternetConnection()
                .ifPresent(e -> assumer.accept("Can not reach the internet (due to " + e.getClass().getSimpleName() + ")", false));
    }

    public static Optional<Exception> checkInternetConnection() {
        return checkInternetConnection(testUrl);
    }

    public static Optional<Exception> checkInternetConnection(String url) {
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

    public static void setVariableValueInObject(Object object, String variable, Object value) throws IllegalAccessException {
        Field field = getFieldIncludingSuperclasses(variable, object.getClass());
        field.setAccessible(true);
        field.set(object, value);
    }

    private static Field getFieldIncludingSuperclasses(String fieldName, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException var5) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getFieldIncludingSuperclasses(fieldName, superclass);
            } else {
                // This is only a testing util, so it's okay to throw a runtime exception here.
                throw new RuntimeException("Could not find field " + fieldName);
            }
        }
    }
}
