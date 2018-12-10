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

package org.eclipse.sw360.antenna.model.artifact;

import java.util.regex.Pattern;

public class ArtifactSelectorHelper {
    private ArtifactSelectorHelper() {
        // only static methods
    }


    public static boolean compareObjectsAsWildcard(Object regex, Object input) {
        if (regex == null || regex.equals(input)) {
            return true;
        }
        if (input == null) {
            return false;
        }
        return compareStringsAsWildcard(regex.toString(), input.toString());
    }

    public static boolean compareStringsAsWildcard(String regex, String input) {
        if(regex == null) {
            return true;
        }
        return regex.equals(input) || Pattern.matches(
                regex.trim().replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*?"),
                input != null ? input : "");
    }
}
