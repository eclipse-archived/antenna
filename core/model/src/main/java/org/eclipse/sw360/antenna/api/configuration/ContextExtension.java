/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.api.configuration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * A class for storing arbitrary extension objects and allowing type-safe
 * access to them.
 * </p>
 * <p>
 * This class allows the storage of objects that might be available only in
 * certain environments in which Antenna can be executed. It can be queried for
 * specific objects by their class and returns an {@code Optional} with the
 * extension object that may or may not be resolved.
 * </p>
 * <p>
 * Clients (typically workflow components) can ask for a specific extension by
 * passing in a concrete class or an interface. If there is an extension object
 * whose class is equal to the class passed in or extends it, it is returned.
 * Note that no efforts are made to find a best match; any object matching the
 * passed in class is returned.
 * </p>
 */
public class ContextExtension {

    private final Set<Object> inner = new HashSet<>();

    /**
     * Adds the given extension object. The object can then be queried via its
     * class or an interface that it implements. <strong>null</strong> objects
     * are not added; also, a single object can be added only once.
     *
     * @param object the extension object
     * @return a flag whether the object could be added
     */
    public boolean put(Object object) {
        if (object == null) {
            return false;
        }
        return inner.add(object);
    }

    /**
     * Returns an {@code Optional} with an extension object stored in this
     * object that has the passed in class or extends it. If no such object
     * can be found, result is an empty {@code Optional}.
     *
     * @param clazz the class, base class, or interface of the extension object
     * @param <T>   the type of the extension object
     * @return an {@code Optional} with the extension object matching this type
     */
    public <T> Optional<T> get(Class<? extends T> clazz) {
        return inner.stream()
                .filter(o -> matches(clazz, o))
                .findFirst()
                .map(clazz::cast);
    }

    /**
     * Checks whether the given object satisfies the passed in extension class.
     * This is the case if the object can be casted to this class.
     *
     * @param clazz  the extension class
     * @param object the object to be checked
     * @return a flag whether this object satisfies this extension class
     */
    private static boolean matches(Class<?> clazz, Object object) {
        return clazz.isInstance(object);
    }
}
