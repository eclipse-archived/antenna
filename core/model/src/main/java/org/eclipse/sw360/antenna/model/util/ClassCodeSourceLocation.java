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
package org.eclipse.sw360.antenna.model.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class ClassCodeSourceLocation {
    public static String getClassCodeSourceLocationAsString(Class<?> clazz) throws URISyntaxException {
        URI codeSourceLocationUri = getClassCodeSourceLocationURI(clazz);
        return new File(codeSourceLocationUri).getPath();
    }

    public static URI getClassCodeSourceLocationURI(Class<?> clazz) throws URISyntaxException {
        return clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
    }
}
