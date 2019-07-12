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
package org.eclipse.sw360.antenna.api.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public class ContextExtension {

    private HashSet<ContextExtension.ContextExtensionElement> inner = new HashSet<>();

    public boolean put(Object object) {
        if (object == null) {
            return false;
        }
        return inner.add(new ContextExtensionElement(object));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class clazz) {
        return inner.stream()
                .filter(e -> e.matches(clazz))
                .findFirst()
                .map(ContextExtensionElement::unpack)
                .map(o -> (T) o);
    }

    private static class ContextExtensionElement {
        private Object object;

        ContextExtensionElement(Object object) {
            this.object = object;
        }

        Object unpack() {
            return object;
        }

        boolean matches(Class theirClass, Class ourClass) {
            return ourClass != null &&
                    (ourClass.equals(theirClass) ||
                            matches(theirClass, ourClass.getSuperclass()) ||
                            Arrays.stream(ourClass.getInterfaces())
                                    .anyMatch(interfaceClass -> matches(theirClass, interfaceClass)) ||
                            Arrays.stream(ourClass.getClasses())
                                    .anyMatch(innerClass -> matches(theirClass, innerClass)));
        }

        boolean matches(Class theirClass) {
            Class ourClass = object.getClass();
            return matches(theirClass, ourClass);
        }
    }
}
