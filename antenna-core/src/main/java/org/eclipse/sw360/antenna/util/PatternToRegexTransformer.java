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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a valid regex from the given pattern. PatternToRegexTransformer
 */
public final class PatternToRegexTransformer {
    private PatternToRegexTransformer() {
        // util
    }

    /**
     * Builds a valid regex from the given pattern.
     * 
     * @param Pattern
     *            that will be transformed to a regex.
     */

    public static String buildFileRegex(List<String> filesToScanPattern) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < filesToScanPattern.size(); i++) {
            if (i == 0) {
                regex.append("(");
            }
            if (i > 0) {
                regex.append("|");
            }
            regex.append("(?:");
            String wildcardPattern = filesToScanPattern.get(i);
            wildcardPattern = convertToRegexLiteral(wildcardPattern);
            wildcardPattern = wildcardPattern.replaceAll(Pattern.quote("?"), ".");
            /*
             * Match any * not preceded and not followed by another * and
             * replace it.
             */
            wildcardPattern = wildcardPattern.replaceAll("(?<!\\*)\\*(?!\\*)", "[^/]*");
            wildcardPattern = wildcardPattern.replaceAll(Pattern.quote("**/"), ".*/?");
            regex.append(wildcardPattern);
            regex.append(")");
            if (i == filesToScanPattern.size() - 1) {
                regex.append(")");
            }
        }
        return regex.toString();
    }

    public static String convertToRegexLiteral(String directory) {
        directory = directory.replaceAll(Pattern.quote("\\"), "/");
        directory = directory.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("."));
        return directory;
    }

}
