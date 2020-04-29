/*
 * Copyright (c) Bosch Software Innovations GmbH 2018-2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.Collections;

public class SW360ComponentAdapterUtils {

    private SW360ComponentAdapterUtils() {}

    public static void setComponentType(SW360Component component, boolean isProprietary) {
        if (isProprietary) {
            component.setComponentType(SW360ComponentType.INTERNAL);
        } else {
            component.setComponentType(SW360ComponentType.OSS);
        }
    }

    static SW360Component createFromRelease(SW360Release release) {
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName(release.getName());
        setComponentType(sw360Component, release.isProprietary());
        sw360Component.setCategories(Collections.singleton("Antenna"));
        return sw360Component;
    }

    static boolean isValidComponent(SW360Component component) {
        return component.getName() != null &&
                !component.getName().isEmpty() &&
                component.getCategories() != null &&
                !component.getCategories().isEmpty();
    }
}
