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

public interface IPrettyPrintable {
    /*
     * Should generate a pretty representation of the object, which can be printed to the console.
     * This can be thought of as a more verbose toString, which might go over multiple lines.
     */
    String prettyPrint();
}
