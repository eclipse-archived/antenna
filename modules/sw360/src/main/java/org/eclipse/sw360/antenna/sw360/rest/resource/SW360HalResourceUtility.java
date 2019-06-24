/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest.resource;

import java.util.Optional;

public class SW360HalResourceUtility {

    private SW360HalResourceUtility() {
        // Utility
    }

    public static Optional<String> getLastIndexOfLinkObject(LinkObjects linkObj) {
        if (linkObj != null && linkObj.getSelf() != null) {
            String href = linkObj.getSelf().getHref();
            if (href != null && !href.isEmpty()) {
                int lastSlashIndex =  href.lastIndexOf('/');
                return Optional.of(href.substring(lastSlashIndex + 1));
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getLastIndexOfLinkObject(Self selfObj) {
        if (selfObj != null) {
            String href = selfObj.getHref();
            if (href != null && !href.isEmpty()) {
                int lastSlashIndex =  href.lastIndexOf('/');
                return Optional.of(href.substring(lastSlashIndex + 1));
            }
        }
        return Optional.empty();
    }
}
