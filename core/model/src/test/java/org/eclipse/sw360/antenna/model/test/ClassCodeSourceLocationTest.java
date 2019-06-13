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
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.util.ClassCodeSourceLocation;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static junit.framework.TestCase.fail;

public class ClassCodeSourceLocationTest {

    @Test
    public void pathFromStringDoesntThrowInvalidPathExceptionTest() throws URISyntaxException{
        try {
            Paths.get(ClassCodeSourceLocation.getClassCodeSourceLocationAsString(this.getClass()));
        } catch (InvalidPathException e) {
            fail("Should not have thrown invalid path exception");
        }
    }

    @Test
    public void pathFromUriDoesntThrowInvalidPathExceptionTest() throws URISyntaxException{
        try {
            Paths.get(ClassCodeSourceLocation.getClassCodeSourceLocationURI(this.getClass()));
        } catch (InvalidPathException e) {
            fail("Should not have thrown invalid path exception");
        }
    }
}
