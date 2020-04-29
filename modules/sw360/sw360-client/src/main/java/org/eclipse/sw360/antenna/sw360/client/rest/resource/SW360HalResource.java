/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.client.rest.resource;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public abstract class SW360HalResource<L extends LinkObjects, E extends Embedded> {
    private L _links = createEmptyLinks();
    private E _embedded = createEmptyEmbedded();

    public abstract L createEmptyLinks();
    public abstract E createEmptyEmbedded();

    @JsonIgnore
    public Self getId() {
        return _links.getSelf();
    }

    @JsonGetter("_links")
    public L get_Links() {
        return _links;
    }

    @JsonSetter("_links")
    public SW360HalResource<L, E> set_Links(L _links) {
        if (_links != null) {
            this._links = _links;
        }
        return this;
    }

    @JsonGetter("_embedded")
    public E get_Embedded() {
        return _embedded;
    }

    @JsonSetter("_embedded")
    public SW360HalResource<L, E> set_Embedded(E _embedded) {
        if (_embedded != null) {
            this._embedded = _embedded;
        }
        return this;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SW360HalResource<?, ?> that = (SW360HalResource<?, ?>) o;
        return Objects.equals(_links, that._links) &&
                Objects.equals(_embedded, that._embedded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_links, _embedded);
    }

    /**
     * <p>
     * A representation of an undefined {@code Embedded}.
     * </p>
     * <p>
     * The constant defined in this enum can be used to represent an
     * {@code Embedded} object when no real value is available. Note:
     * Defining this as an enum makes sure that only a single instance ever
     * exists.
     * </p>
     */
    public enum EmptyEmbedded implements Embedded {
        INSTANCE
    }
}
