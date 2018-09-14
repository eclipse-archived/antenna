/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.util;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

public final class ScanFileFinder {
    public static Collection<File> collectFiles(Set<File> files, String pattern, File baseDirectory) {
        String path = baseDirectory.getAbsolutePath();
        path = PatternToRegexTransformer.convertToRegexLiteral(path);
        if (Pattern.matches(pattern, path)) {
            files.add(baseDirectory);
        } else if (baseDirectory.isDirectory()) {
            File[] baseDirectoryFiles = baseDirectory.listFiles();
            if (baseDirectoryFiles != null) {
                for (File child : baseDirectoryFiles) {
                    collectFiles(files, pattern, child);
                }
            }
        }
        return files;
    }

    private ScanFileFinder() {
        // util
    }
}
