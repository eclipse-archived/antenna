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
package org.eclipse.sw360.antenna.sw360.rest.resource.components;

import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

public class SW360SparseComponent extends SW360HalResource<LinkObjects, Embedded> {
    private String name;
    private SW360ComponentType componentType;

    public String getName() {
        return this.name;
    }

    public SW360SparseComponent setName(String name) {
        this.name = name;
        return this;
    }

    public SW360ComponentType getComponentType() {
        return this.componentType;
    }

    public SW360SparseComponent setComponentType(SW360ComponentType componentType) {
        this.componentType = componentType;
        return this;
    }
}