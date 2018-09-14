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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.HashMap;
import java.util.Map;

public class SW360HalResource<L extends LinkObjects, E extends Embedded> extends ResourceSupport {
    @JsonProperty("_links")
    private L _links;
    @JsonProperty("_embedded")
    private E _embedded;
    private Map<String, Object> additionalProperties = new HashMap<>();

    public L get_Links() {
        return _links;
    }

    public SW360HalResource<L, E> set_Links(L _links) {
        this._links = _links;
        return this;
    }

    public E get_Embedded() {
        return _embedded;
    }

    public SW360HalResource<L, E> set_Embedded(E _embedded) {
        this._embedded = _embedded;
        return this;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
