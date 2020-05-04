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
    private L links = createEmptyLinks();
    private E embedded = createEmptyEmbedded();

    public abstract L createEmptyLinks();
    public abstract E createEmptyEmbedded();

    @JsonIgnore
    public Self getId() {
        return links.getSelf();
    }

    @JsonGetter("_links")
    public L getLinks() {
        return links;
    }

    @JsonSetter("_links")
    public SW360HalResource<L, E> setLinks(L _links) {
        if (_links != null) {
            this.links = _links;
        }
        return this;
    }

    @JsonGetter("_embedded")
    public E getEmbedded() {
        return embedded;
    }

    @JsonSetter("_embedded")
    public SW360HalResource<L, E> setEmbedded(E _embedded) {
        if (_embedded != null) {
            this.embedded = _embedded;
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
        return Objects.equals(links, that.links) &&
                Objects.equals(embedded, that.embedded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, embedded);
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
