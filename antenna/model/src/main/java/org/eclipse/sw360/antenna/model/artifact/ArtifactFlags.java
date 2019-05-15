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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArtifactFlags
        implements IPrettyPrintable {
    private final Map<String,Boolean> flags = new HashMap<>();

    public String prettyPrint() {
        return flags.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().toString())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public void setFlag(String key, boolean value) {
        flags.put(key,value);
    }

    public boolean getFlag(String key) {
        return Optional.ofNullable(flags.get(key))
                .orElse(false);
    }

    public Map<String,Boolean> getRawContent() {
        return new HashMap<>(flags);
    }

    public boolean isEmpty() {
        return flags.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactFlags that = (ArtifactFlags) o;
        return Objects.equals(flags, that.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags);
    }
}
