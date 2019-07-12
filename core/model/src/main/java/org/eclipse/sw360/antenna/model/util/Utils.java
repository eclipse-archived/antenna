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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class Utils {

    private Utils() {
        // only utils
    }

    public static <T> Collection<T> iteratorToCollection(Iterator<T> iterator) {
        Collection<T> licenses = new ArrayList<>();
        while (iterator.hasNext()) {
            licenses.add(iterator.next());
        }
        return licenses;
    }

    /*
     * The function File::getParentFile is known to return null, this wraps the return value into an Optional
     */
    public static Optional<File> getParent(File file) {
        return Optional.ofNullable(file)
                .map(File::getParentFile);
    }

    /*
     * The function Path::getParentFile is known to return null, this wraps the return value into an Optional
     */
    public static Optional<Path> getParent(Path path) {
        return Optional.ofNullable(path)
                .map(Path::getParent);
    }
}
