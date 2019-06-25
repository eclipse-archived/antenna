/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Utils {


    public static <T> Collection<T> iteratorToCollection(Iterator<T> iterator) {
        Collection<T> licenses = new ArrayList<>();
        while (iterator.hasNext()) {
            licenses.add(iterator.next());
        }
        return licenses;
    }
}
