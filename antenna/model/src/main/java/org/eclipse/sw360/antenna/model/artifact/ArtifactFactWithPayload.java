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

import java.util.Objects;
import java.util.Optional;

public abstract class ArtifactFactWithPayload<T> implements ArtifactFact {
    private final T payload;

    public ArtifactFactWithPayload(T payload) {
        this.payload = payload;
    }

    public T get() {
        return payload;
    }

    protected String getEmptyStringRepresentation() {
        return "EMPTY";
    }

    @Override
    public ArtifactFact mergeWith(ArtifactFact resultWithPrecedence) {
        return resultWithPrecedence;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(payload)
                .map(Objects::toString)
                .orElse(getEmptyStringRepresentation());
    }

    @Override
    public String prettyPrint() {
        return "Set " + getFactContentName() + " to " + toString();
    }

    @Override
    public boolean isEmpty() {
        return get() == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactFactWithPayload<?> that = (ArtifactFactWithPayload<?>) o;
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), payload);
    }
}
