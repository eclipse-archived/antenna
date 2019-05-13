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

import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

public class SW360Component extends SW360HalResource<LinkObjects, SW360ComponentEmbedded> {
    private String name;
    private SW360ComponentType componentType;
    private String type;
    private String createdOn;

    public String getName() {
        return this.name;
    }

    public SW360Component setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public SW360Component setType(String type) {
        this.type = type;
        return this;
    }

    public String getCreatedOn() {
        return this.createdOn;
    }

    public SW360Component setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public SW360ComponentType getComponentType() {
        return this.componentType;
    }

    public SW360Component setComponentType(SW360ComponentType componentType) {
        this.componentType = componentType;
        return this;
    }
}