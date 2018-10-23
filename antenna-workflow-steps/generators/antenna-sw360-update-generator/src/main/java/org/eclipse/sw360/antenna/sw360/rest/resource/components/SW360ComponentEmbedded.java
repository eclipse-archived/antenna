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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.users.SW360User;

import java.util.List;

@JsonDeserialize(as = SW360ComponentEmbedded.class)
public class SW360ComponentEmbedded implements Embedded {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("sw360:components")
    private List<SW360Component> components;
    private SW360User createdBy;

    public List<SW360Component> getComponents() { return this.components; }

    public SW360ComponentEmbedded setComponents(List<SW360Component> components) {
        this.components = components;
        return this;
    }

    public SW360User getCreatedBy() {
        return createdBy;
    }

    public SW360ComponentEmbedded setCreatedBy(SW360User createdBy) {
        this.createdBy = createdBy;
        return this;
    }
}

