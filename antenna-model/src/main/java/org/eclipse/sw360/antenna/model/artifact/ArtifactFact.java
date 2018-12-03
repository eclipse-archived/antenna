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

public interface ArtifactFact<T extends ArtifactFact>
        extends IPrettyPrintable {
    String getFactContentName(); // used for pretty printing
    boolean isEmpty();
    default T mergeWith(T resultWithPrecedence) {
        return resultWithPrecedence;
    }
    default Class<? extends ArtifactFact> getKey() {
        return this.getClass();
    }
}
